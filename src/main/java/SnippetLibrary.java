import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.sql.*;

public class SnippetLibrary extends JFrame {
    private static final String DB_URL = "jdbc:mysql://127.0.0.1:3307/sys";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "password";

    private String searchTerm;
    private JPopupMenu suggestionPopup = new JPopupMenu();

    public SnippetLibrary() {
        this(null);
    }

    public SnippetLibrary(String searchTerm) {
        this.searchTerm = searchTerm;
        setTitle("Code Snippet Library");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        Color bgColor = new Color(220, 220, 220);
        Color fgColor = new Color(30, 30, 30);
        Color boxColor = new Color(220, 220, 220);

        // Menu bar
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        menuBar.setBackground(new Color(0, 35, 102));
        menuBar.setForeground(fgColor);

        JMenu home = new JMenu("Home");
        JMenu lib = new JMenu("Snippet Library");
        JMenu editor = new JMenu("Editor");

        home.setForeground(new Color(220, 220, 220));
        lib.setForeground(new Color(220, 220, 220));
        editor.setForeground(new Color(220, 220, 220));
        home.setFont(new Font("SansSerif", Font.PLAIN, 16));
        lib.setFont(new Font("SansSerif", Font.PLAIN, 16));
        editor.setFont(new Font("SansSerif", Font.PLAIN, 16));

        home.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                new HomePage();
                dispose();
            }
        });
        editor.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                new EditorPage(null);
                dispose();
            }
        });

        lib.setEnabled(false);

        menuBar.add(home);
        menuBar.add(lib);
        menuBar.add(editor);
        setJMenuBar(menuBar);

        // Top panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(bgColor);
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("Snippet Library");
        titleLabel.setForeground(fgColor);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        topPanel.add(titleLabel, BorderLayout.WEST);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.setBackground(bgColor);

        JTextField searchField = new JTextField(20);
        JButton searchButton = new JButton("Search");

        if (searchTerm != null) {
            searchField.setText(searchTerm);
        }

        searchButton.addActionListener(e -> {
            String searchText = searchField.getText().trim();
            new SnippetLibrary(searchText);
            dispose();
        });

        // Add suggestion popup
        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                showSuggestions(searchField);
            }
        });

        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        topPanel.add(searchPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Main content area
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(bgColor);

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(bgColor);

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            PreparedStatement stmt;
            if (searchTerm != null && !searchTerm.isEmpty()) {
                stmt = conn.prepareStatement("SELECT * FROM snippets WHERE tag LIKE ? OR title LIKE ?");
                stmt.setString(1, "%" + searchTerm + "%");
                stmt.setString(2, "%" + searchTerm + "%");
            } else {
                stmt = conn.prepareStatement("SELECT * FROM snippets");
            }

            ResultSet rs = stmt.executeQuery();
            boolean found = false;

            while (rs.next()) {
                found = true;
                int id = rs.getInt("id");
                String title = rs.getString("title");
                String tag = rs.getString("tag");
                String lang = rs.getString("language");
                String code = rs.getString("code");
                boolean pinned = rs.getBoolean("pinned");

                JPanel snippetBox = new JPanel(new BorderLayout());
                snippetBox.setBackground(boxColor);
                snippetBox.setBorder(BorderFactory.createTitledBorder(null, title + " (" + lang + ") - #" + tag,
                        0, 0, new Font("SansSerif", Font.BOLD, 14), fgColor));

                JTextArea codeArea = new JTextArea(code);
                codeArea.setEditable(false);
                codeArea.setBackground(new Color(40, 40, 40));
                codeArea.setForeground(new Color(220, 220, 220));
                codeArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
                codeArea.setLineWrap(true);
                codeArea.setWrapStyleWord(true);

                JScrollPane codeScroll = new JScrollPane(codeArea);
                codeScroll.setPreferredSize(new Dimension(700, 150));
                codeScroll.setBorder(null);

                JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                buttonPanel.setBackground(boxColor);

                JButton editButton = new JButton("Edit");
                JButton pinButton = new JButton(pinned ? "Unpin" : "Pin");
                JButton deleteButton = new JButton("Delete");
                JButton copyButton = new JButton("Copy");
                copyButton.addActionListener(e -> {
                    StringSelection selection = new StringSelection(code);
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
                    JOptionPane.showMessageDialog(this, "Code copied to clipboard.");
                });


                editButton.addActionListener(e -> {
                    CodeSnippet snippet = new CodeSnippet(id, title, tag, lang, code, pinned);
                    new EditorPage(snippet);
                    dispose();
                });

                pinButton.addActionListener(e -> {
                    try (Connection pinConn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                         PreparedStatement updateStmt = pinConn.prepareStatement("UPDATE snippets SET pinned = ? WHERE id = ?")) {
                        updateStmt.setBoolean(1, !pinned);
                        updateStmt.setInt(2, id);
                        updateStmt.executeUpdate();
                        JOptionPane.showMessageDialog(this, pinned ? "Snippet unpinned." : "Snippet pinned.");
                        new SnippetLibrary(searchTerm);
                        dispose();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(this, "Error updating pin status.");
                    }
                });

                deleteButton.addActionListener(e -> {
                    int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this snippet?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        try (Connection delConn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                             PreparedStatement delStmt = delConn.prepareStatement("DELETE FROM snippets WHERE id = ?")) {
                            delStmt.setInt(1, id);
                            delStmt.executeUpdate();
                            JOptionPane.showMessageDialog(this, "Snippet deleted.");
                            new SnippetLibrary(searchTerm);
                            dispose();
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(this, "Error deleting snippet.");
                        }
                    }
                });

                buttonPanel.add(copyButton);
                buttonPanel.add(editButton);
                buttonPanel.add(pinButton);
                buttonPanel.add(deleteButton);



                snippetBox.add(codeScroll, BorderLayout.CENTER);
                snippetBox.add(buttonPanel, BorderLayout.SOUTH);
                snippetBox.setMaximumSize(new Dimension(750, 220));
                contentPanel.add(snippetBox);
                contentPanel.add(Box.createVerticalStrut(10));
            }

            if (!found) {
                JLabel noSnippets = new JLabel("No matching snippets found.");
                noSnippets.setForeground(fgColor);
                contentPanel.add(noSnippets);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        add(scrollPane, BorderLayout.CENTER);
        setVisible(true);
    }

    private void showSuggestions(JTextField searchField) {
        String input = searchField.getText().trim();
        suggestionPopup.setVisible(false);
        suggestionPopup.removeAll();

        if (input.isEmpty()) return;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT DISTINCT title FROM snippets WHERE title LIKE ? " +
                             "UNION SELECT DISTINCT tag FROM snippets WHERE tag LIKE ? LIMIT 5")) {
            stmt.setString(1, input + "%");
            stmt.setString(2, input + "%");
            ResultSet rs = stmt.executeQuery();

            boolean found = false;
            while (rs.next()) {
                found = true;
                String suggestion = rs.getString(1);
                JMenuItem item = new JMenuItem(suggestion);
                item.addActionListener(e -> {
                    searchField.setText(suggestion);
                    suggestionPopup.setVisible(false);
                    new SnippetLibrary(suggestion);
                    dispose();
                });
                suggestionPopup.add(item);
            }

            if (found) {
                suggestionPopup.show(searchField, 0, searchField.getHeight());
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
