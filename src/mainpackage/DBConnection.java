/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mainpackage;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;


/**
 *
 * @author nhico
 */
public class DBConnection {
    private static final String DB_URL = "jdbc:sqlite:database/pms.db";
    
    public Connection connect(){
        Connection conn = null;
        
        try{
            conn = DriverManager.getConnection(DB_URL);
            System.out.println("Connected");
        }catch(SQLException e){
             e.printStackTrace();
        }
        return conn;
    }
    public void login(String username, String password, JFrame frame) {
    String queryAdmin = "SELECT * FROM users WHERE username = ? AND password = ?";
    String queryLibrarian = "SELECT * FROM Lusers WHERE username = ? AND password = ?";

    try (Connection conn = connect()) {
        PreparedStatement pstmt = conn.prepareStatement(queryAdmin);
        pstmt.setString(1, username);
        pstmt.setString(2, password);
        ResultSet rs = pstmt.executeQuery();

        if (rs.next()) {
            String name = rs.getString("name");
            new Dashboard(name).setVisible(true);
            frame.dispose();
            return;
        }

        pstmt = conn.prepareStatement(queryLibrarian);
        pstmt.setString(1, username);
        pstmt.setString(2, password);
        rs = pstmt.executeQuery();

        if (rs.next()) {
            Session.id = rs.getInt("ID");
            Session.name = rs.getString("Name");
            Session.username = rs.getString("Username");
            Session.password = rs.getString("Password");

            new LibrarianDashboard().setVisible(true);
            frame.dispose();
            return;
        }

        JOptionPane.showMessageDialog(frame, "Invalid username or password.");

    } catch (SQLException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(frame, "Database error: " + e.getMessage());
    }
}

    public void fetchAllBooks(JTable table){
        String query = "SELECT * FROM Bookss";
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Book ID");
        model.addColumn("Title");
        model.addColumn("Author");
        model.addColumn("Category");
        model.addColumn("Quantity");
        model.addColumn("Status");
        
        try(Connection conn = connect();
            Statement stmt = conn.createStatement()){
            
            ResultSet rs = stmt.executeQuery(query);
            
            while(rs.next()){
                int id = rs.getInt("ID");
                String title = rs.getString("Title");
                String author = rs.getString("Author");
                String category = rs.getString("Category");
                String quantity = rs.getString("Quantity");
                String status = rs.getString("Status");
                
                Object[] row = {id, title, author, category, quantity, status};
                
                model.addRow(row);
            }
            table.setModel(model); 
            
        }catch(SQLException e){
             e.printStackTrace();
        }
    }
    
    public void fetchreportBooks(JTable table){
        String query = "SELECT * FROM reporttable";
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Student Name");
        model.addColumn("Book Title");
        model.addColumn("Borrowed Date");
        model.addColumn("Returned Date");
        
        
        try(Connection conn = connect();
            Statement stmt = conn.createStatement()){
            
            ResultSet rs = stmt.executeQuery(query);
            
            while(rs.next()){
                String studentname = rs.getString("StudentName");
                String booktitle = rs.getString("BookTitle");
                String borroweddate = rs.getString("BorrowedDate");
                String returneddate = rs.getString("DateReturned");
                
                Object[] row = {studentname, booktitle, borroweddate, returneddate};
                
                model.addRow(row);
            }
            table.setModel(model); 
            
        }catch(SQLException e){
             e.printStackTrace();
        }
    }
    
    
    
    public void fetchIssueBooks(JTable table){
    String query = "SELECT * FROM issuebook";
    DefaultTableModel model = new DefaultTableModel();
        model.addColumn("ID");
        model.addColumn("Book ID");
        model.addColumn("Title");
        model.addColumn("Author");
        model.addColumn("Category");
        model.addColumn("Student Name");
        model.addColumn("Issue Date");
        model.addColumn("Return Date");
        model.addColumn("Status");
        model.addColumn("Fine");

        try(Connection conn = connect();
            Statement stmt = conn.createStatement()){

            ResultSet rs = stmt.executeQuery(query);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            while(rs.next()){
                int id = rs.getInt("IssueID");
                int bookid = rs.getInt("BookID");
                String title = rs.getString("Title");
                String author = rs.getString("Author");
                String category = rs.getString("Category");
                String studentName = rs.getString("StudentName");
                String issuedate = rs.getString("IssueDate");
                String returndate = rs.getString("ReturnDate");
                String status = rs.getString("Status");
                int fine = rs.getInt("Fine");

                if (returndate == null || returndate.isEmpty()) {
                    LocalDate issue = LocalDate.parse(issuedate, formatter);
                    LocalDate due = issue.plusDays(7); // 7-day loan example
                    LocalDate today = LocalDate.now();

                    if (today.isAfter(due)) {
                        status = "Overdue";
                        fine = (int) ChronoUnit.DAYS.between(due, today) * 10; // 10 pesos/day
                    }
                }

                Object[] row = {id, bookid, title, author, category, studentName, issuedate, returndate, status, fine};
                model.addRow(row);
            }
            table.setModel(model); 

        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    
    public void createProduct(String title, String author, String category, String quantity){
    String query = "INSERT INTO Bookss (Title, Author, Category, Quantity, Status) VALUES (?,?,?,?, 'Available')";
        
        try(Connection conn = connect();
            PreparedStatement pstmt = conn.prepareStatement(query)){
            pstmt.setString(1, title);
            pstmt.setString(2, author);
            pstmt.setString(3, category);
            pstmt.setString(4, quantity);
            
            pstmt.executeUpdate();
            
            System.out.println("Book Added");
        }catch(SQLException e){
            e.printStackTrace();
        }
    }
    public boolean updateBookQuantity(int id, int quantity) {
    String query = "UPDATE Bookss SET Quantity = ?, Status = ? WHERE ID = ?";
    
    try (Connection conn = connect();
         PreparedStatement pstmt = conn.prepareStatement(query)) {

        String status = (quantity == 0) ? "Unavailable" : "Available";

        pstmt.setInt(1, quantity);
        pstmt.setString(2, status);
        pstmt.setInt(3, id);

        int rows = pstmt.executeUpdate();
        return rows > 0;

    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}


    public void deleteProduct(int id){
    String query ="DELETE FROM Bookss WHERE ID = ?";
        
        try(Connection conn = connect();
            PreparedStatement pstmt = conn.prepareStatement(query)){
            pstmt.setInt(1, id);
            
            pstmt.executeUpdate();
            
            System.out.println("Product Deleted!");
        }catch(SQLException e){
            e.printStackTrace();
        }
    }
    public void deleteUser(int id){
    String query ="DELETE FROM Lusers WHERE ID = ?";
        
        try(Connection conn = connect();
            PreparedStatement pstmt = conn.prepareStatement(query)){
            pstmt.setInt(1, id);
            
            pstmt.executeUpdate();
            
            System.out.println("User Deleted!");
        }catch(SQLException e){
            e.printStackTrace();
        }
    }
    public void fetchAllUsers(JTable table){
    String query = "SELECT * FROM Lusers";
    DefaultTableModel model = new DefaultTableModel();
    model.addColumn("ID");
    model.addColumn("Name");
    model.addColumn("Username");
    model.addColumn("Password");
        
        try(Connection conn = connect();
            Statement stmt = conn.createStatement()){
            
            ResultSet rs = stmt.executeQuery(query);
            
            while(rs.next()){
                int id = rs.getInt("Id");
                String name = rs.getString("Name");
                String username = rs.getString("Username");
                String password = rs.getString("Password");
                
                Object[] row = {id, name, username, password};
                
                model.addRow(row);
            }
            table.setModel(model); 
            
        }catch(SQLException e){
             e.printStackTrace();
        }
    }
    public void register(String name, String username, String password, String role){
    String query = "INSERT INTO Lusers (Name, Username, Password,Role) VALUES (?,?,?,?)";
        try(Connection conn = DriverManager.getConnection(DB_URL);
            PreparedStatement pstmt = conn.prepareStatement(query)){
            pstmt.setString(1, name);
            pstmt.setString(2, username);
            pstmt.setString(3, password);
            pstmt.setString(4, role);
            
            pstmt.execute();
            
            System.out.println("Account Created Successfully!");
            
        }catch(SQLException e){
             e.printStackTrace();
        }
    }
    public boolean updateUser(int id, String name, String username, String password) {
    String query = "UPDATE Lusers SET Name = ?, Username = ?, Password = ? WHERE ID = ?";
        try (Connection conn = connect();
            PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, name);
            pstmt.setString(2, username);
            pstmt.setString(3, password);
            pstmt.setInt(4, id);

            int rows = pstmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void updateLibrarianAccount(String newName, String newUsername, String newPassword) {
    String query = "UPDATE Lusers SET name = ?, username = ?, password = ? WHERE id = ?";

    try (Connection conn = connect();
         PreparedStatement pstmt = conn.prepareStatement(query)) {

        pstmt.setString(1, newName);
        pstmt.setString(2, newUsername);
        pstmt.setString(3, newPassword);
        pstmt.setInt(4, Session.id);

        int rows = pstmt.executeUpdate();

        if (rows > 0) {
            JOptionPane.showMessageDialog(null, "Account updated successfully!");

            // Update session info
            Session.name = newName;
            Session.username = newUsername;
            Session.password = newPassword;

        } else {
            JOptionPane.showMessageDialog(null, "Update failed.");
        }

        }catch (SQLException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(null, "Database error: " + e.getMessage());
        }
    }
    public void searchBooks(JTable table, String keyword) {
    DefaultTableModel model = (DefaultTableModel) table.getModel();
    model.setRowCount(0);

        try (Connection conn = connect();
            PreparedStatement pst = conn.prepareStatement(
                "SELECT * FROM Bookss WHERE "
                + "Title LIKE ? OR Author LIKE ? OR Category LIKE ?"
            )) {

            String searchKeyword = "%" + keyword.trim() + "%";
            pst.setString(1, searchKeyword);
            pst.setString(2, searchKeyword);
            pst.setString(3, searchKeyword);

            ResultSet rs = pst.executeQuery();
            boolean found = false;

            while (rs.next()) {
                found = true;
                model.addRow(new Object[]{
                    rs.getInt("ID"),
                    rs.getString("Title"),
                    rs.getString("Author"),
                    rs.getString("Category"),
                    rs.getString("Quantity"),
                    rs.getString("Status")
                });
            }

            if (!found) {
                model.addRow(new Object[]{"", "No Book Found", "", "", ""});
            }

        } catch (SQLException e) {
        e.printStackTrace();
        }
    }
    
    public void searchBookByKeyword(String keyword,JLabel lblTitle,JLabel lblAuthor,JLabel lblCategory,JLabel lblStatus) {
    try (Connection conn = connect();
         PreparedStatement pst = conn.prepareStatement(
            "SELECT Title, Author, Category, Status FROM Bookss "
          + "WHERE ID = ? OR Title LIKE ? OR Author LIKE ? OR Category LIKE ?")) {
        int id = 0;
        try {
            id = Integer.parseInt(keyword);
        } catch (NumberFormatException e) {
            id = 0;
        }

        String likeKeyword = "%" + keyword.trim() + "%";
        pst.setInt(1, id);
        pst.setString(2, likeKeyword);
        pst.setString(3, likeKeyword);
        pst.setString(4, likeKeyword);

        ResultSet rs = pst.executeQuery();

        if (rs.next()) {
            lblTitle.setText(rs.getString("Title"));
            lblAuthor.setText(rs.getString("Author"));
            lblCategory.setText(rs.getString("Category"));
            lblStatus.setText(rs.getString("Status"));
        } else {
            lblTitle.setText("-");
            lblAuthor.setText("-");
            lblCategory.setText("-");
            lblStatus.setText("-");
        }

        rs.close();

    } catch (SQLException e) {
        e.printStackTrace();
    }
}



    public void filterBooksByCategory(JTable table, String category) {
    String query = "SELECT * FROM Bookss WHERE Category LIKE ?";
    DefaultTableModel model = new DefaultTableModel();
    model.addColumn("Book ID");
    model.addColumn("Title");
    model.addColumn("Author");
    model.addColumn("Category");
    model.addColumn("Quantity");
    model.addColumn("Status");

        try (Connection conn = connect();
            PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, "%" + category + "%");
            ResultSet rs = pstmt.executeQuery();

            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                int id = rs.getInt("ID");
                String title = rs.getString("Title");
                String author = rs.getString("Author");
                String cat = rs.getString("Category");
                String qua = rs.getString("Quantity");
                String status = rs.getString("Status");

                model.addRow(new Object[]{id, title, author, cat, qua, status});
            }

            if (!hasData) {
                model.addRow(new Object[]{"No Book Found","", "", "", "", ""});
            }

            table.setModel(model);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void sortBooks(JTable table, String orderType) {
    String query = "SELECT * FROM Bookss";
    
        if (orderType.equals("ASC")) {
            query += " ORDER BY Title ASC";
        } else if (orderType.equals("DESC")) {
            query += " ORDER BY Title DESC";
        } else if (orderType.equals("RND")) {
            query += " ORDER BY RANDOM()";
        }

        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Book ID");
        model.addColumn("Title");
        model.addColumn("Author");
        model.addColumn("Category");
        model.addColumn("Quantity");
        model.addColumn("Status");

        try (Connection conn = connect();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                int id = rs.getInt("ID");
                String title = rs.getString("Title");
                String author = rs.getString("Author");
                String category = rs.getString("Category");
                String quantity = rs.getString("Quantity");
                String status = rs.getString("Status");

                model.addRow(new Object[]{id, title, author, category, quantity, status});
            }

            table.setModel(model);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public boolean isBookExists(String title) {
    boolean exists = false;
    String query = "SELECT * FROM Bookss WHERE Title = ?";
        try (Connection conn = connect();
            PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, title);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
            exists = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return exists;
    }
    public int getTotalBooks() {
    String query = "SELECT COUNT(*) FROM Bookss";
        try (Connection conn = connect();
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getTotalUsers() {
    String query = "SELECT COUNT(*) FROM Lusers";
        try (Connection conn = connect();
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    public int getTotalAvailableBooks() {
    String query = "SELECT COUNT(*) FROM Bookss WHERE Status = 'Available'";
    try (Connection conn = connect();
         PreparedStatement pstmt = conn.prepareStatement(query);
         ResultSet rs = pstmt.executeQuery()) {

        if (rs.next()) {
            return rs.getInt(1);
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }
    return 0;
}
    
    public int getTotalUnavailableBooks() {
    String query = "SELECT COUNT(*) FROM Bookss WHERE Status = 'Unavailable'";
    try (Connection conn = connect();
         PreparedStatement pstmt = conn.prepareStatement(query);
         ResultSet rs = pstmt.executeQuery()) {

        if (rs.next()) {
            return rs.getInt(1);
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }
    return 0;
}
    public int getTotalReservedBooks() {
    String query = "SELECT COUNT(*) FROM ReservedBooks";
    try (Connection conn = connect();
         PreparedStatement pstmt = conn.prepareStatement(query);
         ResultSet rs = pstmt.executeQuery()) {

        if (rs.next()) {
            return rs.getInt(1);
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }
    return 0;
}
    
    public void issueBook(int bookID,String title,String author,String category, String studentName, String returnDate,String librarianName) {
    String issueDate = java.time.LocalDate.now().toString();

    try (Connection conn = connect()) {
        conn.setAutoCommit(false);

        String checkSQL = "SELECT Quantity FROM Bookss WHERE ID = ?";
        try (PreparedStatement pstCheck = conn.prepareStatement(checkSQL)) {
            pstCheck.setInt(1, bookID);
            ResultSet rs = pstCheck.executeQuery();

            if (!rs.next()) {
                JOptionPane.showMessageDialog(null, "❌ Book not found!");
                return;
            }

            int quantity = rs.getInt("Quantity");
            if (quantity <= 0) {
                JOptionPane.showMessageDialog(null, "⚠️ This book is unavailable for issue!");
                return;
            }
        }

        String sqlInsert = "INSERT INTO issuebook "
                         + "(BookID, Title, Author, Category, StudentName, IssueDate, ReturnDate, Status, Fine) "
                         + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst1 = conn.prepareStatement(sqlInsert)) {
            pst1.setInt(1, bookID);
            pst1.setString(2, title);
            pst1.setString(3, author);
            pst1.setString(4, category);
            pst1.setString(5, studentName);
            pst1.setString(6, issueDate);
            pst1.setString(7, returnDate);
            pst1.setString(8, "Issued");
            pst1.setInt(9, 0);
            pst1.executeUpdate();
        }

        String newStatusSQL = "UPDATE Bookss SET Quantity = Quantity - 1, "
                            + "Status = CASE WHEN Quantity - 1 <= 0 THEN 'Unavailable' ELSE 'Available' END "
                            + "WHERE ID = ?";
        try (PreparedStatement pst2 = conn.prepareStatement(newStatusSQL)) {
            pst2.setInt(1, bookID);
            pst2.executeUpdate();
        }

        conn.commit();

        logLibrarianActivity(librarianName, "ISSUE", "Issued '" + title + "' to " + studentName);

        JOptionPane.showMessageDialog(null, "Book issued successfully to " + studentName
                                            + "\nReturn Date: " + returnDate);

    } catch (SQLException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(null, "Error issuing book: " + e.getMessage());
    }
}





    public void searchIssuedBookByID(String issuedID, JLabel lblIssuedID, JLabel lblBookID, JLabel lblTitle, JLabel lblIssuedOn) {
    try (Connection conn = connect()) {
        String sql = "SELECT IssueID, BookID, Title, IssueDate FROM issuebook WHERE IssueID = ?";
        PreparedStatement pst = conn.prepareStatement(sql);
        pst.setInt(1, Integer.parseInt(issuedID));
        ResultSet rs = pst.executeQuery();

        if (rs.next()) {
            lblIssuedID.setText(String.valueOf(rs.getInt("IssueID")));
            lblBookID.setText(String.valueOf(rs.getInt("BookID")));
            lblTitle.setText(rs.getString("Title"));
            lblIssuedOn.setText(rs.getString("IssueDate"));
        } else {
            lblIssuedID.setText("-");
            lblBookID.setText("-");
            lblTitle.setText("-");
            lblIssuedOn.setText("-");
            JOptionPane.showMessageDialog(null, "Issued Book not found!");
        }

    } catch (NumberFormatException ex) {
        JOptionPane.showMessageDialog(null, "Invalid Issued ID format.");
    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(null, "Database error: " + ex.getMessage());
    }
}


    public int calculateFine(String issueDateStr, String returnDateStr) {
    int fine = 0;
    int allowedDays = 7;
    int fineRate = 5;

    try {
        java.time.LocalDate issueDate = java.time.LocalDate.parse(issueDateStr);
        java.time.LocalDate returnDate = java.time.LocalDate.parse(returnDateStr);
        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(issueDate, returnDate);

        if (daysBetween > allowedDays) {
            fine = (int) ((daysBetween - allowedDays) * fineRate);
        }
    } catch (Exception e) {
        e.printStackTrace();
    }

    return fine;
}
    public boolean returnBook(int issueID, String librarianName) {
    try (Connection conn = connect()) {
        conn.setAutoCommit(false);

        String selectSQL = "SELECT BookID, Title, StudentName, IssueDate, ReturnDate FROM issuebook WHERE IssueID = ?";
        int bookID;
        String title, studentName, issueDateStr, returnDateStr;
        try (PreparedStatement pst = conn.prepareStatement(selectSQL)) {
            pst.setInt(1, issueID);
            ResultSet rs = pst.executeQuery();

            if (!rs.next()) {
                JOptionPane.showMessageDialog(null, "❌ Issued Book not found!");
                return false;
            }

            bookID = rs.getInt("BookID");
            title = rs.getString("Title");
            studentName = rs.getString("StudentName");
            issueDateStr = rs.getString("IssueDate");
            returnDateStr = rs.getString("ReturnDate");
        }

        int allowedDays = 7;
        int fineRate = 5;
        java.time.LocalDate issueDate = java.time.LocalDate.parse(issueDateStr);
        java.time.LocalDate returnDate = java.time.LocalDate.parse(returnDateStr);
        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(issueDate, returnDate);
        int fine = (daysBetween > allowedDays) ? (int)((daysBetween - allowedDays) * fineRate) : 0;

        // 3️⃣ Update Bookss
        String updateBookSQL = "UPDATE Bookss SET Quantity = Quantity + 1, Status = 'Available' WHERE ID = ?";
        try (PreparedStatement pstUpdate = conn.prepareStatement(updateBookSQL)) {
            pstUpdate.setInt(1, bookID);
            pstUpdate.executeUpdate();
        }

        String reportSQL = "INSERT INTO reporttable (StudentName, BookTitle, BorrowedDate, DateReturned) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstReport = conn.prepareStatement(reportSQL)) {
            pstReport.setString(1, studentName);
            pstReport.setString(2, title);
            pstReport.setString(3, issueDateStr);
            pstReport.setString(4, returnDateStr);
            pstReport.executeUpdate();
        }

        String deleteSQL = "DELETE FROM issuebook WHERE IssueID = ?";
        try (PreparedStatement pstDel = conn.prepareStatement(deleteSQL)) {
            pstDel.setInt(1, issueID);
            pstDel.executeUpdate();
        }

        conn.commit();

        logLibrarianActivity(librarianName, "RETURN", "Returned '" + title + "' from " + studentName);

        JOptionPane.showMessageDialog(null, "✅ Book returned successfully!\nFine: ₱" + fine);
        return true;

    } catch (SQLException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(null, "❌ Error returning book: " + e.getMessage());
        return false;
    }
}

    public void logLibrarianActivity(String librarianName, String actionType, String details) {
        if (librarianName == null || librarianName.isEmpty()) {
            JOptionPane.showMessageDialog(null, "⚠️ Librarian name is missing! Please log in first.");
            return;
        }

    String sql = "INSERT INTO librarian_activity (LibrarianName, ActionType, Details) VALUES (?, ?, ?)";
        try (Connection conn = connect();
            PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, librarianName);
            pst.setString(2, actionType);
            pst.setString(3, details);
            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error logging activity: " + e.getMessage());
        }
    }

    public void fetchLibrarianActivity(JTable table) {
    String sql = "SELECT * FROM librarian_activity ORDER BY Timestamp DESC";
    DefaultTableModel model = new DefaultTableModel();

        model.addColumn("ID");
        model.addColumn("Librarian");
        model.addColumn("Action");
        model.addColumn("Details");
        model.addColumn("Timestamp");

        try (Connection conn = connect();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {

        while (rs.next()) {
            int id = rs.getInt("ID");
            String name = rs.getString("LibrarianName");
            String action = rs.getString("ActionType");
            String details = rs.getString("Details");
            String timestamp = rs.getString("Timestamp");

            model.addRow(new Object[]{id, name, action, details, timestamp});
        }

            table.setModel(model);

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "❌ Error fetching librarian activities: " + e.getMessage());
        }
    }



   
    public void reserveBook(String name, int bookId, String title, String author, String claimDate) {
    String query = "INSERT INTO ReservedBooks (name, book_id, title, author, reserve_at, claim_date) VALUES (?, ?, ?, ?, datetime('now'), ?)";

    try (Connection conn = connect();
         PreparedStatement pstmt = conn.prepareStatement(query)) {
        pstmt.setString(1, name);
        pstmt.setInt(2, bookId);
        pstmt.setString(3, title);
        pstmt.setString(4, author);
        pstmt.setString(5, claimDate);
        pstmt.executeUpdate();
        System.out.println("Book reserved successfully!");
    } catch (SQLException e) {
        e.printStackTrace();
    }
}


    public int getBookQuantity(int id) {
    String query = "SELECT Quantity FROM Bookss WHERE ID = ?";
        try (Connection conn = connect();
            PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("Quantity");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void updateBookStatus(int id, String status) {
    String query = "UPDATE Bookss SET Status = ? WHERE ID = ?";
        try (Connection conn = connect();
            PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void updateOverdueBooks() {
    String sql = "SELECT id, return_date FROM issued_books WHERE status = 'Issued'";

    try (Connection conn = connect();
         PreparedStatement pstmt = conn.prepareStatement(sql);
         ResultSet rs = pstmt.executeQuery()) {

        while (rs.next()) {
            int id = rs.getInt("id");
            String returnDate = rs.getString("return_date");

            LocalDate today = LocalDate.now();
            LocalDate dueDate = LocalDate.parse(returnDate);

            if (today.isAfter(dueDate)) {
                long daysLate = ChronoUnit.DAYS.between(dueDate, today);
                int fine = (int) (daysLate * 10);

                String update = "UPDATE issued_books SET status = 'Overdue', fine = ? WHERE id = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(update)) {
                    updateStmt.setInt(1, fine);
                    updateStmt.setInt(2, id);
                    updateStmt.executeUpdate();
                }
            }
        }

        System.out.println("✅ Overdue books updated successfully!");

    } catch (SQLException e) {
        System.out.println("Error updating overdue books: " + e.getMessage());
    }
}



    public void fetchAllReservedBooks(JTable table) {
    String query = "SELECT * FROM ReservedBooks";
    DefaultTableModel model = new DefaultTableModel() {

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    model.addColumn("ID");
    model.addColumn("Name");
    model.addColumn("Book ID");
    model.addColumn("Title");
    model.addColumn("Author");
    model.addColumn("Reserved At");
    model.addColumn("Claim Date");

    try (Connection conn = connect();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(query)) {

        while (rs.next()) {
            model.addRow(new Object[]{
                rs.getInt("ID"),
                rs.getString("name"),
                rs.getInt("book_id"),
                rs.getString("title"),
                rs.getString("author"),
                rs.getString("reserve_at"),
                rs.getString("claim_date")
            });
        }

        table.setModel(model);

    } catch (SQLException e) {
        e.printStackTrace();
    }
}
    
public void extendClaimDate(int id, String newClaimDate) {
    String query = "UPDATE ReservedBooks SET claim_date = ? WHERE id = ?";

    try (Connection conn = connect();
         PreparedStatement pstmt = conn.prepareStatement(query)) {

        pstmt.setString(1, newClaimDate);
        pstmt.setInt(2, id);
        pstmt.executeUpdate();

        System.out.println("Claim date updated successfully!");

    } catch (SQLException e) {
        e.printStackTrace();
    }
}
    public void fetchBooksWithReserveStatus(JTable table) {
    String query = "SELECT ID, Title, Author, Quantity, " +
                   "CASE WHEN Quantity = 0 THEN 'Reserved' ELSE Status END AS Status " +
                   "FROM Bookss";

    DefaultTableModel model = new DefaultTableModel();
    model.addColumn("ID");
    model.addColumn("Title");
    model.addColumn("Author");
    model.addColumn("Quantity");
    model.addColumn("Status");

        try (Connection conn = connect();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("ID"),
                    rs.getString("Title"),
                    rs.getString("Author"),
                    rs.getInt("Quantity"),
                    rs.getString("Status")
                });
            }

            table.setModel(model);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}

