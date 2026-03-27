package com.runewatchsa;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import net.runelite.client.ui.components.IconTextField;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.PluginErrorPanel;
import net.runelite.client.util.LinkBrowser;
import java.awt.Cursor;
import java.awt.FlowLayout;

public class RuneWatchSAPanel extends PluginPanel
{
    private final IconTextField searchBar = new IconTextField();
    private final JPanel listContainer = new JPanel();
    private final PluginErrorPanel errorPanel = new PluginErrorPanel();
    private final CaseManager caseManager;

    private List<Case> filteredCases = new ArrayList<>();
    private int currentPage = 0;
    private static final int ITEMS_PER_PAGE = 6;

    @Inject
    public RuneWatchSAPanel(CaseManager caseManager)
    {
        super();
        this.caseManager = caseManager;

        setBorder(null);
        setLayout(new BorderLayout());
        setBackground(ColorScheme.DARK_GRAY_COLOR);

        // Header / Search Area
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setBorder(new EmptyBorder(0, 0, 10, 0));

        JLabel title = new JLabel("RuneWatch SA", SwingConstants.CENTER);
        title.setFont(FontManager.getRunescapeBoldFont());
        title.setBorder(new EmptyBorder(15, 0, 10, 0)); // Aumentado para 15
        headerPanel.add(title, BorderLayout.NORTH);

        searchBar.setIcon(IconTextField.Icon.SEARCH);
        searchBar.setPreferredSize(new Dimension(PluginPanel.PANEL_WIDTH, 35));
        searchBar.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        searchBar.setHoverBackgroundColor(ColorScheme.DARK_GRAY_HOVER_COLOR);
        searchBar.getDocument().addDocumentListener(new DocumentListener()
        {
            @Override
            public void insertUpdate(DocumentEvent e) { updateFilter(); }
            @Override
            public void removeUpdate(DocumentEvent e) { updateFilter(); }
            @Override
            public void changedUpdate(DocumentEvent e) { updateFilter(); }
        });
        headerPanel.add(searchBar, BorderLayout.CENTER);

        JButton refreshBtn = new JButton("Atualizar Casos");
        refreshBtn.setFont(FontManager.getRunescapeSmallFont());
        refreshBtn.setFocusable(false);
        refreshBtn.addActionListener(e -> {
            caseManager.refresh();
        });

        // Wrapper para dar margem real no topo do botão
        JPanel btnWrapper = new JPanel(new BorderLayout());
        btnWrapper.setBackground(ColorScheme.DARK_GRAY_COLOR);
        btnWrapper.setBorder(new EmptyBorder(10, 0, 0, 0));
        btnWrapper.add(refreshBtn, BorderLayout.CENTER);

        headerPanel.add(btnWrapper, BorderLayout.SOUTH);

        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BorderLayout());
        mainContent.setBackground(ColorScheme.DARK_GRAY_COLOR);
        mainContent.add(headerPanel, BorderLayout.NORTH);

        // List Container with BoxLayout (stacks items top-to-bottom)
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        listContainer.setBackground(ColorScheme.DARK_GRAY_COLOR);
        
        mainContent.add(listContainer, BorderLayout.CENTER);
        
        add(mainContent, BorderLayout.NORTH);

        // Error Panel (for empty states)
        errorPanel.setContent("Nenhum caso encontrado", "Tente buscar por outro nome ou aguarde a atualização da rede.");
        
        refresh();
    }

    public void refresh()
    {
        updateFilter();
    }

    private void updateFilter()
    {
        String query = searchBar.getText().toLowerCase();
        filteredCases = caseManager.getCases().stream()
            .filter(c -> c.getName().toLowerCase().contains(query) || 
                         (c.getNameHistory() != null && c.getNameHistory().stream().anyMatch(h -> h.toLowerCase().contains(query))))
            .collect(Collectors.toList());
        
        currentPage = 0;
        rebuild();
    }

    private void rebuild()
    {
        listContainer.removeAll();

        if (filteredCases.isEmpty())
        {
            listContainer.add(errorPanel);
        }
        else
        {
            int start = currentPage * ITEMS_PER_PAGE;
            int end = Math.min(start + ITEMS_PER_PAGE, filteredCases.size());

            for (int i = start; i < end; i++)
            {
                listContainer.add(createCaseRow(filteredCases.get(i)));
                listContainer.add(Box.createVerticalStrut(8));
            }

            if (filteredCases.size() > ITEMS_PER_PAGE)
            {
                listContainer.add(createPaginationPanel());
            }
        }

        listContainer.revalidate();
        listContainer.repaint();
    }

    private JPanel createCaseRow(Case c)
    {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        row.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Painel de conteúdo
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(ColorScheme.DARKER_GRAY_COLOR);

        JLabel nameLabel = new JLabel("RSN: " + c.getName());
        nameLabel.setFont(FontManager.getRunescapeBoldFont());
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(nameLabel);

        JLabel reasonLabel = new JLabel(c.getReason().toUpperCase());
        reasonLabel.setFont(FontManager.getRunescapeBoldFont());
        reasonLabel.setForeground(Color.RED);
        reasonLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(reasonLabel);

        JLabel valueLabel = new JLabel("Valor: " + c.getValue() + " ");
        valueLabel.setFont(FontManager.getRunescapeSmallFont());
        valueLabel.setForeground(Color.YELLOW);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        valueLabel.setBorder(new EmptyBorder(5, 0, 0, 0));
        content.add(valueLabel);

        // Informações das Datas (Mais claras)
        /*JLabel submissionLabel = new JLabel("Enviado em: " + c.getSubmissionDate());
        submissionLabel.setFont(FontManager.getRunescapeSmallFont());
        submissionLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR); // Cor mais clara!
        submissionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(submissionLabel);*/

        JLabel approvalLabel = new JLabel("Confirmado em: " + c.getApprovalDate());
        approvalLabel.setFont(FontManager.getRunescapeSmallFont());
        approvalLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR); // Cor mais clara!
        approvalLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(approvalLabel);

        // Histórico de nomes (Compacto para não quebrar o layout)
        if (c.getNameHistory() != null && !c.getNameHistory().isEmpty()) {
            List<String> history = c.getNameHistory();
            String historyText = "Nomes ant.: " + history.get(0);
            
            if (history.size() > 1) {
                historyText += ", " + history.get(1);
            }
            if (history.size() > 2) {
                historyText += " + " + (history.size() - 2) + " outros";
            }
            
            JLabel historyLabel = new JLabel(historyText);
            historyLabel.setFont(FontManager.getRunescapeSmallFont());
            historyLabel.setForeground(ColorScheme.DARK_GRAY_HOVER_COLOR); // Discreto
            historyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            historyLabel.setBorder(new EmptyBorder(5, 0, 0, 0));
            content.add(historyLabel);
        }

        // Botão de Detalhes
        JButton detailsBtn = new JButton("Ver Detalhes");
        detailsBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        detailsBtn.setFont(FontManager.getRunescapeSmallFont());
        detailsBtn.setFocusable(false);
        detailsBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailsBtn.setBorder(new EmptyBorder(5, 0, 5, 0));
        detailsBtn.addActionListener(e -> LinkBrowser.browse(c.getEvidence()));
        
        row.add(content, BorderLayout.CENTER);
        row.add(detailsBtn, BorderLayout.SOUTH);

        return row;
    }

    private JPanel createPaginationPanel()
    {
        JPanel pagination = new JPanel(new GridLayout(1, 3, 5, 0)); // 3 colunas para acomodar o texto
        pagination.setBorder(new EmptyBorder(10, 0, 10, 0));
        pagination.setBackground(ColorScheme.DARK_GRAY_COLOR);

        JButton prev = new JButton("Anterior");
        prev.setEnabled(currentPage > 0);
        prev.addActionListener(e -> { currentPage--; rebuild(); });

        int totalPages = (int) Math.ceil((double) filteredCases.size() / ITEMS_PER_PAGE);
        JLabel pageInfo = new JLabel("Pág. " + (currentPage + 1) + " / " + totalPages, SwingConstants.CENTER);
        pageInfo.setFont(FontManager.getRunescapeSmallFont());
        pageInfo.setForeground(ColorScheme.LIGHT_GRAY_COLOR);

        JButton next = new JButton("Próximo");
        next.setEnabled((currentPage + 1) * ITEMS_PER_PAGE < filteredCases.size());
        next.addActionListener(e -> { currentPage++; rebuild(); });

        pagination.add(prev);
        pagination.add(pageInfo);
        pagination.add(next);
        
        return pagination;
    }
}
