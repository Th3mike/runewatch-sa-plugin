package com.runewatchsa;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;
import net.runelite.client.util.Text;

public class RuneWatchSAOverlay extends Overlay
{
    private final Client client;
    private final RuneWatchSAConfig config;
    private final CaseManager caseManager;
    private final ModelOutlineRenderer modelOutlineRenderer;

    @Inject
    private RuneWatchSAOverlay(Client client, RuneWatchSAConfig config, CaseManager caseManager, ModelOutlineRenderer modelOutlineRenderer)
    {
        this.client = client;
        this.config = config;
        this.caseManager = caseManager;
        this.modelOutlineRenderer = modelOutlineRenderer;
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
        setPriority(OverlayPriority.MED);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (!config.highlightScammers())
        {
            return null;
        }

        for (Player player : client.getPlayers())
        {
            if (player == null || player.getName() == null)
            {
                continue;
            }

            String name = Text.standardize(player.getName());
            Case c = caseManager.get(name);

            if (c != null)
            {
                renderPlayer(graphics, player);
            }
        }

        return null;
    }

    private void renderPlayer(Graphics2D graphics, Player player)
    {
        if (config.drawPlayerName())
        {
            String text = player.getName() + " [SCAMMER]";
            Point textLocation = player.getCanvasTextLocation(graphics, text, player.getLogicalHeight() + 40);

            if (textLocation != null)
            {
                OverlayUtil.renderTextLocation(graphics, textLocation, text, Color.RED);
            }
        }

        if (config.drawOutline())
        {
            modelOutlineRenderer.drawOutline(player, 2, Color.RED, 0);
        }
    }
}
