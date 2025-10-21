/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mainpackage;

/**
 *
 * @author nhico
 */
public class Session {
    public static int id;
    public static String name;
    public static String username;
    public static String password;
    
    public static void clearSession() {
        id = 0;
        name = null;
        username = null;
        password = null;
    }
}
