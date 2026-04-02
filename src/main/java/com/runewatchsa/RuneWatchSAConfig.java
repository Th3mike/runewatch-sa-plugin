package com.runewatchsa;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Keybind;

@ConfigGroup("runewatchsa")
public interface RuneWatchSAConfig extends Config {
    @ConfigItem(keyName = "notifyOnTrade", name = "Notificar ao negociar", description = "Notifica você quando negociar com um jogador da lista RuneWatch SA", position = 1)
    default boolean notifyOnTrade() {
        return true;
    }

    @ConfigItem(keyName = "notifyOnRadius", name = "Notificar por proximidade", description = "Notifica você quando um jogador da lista RuneWatch SA estiver por perto", position = 2)
    default boolean notifyOnRadius() {
        return true;
    }

    @ConfigItem(keyName = "showSidebarIcon", name = "Exibir Ícone Lateral", description = "Mostra ou esconde o ícone do RuneWatch SA no menu lateral do RuneLite", position = 3)
    default boolean showSidebarIcon() {
        return true;
    }

    @ConfigItem(keyName = "highlightScammers", name = "Sinalizar Scammers no Mundo", description = "Desenha marcações visuais em jogadores que estão na lista", position = 4)
    default boolean highlightScammers() {
        return true;
    }

    @ConfigItem(keyName = "drawPlayerName", name = "Mostrar Nome sobre o Scammer", description = "Exibe o nome do jogador em vermelho acima dele", position = 6)
    default boolean drawPlayerName() {
        return true;
    }

    @ConfigItem(keyName = "drawOutline", name = "Contorno no Scammer", description = "Desenha um contorno ao redor do modelo do jogador", position = 7)
    default boolean drawOutline() {
        return true;
    }

    @ConfigItem(
        keyName = "showInvestigateOption",
        name = "Opção 'Investigate' no Menu",
        description = "Adiciona a opção de investigar o jogador ao clicar com o botão direito",
        position = 8
    )
    default boolean showInvestigateOption() {
        return true;
    }
}
