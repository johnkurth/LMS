/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mainpackage;
import java.sql.*;
import javax.swing.JFrame;
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
        model.addColumn("Status");
        
        try(Connection conn = connect();
            Statement stmt = conn.createStatement()){
            
            ResultSet rs = stmt.executeQuery(query);
            
            while(rs.next()){
                int id = rs.getInt("ID");
                String title = rs.getString("Title");
                String author = rs.getString("Author");
                String category = rs.getString("Category");
                String status = rs.getString("Status");
                
                Object[] row = {id, title, author, category, status};
                
                model.addRow(row);
            }
            table.setModel(model); 
            
        }catch(SQLException e){
             e.printStackTrace();
        }
    }
    
    public void createProduct(String title, String author, String category){
        String query = "INSERT INTO Bookss (Title, Author, Category, Status) VALUES (?,?,?, 'Available')";
        
        try(Connection conn = connect();
            PreparedStatement pstmt = conn.prepareStatement(query)){
            pstmt.setString(1, title);
            pstmt.setString(2, author);
            pstmt.setString(3, category);
            
            pstmt.executeUpdate();
            
            System.out.println("Book Added");
        }catch(SQLException e){
            e.printStackTrace();
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

    } catch (SQLException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(null, "Database error: " + e.getMessage());
    }
}

}
