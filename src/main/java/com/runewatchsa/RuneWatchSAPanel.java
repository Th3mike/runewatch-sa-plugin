package com.runewatchsa;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
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
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.DynamicGridLayout;
import net.runelite.client.util.LinkBrowser;

public class RuneWatchSAPanel extends PluginPanel
{
    private final CaseManager caseManager;

    private final JPanel listContainer = new JPanel();
    private final JTextField searchBar = new JTextField();
    private final JLabel pageLabel = new JLabel("Página 1 de 1");
    private final JButton prevBtn = new JButton("<");
    private final JButton nextBtn = new JButton(">");
    
    private int currentPage = 0;
    private static final int ITEMS_PER_PAGE = 6;
    private List<Case> filteredCases = new ArrayList<>();

    @Inject
    public RuneWatchSAPanel(CaseManager caseManager)
    {
        super();
        this.caseManager = caseManager;

        setBorder(new EmptyBorder(10, 10, 10, 10));
        setLayout(new BorderLayout());

        // Header Title
        JLabel title = new JLabel("RuneWatch SA");
        title.setFont(FontManager.getRunescapeBoldFont());
        title.setForeground(Color.WHITE);
        title.setBorder(new EmptyBorder(0, 0, 10, 0));
        add(title, BorderLayout.NORTH);

        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));

        // Search Bar
        searchBar.setPreferredSize(new Dimension(PluginPanel.PANEL_WIDTH, 35));
        searchBar.setMaximumSize(new Dimension(PluginPanel.PANEL_WIDTH, 35));
        searchBar.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        searchBar.setForeground(Color.WHITE);
        searchBar.setCaretColor(Color.WHITE);
        searchBar.setBorder(new EmptyBorder(5, 5, 5, 5));
        searchBar.getDocument().addDocumentListener(new DocumentListener()
        {
            @Override
            public void insertUpdate(DocumentEvent e) { updateFilter(); }
            @Override
            public void removeUpdate(DocumentEvent e) { updateFilter(); }
            @Override
            public void changedUpdate(DocumentEvent e) { updateFilter(); }
        });
        
        mainContent.add(searchBar);
        mainContent.add(Box.createVerticalStrut(10));

        // Refresh Button
        JButton refreshBtn = new JButton("Atualizar Casos");
        refreshBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        refreshBtn.addActionListener(e -> caseManager.refresh());
        mainContent.add(refreshBtn);

        mainContent.add(Box.createVerticalStrut(15));

        // Container para os cards
        listContainer.setLayout(new DynamicGridLayout(0, 1, 0, 10));
        mainContent.add(listContainer);

        add(mainContent, BorderLayout.CENTER);

        // Footer Pagination
        JPanel footer = new JPanel(new BorderLayout());
        JPanel paginationControls = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        
        prevBtn.setPreferredSize(new Dimension(40, 25));
        nextBtn.setPreferredSize(new Dimension(40, 25));
        
        prevBtn.addActionListener(e -> {
            if (currentPage > 0) {
                currentPage--;
                repopulate();
            }
        });
        
        nextBtn.addActionListener(e -> {
            if ((currentPage + 1) * ITEMS_PER_PAGE < filteredCases.size()) {
                currentPage++;
                repopulate();
            }
        });

        paginationControls.add(prevBtn);
        paginationControls.add(pageLabel);
        paginationControls.add(nextBtn);
        
        footer.add(paginationControls, BorderLayout.CENTER);
        footer.setBorder(new EmptyBorder(10, 0, 0, 0));

        add(footer, BorderLayout.SOUTH);
    }

    private void updateFilter()
    {
        String text = searchBar.getText().toLowerCase();
        filteredCases = caseManager.getCases().values().stream()
            .filter(c -> c.getName().toLowerCase().contains(text) || 
                         (c.getNameHistory() != null && c.getNameHistory().stream().anyMatch(h -> h.toLowerCase().contains(text))))
            .collect(Collectors.toList());
        
        currentPage = 0;
        repopulate();
    }

    public void repopulate()
    {
        listContainer.removeAll();

        if (searchBar.getText().isEmpty() || searchBar.getText().equals("Pesquisar jogador...")) {
            filteredCases = new ArrayList<>(caseManager.getCases().values());
        }

        int totalPages = Math.max(1, (int) Math.ceil((double) filteredCases.size() / ITEMS_PER_PAGE));
        pageLabel.setText(String.format("Página %d de %d", currentPage + 1, totalPages));

        prevBtn.setEnabled(currentPage > 0);
        nextBtn.setEnabled((currentPage + 1) * ITEMS_PER_PAGE < filteredCases.size());

        filteredCases.stream()
            .skip((long) currentPage * ITEMS_PER_PAGE)
            .limit(ITEMS_PER_PAGE)
            .forEach(c -> listContainer.add(createCaseRow(c)));

        listContainer.revalidate();
        listContainer.repaint();
    }

    private JPanel createCaseRow(Case c)
    {
        JPanel row = new JPanel();
        row.setLayout(new BorderLayout());
        row.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        row.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(ColorScheme.DARKER_GRAY_COLOR);

        JLabel nameLabel = new JLabel(c.getName());
        nameLabel.setFont(FontManager.getRunescapeBoldFont());
        nameLabel.setForeground(Color.WHITE);
        content.add(nameLabel);

        JLabel reasonLabel = new JLabel(c.getReason());
        reasonLabel.setFont(FontManager.getRunescapeSmallFont());
        reasonLabel.setForeground(Color.YELLOW);
        content.add(reasonLabel);

        JLabel valueLabel = new JLabel("Valor: " + c.getValue());
        valueLabel.setFont(FontManager.getRunescapeSmallFont());
        valueLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
        content.add(valueLabel);

        JLabel approvalLabel = new JLabel("Verificado: " + c.getApprovalDate());
        approvalLabel.setFont(FontManager.getRunescapeSmallFont());
        approvalLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
        content.add(approvalLabel);

        if (c.getNameHistory() != null && !c.getNameHistory().isEmpty())
        {
            String historyText = "Nomes ant.: " + String.join(", ", c.getNameHistory().stream().limit(2).collect(Collectors.toList()));
            if (c.getNameHistory().size() > 2) historyText += " + " + (c.getNameHistory().size() - 2);
            
            JLabel historyLabel = new JLabel(historyText);
            historyLabel.setFont(FontManager.getRunescapeSmallFont());
            historyLabel.setForeground(ColorScheme.DARK_GRAY_HOVER_COLOR);
            content.add(historyLabel);
        }

        row.add(content, BorderLayout.CENTER);

        JButton detailsBtn = new JButton("Detalhes");
        detailsBtn.setFont(FontManager.getRunescapeSmallFont());
        detailsBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        detailsBtn.addActionListener(e -> LinkBrowser.browse(c.getEvidence()));
        row.add(detailsBtn, BorderLayout.SOUTH);

        return row;
    }
}
