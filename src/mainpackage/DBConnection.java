/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mainpackage;
import java.sql.*;
import javax.swing.JFrame;
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
    public void login(String username, String password, JFrame frame){
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        
        try(Connection conn = connect();
            PreparedStatement pstmt = conn.prepareStatement(query)){
            
            pstmt.setString(1,username);
            pstmt.setString(2,password);
            
            ResultSet rs = pstmt.executeQuery();
            
            if(rs.next()){
                String role = rs.getString("role");
                String name = rs.getString("name");
                if(role.equalsIgnoreCase("admin")){
                    new Dashboard(name).setVisible(true);
                    frame.dispose();
                }else{
                    new LibrarianDashboard(name).setVisible(true);
                    frame.dispose();
                }
            }
            
        }catch(SQLException e){
             e.printStackTrace();
            
        }
    }
    public void fetchAllBooks(JTable table){
        String query = "SELECT * FROM books";
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Book ID");
        model.addColumn("Title");
        model.addColumn("Author");
        model.addColumn("Publisher");
        model.addColumn("Status");
        
        try(Connection conn = connect();
            Statement stmt = conn.createStatement()){
            
            ResultSet rs = stmt.executeQuery(query);
            
            while(rs.next()){
                int id = rs.getInt("Book id");
                String title = rs.getString("Title");
                String author = rs.getString("Author");
                String publisher = rs.getString("Publisher");
                String status = rs.getString("Status");
                
                Object[] row = {id, title, author, publisher, status};
                
                model.addRow(row);
            }
            table.setModel(model); 
            
        }catch(SQLException e){
             e.printStackTrace();
        }
    }
    
    public void createProduct(String title, String author, String publisher){
        String query = "INSERT INTO books (Title, Author, Publisher, Status) VALUES (?,?,?, 'Available')";
        
        try(Connection conn = connect();
            PreparedStatement pstmt = conn.prepareStatement(query)){
            pstmt.setString(1, title);
            pstmt.setString(2, author);
            pstmt.setString(3, publisher);
            
            pstmt.executeUpdate();
            
            System.out.println("Book Added");
        }catch(SQLException e){
            e.printStackTrace();
        }
    }
    public void deleteProduct(int id){
        String query ="DELETE FROM books WHERE \"Book id\" = ?";
        
        try(Connection conn = connect();
            PreparedStatement pstmt = conn.prepareStatement(query)){
            pstmt.setInt(1, id);
            
            pstmt.executeUpdate();
            
            System.out.println("Product Deleted!");
        }catch(SQLException e){
            e.printStackTrace();
        }
    }
    
    public void fetchAllUsers(JTable table){
        String query = "SELECT * FROM users";
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("User_ID");
        model.addColumn("Name");
        model.addColumn("Username");
        model.addColumn("Password");
        
        try(Connection conn = connect();
            Statement stmt = conn.createStatement()){
            
            ResultSet rs = stmt.executeQuery(query);
            
            while(rs.next()){
                int id = rs.getInt("User_Id");
                String name = rs.getString("name");
                String username = rs.getString("username");
                String password = rs.getString("password");
                
                Object[] row = {id, name, username, password};
                
                model.addRow(row);
            }
            table.setModel(model); 
            
        }catch(SQLException e){
             e.printStackTrace();
        }
    }
    
    public void issueBook(int bookId) {
    String query = "UPDATE books SET status = 'Unavailable' WHERE \"Book id\" = ?";
    try (Connection conn = connect();
         PreparedStatement pstmt = conn.prepareStatement(query)) {
        pstmt.setInt(1, bookId);
        pstmt.executeUpdate();
        System.out.println("Book marked as Unavailable (Issued).");
    } catch (SQLException e) {
        e.printStackTrace();
    }
}
    public void returnBook(int bookId) {
    String query = "UPDATE books SET status = 'Available' WHERE \"Book id\" = ?";
    try (Connection conn = connect();
         PreparedStatement pstmt = conn.prepareStatement(query)) {
        pstmt.setInt(1, bookId);
        pstmt.executeUpdate();
        System.out.println("Book marked as Available (Returned).");
    } catch (SQLException e) {
        e.printStackTrace();
    }
}
}
