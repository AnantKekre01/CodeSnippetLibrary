# Code Snippet Organizer App

This is a Java Swing-based desktop application I built to help me organize and manage code snippets more efficiently. It connects to a MySQL database using JDBC and provides a clean GUI to add, edit, delete, search, and pin code snippets. Think of it as a personal code snippet library with a built-in search and editor.

---

##  Features

### Home Page
- Displays a **menu bar** with navigation options.
- Shows all **pinned snippets** on the left panel.
- Includes a **search bar** on the right to search by **title**.
- **Live suggestions** appear as you type (searches both titles and tags).
- Clicking a suggestion opens the full snippet in the Snippet Library.
- Each pinned snippet comes with:
  -  Edit button
  -  Copy button
  -  Unpin button

---

###  Editor Page
- Accessed via the **"Editor"** button in the menu.
- Lets you add a new snippet or edit an existing one.
- Form fields:
  - **Title**
  - **Tag**
  - **Language**
  - **Code** (multi-line text area)
- Data is stored in a MySQL database.
- If you're editing, the form auto-fills with the existing values.

---

###  Snippet Library
- Displays all snippets from the database in a clean `JTable`.
- Each row shows:
  - Snippet title
  - Language
  - Tag
  - Action buttons:
    -  Copy
    -  Edit
    -  Delete
- Clicking a snippet lets you view the full code.

---

###  Database
- Connected using **JDBC** to **MySQL**.
- Table structure:
  - `id` (INT, Primary Key)
  - `title` (VARCHAR)
  - `tag` (VARCHAR)
  - `language` (VARCHAR)
  - `code` (TEXT)
  - `pinned` (BOOLEAN or TINYINT)

---

## 🛠 Technologies Used
- Java Swing (GUI)
- JDBC (Database connectivity)
- MySQL (Data storage)
- IntelliJ IDEA (IDE)

---

##  Why I Built This
I found myself reusing bits of code across multiple projects and wanted a lightweight app where I could organize and retrieve snippets easily. This app helps me stay organized, and the GUI makes it easy to manage without diving into files or cloud-based solutions every time.

---

##  Future Improvements
- Tag-based filtering
- Export/import snippets as files
- Dark mode UI
- Backup/restore database
- Syntax highlighting (maybe with a third-party library)

---

##  Screenshots (Coming Soon)

---

##  How to Run
1. Clone the repo:
   ```bash
   git clone https://github.com/AnantKekre01/CodeSnippetLibrary.git
