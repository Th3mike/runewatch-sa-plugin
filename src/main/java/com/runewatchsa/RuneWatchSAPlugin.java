package com.runewatchsa;

import com.google.inject.Provides;
import com.google.inject.Injector;
import java.awt.image.BufferedImage;
import java.util.concurrent.ScheduledExecutorService;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.PlayerSpawned;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.Text;
import net.runelite.client.callback.ClientThread;

@Slf4j
@PluginDescriptor(
    name = "RuneWatch SA",
    description = "Shows players on the RuneWatch South America watchlist",
    tags = {"scam", "watch", "list", "south america", "sa"}
)
public class RuneWatchSAPlugin extends Plugin
{
    @Inject
    private Client client;

    @Inject
    private RuneWatchSAConfig config;

    @Inject
    private CaseManager caseManager;

    @Inject
    private ChatMessageManager chatMessageManager;

    @Inject
    private ScheduledExecutorService executor;

    @Inject
    private ClientToolbar clientToolbar;

    @Inject
    private ClientThread clientThread;

    @Inject
    private Injector injector;

    private RuneWatchSAPanel panel;
    private NavigationButton navButton;

    @Override
    protected void startUp() throws Exception
    {
        panel = injector.getInstance(RuneWatchSAPanel.class);
        panel.init(this, caseManager);

        caseManager.setOnDataLoaded((cases) -> {
            clientThread.invokeLater(() -> {
                panel.repopulate();
            });
        });

        // Trigger initial data load
        executor.execute(caseManager::refresh);

        if (config.showSidebarIcon())
        {
            setupNavButton();
        }
    }

    private void setupNavButton()
    {
        if (navButton != null) return;

        final BufferedImage icon = ImageUtil.loadImageResource(RuneWatchSAPlugin.class, "/icon.png");
        navButton = NavigationButton.builder()
                .tooltip("RuneWatch SA")
                .icon(icon)
                .priority(5)
                .panel(panel)
                .build();

        clientToolbar.addNavigation(navButton);
    }

    private void removeNavButton()
    {
        if (navButton == null) return;
        clientToolbar.removeNavigation(navButton);
        navButton = null;
    }

    @Override
    protected void shutDown() throws Exception
    {
        removeNavButton();
    }

    @Provides
    RuneWatchSAConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(RuneWatchSAConfig.class);
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event)
    {
        if (!event.getGroup().equals("runewatchsa")) return;

        if (event.getKey().equals("showSidebarIcon"))
        {
            if (config.showSidebarIcon())
            {
                setupNavButton();
            }
            else
            {
                removeNavButton();
            }
        }
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged)
    {
        if (gameStateChanged.getGameState() == GameState.LOGGING_IN)
        {
            executor.execute(caseManager::refresh);
        }
    }

    @Subscribe
    public void onPlayerSpawned(PlayerSpawned event)
    {
        if (!config.notifyOnRadius())
        {
            return;
        }

        final Player player = event.getPlayer();
        if (player == null || player.getName() == null) return;

        final String name = Text.standardize(player.getName());

        if (caseManager.get(name) != null)
        {
            final String message = new ChatMessageBuilder()
                .append(ChatColorType.HIGHLIGHT)
                .append("[RuneWatch SA] Alerta: O jogador " + player.getName() + " está próximo e consta na lista de scammers!")
                .build();

            chatMessageManager.queue(QueuedMessage.builder()
                .type(ChatMessageType.GAMEMESSAGE)
                .runeLiteFormattedMessage(message)
                .build());
        }
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event)
    {
        if (event.getMenuOption().equals("Trade with") && config.notifyOnTrade())
        {
            String rawName = Text.removeTags(event.getMenuTarget());
            String cleanName = rawName.split(" \\(")[0].trim();
            final String name = Text.standardize(cleanName);
            
            final Case c = caseManager.get(name);

            if (c != null)
            {
                final String message = new ChatMessageBuilder()
                    .append(ChatColorType.HIGHLIGHT)
                    .append("[RuneWatch SA] PERIGO: " + cleanName + " está na lista: " + c.getReason())
                    .build();

                chatMessageManager.queue(QueuedMessage.builder()
                    .type(ChatMessageType.GAMEMESSAGE)
                    .runeLiteFormattedMessage(message)
                    .build());
            }
        }
    }
}
