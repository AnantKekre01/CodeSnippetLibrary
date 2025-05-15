import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.sql.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
public class HomePage extends JFrame {
    private static final String DB_URL = "jdbc:mysql://127.0.0.1:3307/sys";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "password";

    public HomePage() {
        setTitle("Code Snippet Library - Home");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Colors
        Color bgColor = new Color(220, 220, 220);
        Color fgColor = new Color(30,30,30);
        Color boxColor = new Color(220, 220, 220);

        // Menu bar
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        menuBar.setBackground(new Color(0,35,102));
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


        lib.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                new SnippetLibrary();
                dispose();
            }
        });
        editor.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                new EditorPage(null);
                dispose();
            }
        });

        home.setEnabled(false);

        menuBar.add(home);
        menuBar.add(lib);
        menuBar.add(editor);
        setJMenuBar(menuBar);

        // Top panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(bgColor);
        topPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel welcomeLabel = new JLabel("Welcome to your Code Snippet Library");
        welcomeLabel.setForeground(new Color(30, 30, 30));
        welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        topPanel.add(welcomeLabel, BorderLayout.WEST);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.setBackground(bgColor);


        JButton searchButton = new JButton("Search");
        JTextField searchField = new JTextField(20);
        JPopupMenu suggestionPopup = new JPopupMenu();

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                showSuggestions();
            }

            public void removeUpdate(DocumentEvent e) {
                showSuggestions();
            }

            public void changedUpdate(DocumentEvent e) {
                showSuggestions();
            }

            private void showSuggestions() {
                String input = searchField.getText().trim();
                suggestionPopup.setVisible(false);
                suggestionPopup.removeAll();

                if (input.isEmpty()) return;

                try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                     PreparedStatement stmt = conn.prepareStatement(
                             "SELECT 'title' AS type, title AS value FROM snippets WHERE title LIKE ? " +
                                     "UNION " +
                                     "SELECT 'tag' AS type, tag AS value FROM snippets WHERE tag LIKE ? LIMIT 5"
                     )) {

                    stmt.setString(1, input + "%");
                    stmt.setString(2, input + "%");

                    ResultSet rs = stmt.executeQuery();

                    boolean found = false;
                    while (rs.next()) {
                        found = true;
                        String type = rs.getString("type");
                        String value = rs.getString("value");

                        JMenuItem item = new JMenuItem(value);
                        item.addActionListener(e -> {
                            searchField.setText(value);
                            suggestionPopup.setVisible(false);
                            new SnippetLibrary(value); // search both tag and title
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

        });

        searchButton.addActionListener(e -> {
            String searchText = searchField.getText().trim();
            if (!searchText.isEmpty()) {
                new SnippetLibrary(searchText); // Open with search term
                dispose();
            }
        });

        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        topPanel.add(searchPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Main content panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(bgColor);
        JLabel pinnedHeading = new JLabel("Pinned Codes");
        pinnedHeading.setFont(new Font("SansSerif", Font.BOLD, 18));
        pinnedHeading.setForeground(new Color(30,30,30));
        pinnedHeading.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(pinnedHeading);

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBackground(bgColor);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM snippets WHERE pinned = 1");
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
                JButton copyButton = new JButton("Copy");

                copyButton.addActionListener(e -> {
                    StringSelection selection = new StringSelection(codeArea.getText());
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    clipboard.setContents(selection, selection);
                    JOptionPane.showMessageDialog(this, "Code copied to clipboard.");
                });

                codeArea.setBackground(new Color(30, 30, 30));
                codeArea.setForeground(new Color(220, 220, 220));
                codeArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
                codeArea.setLineWrap(true);
                codeArea.setWrapStyleWord(true);

                JScrollPane codeScroll = new JScrollPane(codeArea);
                codeScroll.setPreferredSize(new Dimension(700, 150));
                codeScroll.setBorder(null);

                // Buttons panel
                JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                buttonPanel.setBackground(boxColor);

                JButton editButton = new JButton("Edit");
                JButton pinButton = new JButton("Unpin");
                JButton deleteButton = new JButton("Delete");

                editButton.addActionListener(e -> {
                    CodeSnippet snippet = new CodeSnippet(id, title, tag, lang, code, pinned);
                    new EditorPage(snippet);
                    dispose();
                });

                pinButton.addActionListener(e -> {
                    try (Connection pinConn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                         PreparedStatement updateStmt = pinConn.prepareStatement("UPDATE snippets SET pinned = ? WHERE id = ?")) {
                        updateStmt.setBoolean(1, false);
                        updateStmt.setInt(2, id);
                        updateStmt.executeUpdate();
                        JOptionPane.showMessageDialog(this, "Snippet unpinned.");
                        new HomePage();
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
                            new HomePage();
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
                JLabel noPinned = new JLabel("No pinned snippets found.");
                noPinned.setForeground(fgColor);
                contentPanel.add(noPinned);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        add(scrollPane, BorderLayout.CENTER);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(HomePage::new);
    }
}

