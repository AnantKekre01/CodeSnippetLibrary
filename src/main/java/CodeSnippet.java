public class CodeSnippet {
    private int id;
    private String title;
    private String tag;
    private String language;
    private String code;
    private boolean pinned;

    public CodeSnippet(int id, String title, String tag, String language, String code, boolean pinned) {
        this.id = id;
        this.title = title;
        this.tag = tag;
        this.language = language;
        this.code = code;
        this.pinned = pinned;
    }

    // Getters and setters below...
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getTag() { return tag; }
    public String getLanguage() { return language; }
    public String getCode() { return code; }
    public boolean isPinned() { return pinned; }
}
