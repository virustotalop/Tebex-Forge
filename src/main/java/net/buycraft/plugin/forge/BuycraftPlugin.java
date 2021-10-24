package net.buycraft.plugin.forge;

import net.buycraft.plugin.BuyCraftAPI;
import net.buycraft.plugin.IBuycraftPlatform;
import net.buycraft.plugin.data.QueuedPlayer;
import net.buycraft.plugin.data.responses.ServerInformation;
import net.buycraft.plugin.execution.DuePlayerFetcher;
import net.buycraft.plugin.execution.placeholder.NamePlaceholder;
import net.buycraft.plugin.execution.placeholder.PlaceholderManager;
import net.buycraft.plugin.execution.placeholder.UuidPlaceholder;
import net.buycraft.plugin.execution.strategy.CommandExecutor;
import net.buycraft.plugin.execution.strategy.PostCompletedCommandsTask;
import net.buycraft.plugin.execution.strategy.QueuedCommandExecutor;
import net.buycraft.plugin.forge.command.*;
import net.buycraft.plugin.shared.Setup;
import net.buycraft.plugin.shared.config.BuycraftConfiguration;
import net.buycraft.plugin.shared.config.BuycraftI18n;
import net.buycraft.plugin.shared.tasks.PlayerJoinCheckTask;
import net.buycraft.plugin.shared.util.AnalyticsSend;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import okhttp3.OkHttpClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.logging.Level;

@Mod(modid = "buycraftx", name = "BuycraftX", version = BuycraftPlugin.PLUGIN_VERSION, acceptableRemoteVersions = "*")
public class BuycraftPlugin {

    public static final String PLUGIN_VERSION = "${pluginVersion}";

    private static final Logger LOGGER = LogManager.getLogger("Tebex");

    public static final Style SUCCESS_STYLE = new Style().setColor(TextFormatting.GREEN);
    public static final Style INFO_STYLE = new Style().setColor(TextFormatting.YELLOW);
    public static final Style ERROR_STYLE = new Style().setColor(TextFormatting.RED);

    private final PlaceholderManager placeholderManager = new PlaceholderManager();
    private final BuycraftConfiguration configuration = new BuycraftConfiguration();
    private final Path baseDirectory = Paths.get("config", "buycraft");

    private final List<ForgeScheduledTask> scheduledTasks = new ArrayList<>();

    private MinecraftServer server;
    private static ScheduledExecutorService executor;

    private BuyCraftAPI apiClient;
    private DuePlayerFetcher duePlayerFetcher;
    private ServerInformation serverInformation;
    private OkHttpClient httpClient;
    private IBuycraftPlatform platform;
    private CommandExecutor commandExecutor;
    private BuycraftI18n i18n; //TODO Re-enable when forge fixes resource loading
    private PostCompletedCommandsTask completedCommandsTask;
    private PlayerJoinCheckTask playerJoinCheckTask;

    public BuycraftPlugin() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    public static class Debug implements Predicate<RunnableScheduledFuture<?>> {

        @Override
        public boolean test(RunnableScheduledFuture<?> runnableScheduledFuture) {
            return !runnableScheduledFuture.isPeriodic();
        }
    }

    // As as close to an onEnable as we are ever going to get :(
    @Mod.EventHandler
    public void onServerStarting(FMLServerStartingEvent event) {
        if (event.getServer().isDedicatedServer()) {
            server = event.getServer();
            executor = Executors.newScheduledThreadPool(2, r -> new Thread(r, "Buycraft Scheduler Thread"));

            platform = new ForgeBuycraftPlatform(this);

            this.registerTebexCommand(event,"tebex");
            this.registerTebexCommand(event,"buycraft");

            if (!configuration.isDisableBuyCommand())
                configuration.getBuyCommandName().forEach(cmd ->
                        event.registerServerCommand(new BuyCommand(this, cmd)));
            //endregion

            try {
                try {
                    Files.createDirectory(baseDirectory);
                } catch (FileAlreadyExistsException ignored) {
                }
                Path configPath = baseDirectory.resolve("config.properties");
                try {
                    configuration.load(configPath);
                } catch (NoSuchFileException e) {
                    // Save defaults
                    configuration.fillDefaults();
                    configuration.save(configPath);
                }
            } catch (IOException e) {
                getLogger().error("Unable to load configuration! The plugin will disable itself now.", e);
                return;
            }

            i18n = configuration.createI18n(); //TODO Re-enable when forge fixes resource loading
            getLogger().warn("Forcing english translations while we wait on a forge bugfix!");

            httpClient = Setup.okhttp(baseDirectory.resolve("cache").toFile());

            String serverKey = configuration.getServerKey();
            if (serverKey == null || serverKey.equals("INVALID")) {
                getLogger().info("Looks like this is a fresh setup. Get started by using 'buycraft secret <key>' in the console.");
            } else {
                getLogger().info("Validating your server key...");
                BuyCraftAPI client = BuyCraftAPI.create(configuration.getServerKey(), httpClient);
                try {
                    updateInformation(client);
                } catch (IOException e) {
                    getLogger().error(String.format("We can't check if your server can connect to Buycraft: %s", e.getMessage()));
                }
                apiClient = client;
            }

            placeholderManager.addPlaceholder(new NamePlaceholder());
            placeholderManager.addPlaceholder(new UuidPlaceholder());
            platform.executeAsyncLater(duePlayerFetcher = new DuePlayerFetcher(platform, configuration.isVerbose()), 1, TimeUnit.SECONDS);
            completedCommandsTask = new PostCompletedCommandsTask(platform);
            commandExecutor = new QueuedCommandExecutor(platform, completedCommandsTask);
            scheduledTasks.add(ForgeScheduledTask.Builder.create().withInterval(1).withDelay(1).withTask((Runnable) commandExecutor).build());
            scheduledTasks.add(ForgeScheduledTask.Builder.create().withAsync(true).withInterval(20).withDelay(20).withTask(completedCommandsTask).build());
            playerJoinCheckTask = new PlayerJoinCheckTask(platform);
            scheduledTasks.add(ForgeScheduledTask.Builder.create().withInterval(20).withDelay(20).withTask(playerJoinCheckTask).build());

            if (serverInformation != null) {
                scheduledTasks.add(ForgeScheduledTask.Builder.create()
                        .withAsync(true)
                        .withInterval(20 * 60 * 60 * 24)
                        .withTask(() -> {
                            try {
                                AnalyticsSend.postServerInformation(httpClient, configuration.getServerKey(), platform, server.isServerInOnlineMode());
                            } catch (IOException e) {
                                getLogger().warn("Can't send analytics", e);
                            }
                        })
                        .build());
            }
        }
    }

    private void registerTebexCommand(FMLServerStartingEvent event, String alias) {
        event.registerServerCommand(new TebexRootCmd(this, event.getServer(), alias)
                .addChild(new CouponCmd(this))
                .addChild(new ForceCheckCmd(this))
                .addChild(new InfoCmd(this))
                .addChild(new ReportCmd(this))
                .addChild(new SecretCmd(this))
        );
    }

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.player.getServer() != null && event.player.getServer().isDedicatedServer()) {
            if (apiClient == null) {
                return;
            }

            QueuedPlayer qp = duePlayerFetcher.fetchAndRemoveDuePlayer(event.player.getName());
            if (qp != null) {
                playerJoinCheckTask.queue(qp);
            }
        }
    }

    // I hate that forge after all these years still hasn't given us a nice scheduler. So here's my poor attempt at
    // DIYing one similar to how bukkits one works, with a nice builder thrown on top.
    @SubscribeEvent
    public void onTick(TickEvent.ServerTickEvent event) {
        if (server != null && server.isDedicatedServer() && event.phase == TickEvent.Phase.END) {
            scheduledTasks.forEach(task -> {
                if (task.getCurrentDelay() > 0) {
                    task.setCurrentDelay(task.getCurrentDelay() - 1);
                    return;
                }

                if (task.getInterval() > -1 && task.getCurrentIntervalTicks() > 0) {
                    task.setCurrentIntervalTicks(task.getCurrentIntervalTicks() - 1);
                    return;
                }

                if (!task.isAsync()) {
                    try {
                        task.getTask().run();
                    } catch (Exception e) {
                        platform.log(Level.SEVERE, "Error executing scheduled task!", e);
                    }
                } else {
                    executor.submit(task.getTask());
                }

                if (task.getInterval() > -1) {
                    task.setCurrentIntervalTicks(task.getInterval());
                }
            });
            scheduledTasks.removeIf(task -> task.getCurrentDelay() <= 0 && task.getInterval() <= -1);
        }
    }

    public Logger getLogger() {
        return LOGGER;
    }

    public MinecraftServer getServer() {
        return server;
    }

    public ScheduledExecutorService getExecutor() {
        return executor;
    }

    public PlaceholderManager getPlaceholderManager() {
        return placeholderManager;
    }

    public BuycraftConfiguration getConfiguration() {
        return configuration;
    }

    public void saveConfiguration() throws IOException {
        Path configPath = getBaseDirectory().resolve("config.properties");
        configuration.save(configPath);
    }

    public Path getBaseDirectory() {
        return baseDirectory;
    }

    public BuyCraftAPI getApiClient() {
        return apiClient;
    }

    public void setApiClient(BuyCraftAPI apiClient) {
        this.apiClient = apiClient;
    }

    public DuePlayerFetcher getDuePlayerFetcher() {
        return duePlayerFetcher;
    }

    public ServerInformation getServerInformation() {
        return serverInformation;
    }

    public void updateInformation(BuyCraftAPI client) throws IOException {
        serverInformation = client.getServerInformation().execute().body();
    }

    public OkHttpClient getHttpClient() {
        return httpClient;
    }

    public IBuycraftPlatform getPlatform() {
        return platform;
    }

    public CommandExecutor getCommandExecutor() {
        return commandExecutor;
    }

    public BuycraftI18n getI18n() {
        return i18n;
    }

    public PostCompletedCommandsTask getCompletedCommandsTask() {
        return completedCommandsTask;
    }

    public PlayerJoinCheckTask getPlayerJoinCheckTask() {
        return playerJoinCheckTask;
    }
}
