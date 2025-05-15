import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;
import java.util.List;

public class EditorPage extends JFrame {
    private static final String DB_URL = "jdbc:mysql://127.0.0.1:3307/sys";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "password";

    private JTextField titleField, tagField, langField;
    private JTextArea codeArea;
    private Integer snippetId;
    private CodeSnippet existingSnippet;

    public EditorPage(CodeSnippet snippet) {
        this.snippetId = (snippet != null) ? snippet.getId() : null;
        this.existingSnippet = snippet;

        setTitle("Code Editor");
        setSize(700, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        Color bgColor = new Color(220, 220, 220);
        Color fgColor = new Color(30, 30, 30);
        Color fieldColor = new Color(220, 220, 220);

        JMenuBar menuBar = new JMenuBar();
        menuBar.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        menuBar.setBackground(new Color(0, 35, 102));
        menuBar.setForeground(fgColor);

        JMenu home = new JMenu("Home");
        JMenu lib = new JMenu("Snippet Library");
        JMenu editor = new JMenu("Editor");
        editor.setEnabled(false);

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
        lib.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                new SnippetLibrary();
                dispose();
            }
        });

        menuBar.add(home);
        menuBar.add(lib);
        menuBar.add(editor);
        setJMenuBar(menuBar);

        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        formPanel.setBackground(bgColor);

        titleField = new JTextField();
        tagField = new JTextField();
        langField = new JTextField();
        codeArea = new JTextArea(10, 40);
        codeArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        codeArea.setBackground(fieldColor);
        codeArea.setForeground(fgColor);
        codeArea.setCaretColor(fgColor);

        JTextField[] fields = { titleField, tagField, langField };
        for (JTextField field : fields) {
            field.setBackground(fieldColor);
            field.setForeground(fgColor);
            field.setCaretColor(fgColor);
        }

        formPanel.add(createLabel("Title:", fgColor));
        formPanel.add(titleField);
        formPanel.add(createLabel("Tag:", fgColor));
        formPanel.add(tagField);
        formPanel.add(createLabel("Language:", fgColor));
        formPanel.add(langField);
        formPanel.add(createLabel("Code:", fgColor));
        formPanel.add(new JScrollPane(codeArea));

        JButton saveButton = new JButton("Save");
        saveButton.setBackground(new Color(70, 130, 180));
        saveButton.setForeground(Color.WHITE);
        saveButton.setFocusPainted(false);
        saveButton.addActionListener(e -> saveSnippet());

        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(bgColor);
        bottomPanel.add(saveButton);

        add(formPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        if (snippet != null) {
            loadSnippet(snippet);
        }

        setVisible(true);
    }

    private JLabel createLabel(String text, Color fgColor) {
        JLabel label = new JLabel(text);
        label.setForeground(fgColor);
        return label;
    }

    private void loadSnippet(CodeSnippet snippet) {
        titleField.setText(snippet.getTitle());
        tagField.setText(snippet.getTag());
        langField.setText(snippet.getLanguage());
        codeArea.setText(snippet.getCode());
    }

    private void saveSnippet() {
        String title = titleField.getText().trim();
        String tag = tagField.getText().trim();
        String lang = langField.getText().trim();
        String code = codeArea.getText().trim();

        if (title.isEmpty() || tag.isEmpty() || lang.isEmpty() || code.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill out all the fields!", "Missing Information", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            if (snippetId == null) {
                // NEW SNIPPET
                PreparedStatement titleCheck = conn.prepareStatement("SELECT COUNT(*) FROM snippets WHERE title = ?");
                titleCheck.setString(1, title);
                ResultSet titleResult = titleCheck.executeQuery();
                titleResult.next();
                if (titleResult.getInt(1) > 0) {
                    JOptionPane.showMessageDialog(this, "The title already exists. Please choose another title.", "Duplicate Title", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                PreparedStatement tagCheck = conn.prepareStatement("SELECT COUNT(*) FROM snippets WHERE tag = ?");
                tagCheck.setString(1, tag);
                ResultSet tagResult = tagCheck.executeQuery();
                tagResult.next();
                if (tagResult.getInt(1) > 0) {
                    JOptionPane.showMessageDialog(this, "The tag already exists. Please choose another tag.", "Duplicate Tag", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                PreparedStatement codeCheck = conn.prepareStatement("SELECT title FROM snippets WHERE code = ?");
                codeCheck.setString(1, code);
                ResultSet codeResult = codeCheck.executeQuery();
                if (codeResult.next()) {
                    JOptionPane.showMessageDialog(this, "This code is already saved under the title: " + codeResult.getString("title"), "Duplicate Code", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                PreparedStatement insert = conn.prepareStatement("INSERT INTO snippets (title, tag, language, code, pinned) VALUES (?, ?, ?, ?, 0)");
                insert.setString(1, title);
                insert.setString(2, tag);
                insert.setString(3, lang);
                insert.setString(4, code);
                insert.executeUpdate();

                JOptionPane.showMessageDialog(this, "Snippet saved!");

            } else {
                // EDIT EXISTING
                String oldTitle = existingSnippet.getTitle();
                String oldTag = existingSnippet.getTag();
                String oldLang = existingSnippet.getLanguage();
                String oldCode = existingSnippet.getCode();

                List<String> updatedFields = new ArrayList<>();

                if (!title.equals(oldTitle)) {
                    PreparedStatement titleCheck = conn.prepareStatement("SELECT COUNT(*) FROM snippets WHERE title = ? AND id != ?");
                    titleCheck.setString(1, title);
                    titleCheck.setInt(2, snippetId);
                    ResultSet titleResult = titleCheck.executeQuery();
                    titleResult.next();
                    if (titleResult.getInt(1) > 0) {
                        JOptionPane.showMessageDialog(this, "Another snippet with this title already exists.", "Duplicate Title", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    updatedFields.add("Title");
                }

                if (!tag.equals(oldTag)) updatedFields.add("Tag");
                if (!lang.equals(oldLang)) updatedFields.add("Language");

                if (!code.equals(oldCode)) {
                    PreparedStatement codeCheck = conn.prepareStatement("SELECT title FROM snippets WHERE code = ? AND id != ?");
                    codeCheck.setString(1, code);
                    codeCheck.setInt(2, snippetId);
                    ResultSet codeResult = codeCheck.executeQuery();
                    if (codeResult.next()) {
                        JOptionPane.showMessageDialog(this, "This code is already saved under the title: " + codeResult.getString("title"), "Duplicate Code", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    updatedFields.add("Code");
                }

                if (updatedFields.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "No changes made.");
                    return;
                }

                PreparedStatement update = conn.prepareStatement("UPDATE snippets SET title = ?, tag = ?, language = ?, code = ? WHERE id = ?");
                update.setString(1, title);
                update.setString(2, tag);
                update.setString(3, lang);
                update.setString(4, code);
                update.setInt(5, snippetId);
                update.executeUpdate();

                JOptionPane.showMessageDialog(this, "Updated fields: " + String.join(", ", updatedFields));
            }

            new HomePage();
            dispose();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error occurred.");
        }
    }
}
