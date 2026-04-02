package com.runewatchsa;

import com.google.inject.Injector;
import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ScheduledExecutorService;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.swing.*;

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
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.client.util.LinkBrowser;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.events.ClientTick;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.Text;
import java.awt.Color;

@Slf4j
@PluginDescriptor(name = "RuneWatch SA", description = "Shows players on the RuneWatch South America watchlist", tags = {
        "scam", "watch", "list", "south america", "sa", "runewatch" })
public class RuneWatchSAPlugin extends Plugin {
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
    private OverlayManager overlayManager;

    @Inject
    private RuneWatchSAOverlay overlay;

    private RuneWatchSAPanel panel;
    private NavigationButton navButton;

    @Override
    protected void startUp() throws Exception {
        panel = injector.getInstance(RuneWatchSAPanel.class);

        // Link data loading to panel refresh
        caseManager.setOnDataLoaded(panel::refresh);

        // Fetch data once on startup
        executor.execute(caseManager::refresh);

        if (config.showSidebarIcon()) {
            setupNavButton();
        }

        overlayManager.add(overlay);
    }

    private void setupNavButton() {
        if (navButton != null)
            return;

        BufferedImage icon = null;
        try {
            icon = ImageUtil.loadImageResource(getClass(), "icon.png");
        } catch (Exception e) {
            log.error("Failed to load plugin icon", e);
        }

        if (icon != null) {
            navButton = NavigationButton.builder()
                    .tooltip("RuneWatch SA")
                    .icon(icon)
                    .priority(5)
                    .panel(panel)
                    .build();

            clientToolbar.addNavigation(navButton);
        }
    }

    private void removeNavButton() {
        if (navButton == null)
            return;

        NavigationButton button = navButton;
        navButton = null;

        // Remove na thread UI para evitar NPEs intermitentes
        SwingUtilities.invokeLater(() -> {
            try {
                clientToolbar.removeNavigation(button);
            } catch (Exception e) {
                log.warn("Failed to remove navigation button", e);
            }
        });
    }

    @Override
    protected void shutDown() throws Exception {
        removeNavButton();
        overlayManager.remove(overlay);
    }

    @Provides
    RuneWatchSAConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(RuneWatchSAConfig.class);
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event) {
        if (!event.getGroup().equals("runewatchsa"))
            return;

        if (event.getKey().equals("showSidebarIcon")) {
            if (config.showSidebarIcon()) {
                setupNavButton();
            } else {
                removeNavButton();
            }
        }
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged) {
        if (gameStateChanged.getGameState() == GameState.LOGGING_IN) {
            executor.execute(caseManager::refresh);
        }
    }

    @Subscribe
    public void onPlayerSpawned(PlayerSpawned event) {
        if (!config.notifyOnRadius()) {
            return;
        }

        final Player player = event.getPlayer();
        if (player == null || player.getName() == null)
            return;

        final String name = Text.standardize(player.getName());
        Case c = caseManager.get(name);

        if (c != null) {
            final String message = new ChatMessageBuilder()
                    .append(Color.RED, "[RuneWatch SA] ")
                    .append(Color.ORANGE, "ALERTA: ")
                    .append(Color.WHITE, "O SCAMMER ")
                    .append(Color.RED, player.getName())
                    .append(Color.WHITE, " está próximo! Motivo: ")
                    .append(Color.YELLOW, c.getReason())
                    .build();

            chatMessageManager.queue(QueuedMessage.builder()
                    .type(ChatMessageType.GAMEMESSAGE)
                    .runeLiteFormattedMessage(message)
                    .build());
        }
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event) {
        if (event.getMenuOption().equals("Trade with") && config.notifyOnTrade()) {
            // O target vem como "<col=ffffff>Nome</col> (level-126)"
            String rawName = Text.removeTags(event.getMenuTarget());
            // Remove o "(level-126)"
            String cleanName = rawName.split(" \\(")[0].trim();
            final String name = Text.standardize(cleanName);

            final Case c = caseManager.get(name);

            if (c != null) {
                final String message = new ChatMessageBuilder()
                        .append(Color.RED, "[RuneWatch SA] ")
                        .append(Color.ORANGE, "PERIGO: ")
                        .append(Color.WHITE, "Você está negociando com o SCAMMER ")
                        .append(Color.RED, cleanName)
                        .append(Color.WHITE, ". Ele está na lista: ")
                        .append(Color.YELLOW, c.getReason())
                        .build();

                chatMessageManager.queue(QueuedMessage.builder()
                        .type(ChatMessageType.GAMEMESSAGE)
                        .runeLiteFormattedMessage(message)
                        .build());
            }
        }
    }

    @Subscribe
    public void onMenuEntryAdded(MenuEntryAdded event) {
        if (!config.showInvestigateOption()) {
            return;
        }

        final String option = event.getOption();
        if (!option.equals("Trade with") && !option.equals("Walk here")) {
            return;
        }

        final Player player = event.getMenuEntry().getPlayer();
        if (player == null) {
            return;
        }

        final String name = Text.standardize(player.getName());
        final Case c = caseManager.get(name);

        if (c != null) {
            client.createMenuEntry(-1)
                    .setOption("Investigate")
                    .setTarget(event.getTarget())
                    .setType(MenuAction.RUNELITE)
                    .onClick(e -> LinkBrowser.browse(c.getEvidence()));
        }
    }
}
