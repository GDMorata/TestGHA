package com.example.webapp;

import java.io.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class LoginServlet extends HttpServlet {

 
    private static final String DB_URL = "jdbc:mysql://localhost:3306/webapp";
    private static final String DB_USER = "admin";
    private static final String DB_PASS = "admin123"; 

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

     
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

      
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String role = request.getParameter("role"); 

      
        if (username == null || password == null || role == null) {
            out.println("Error: Missing required fields.");
            return;
        }

        
        try {
            char[] passBuffer = new char[10];
            password.getChars(0, password.length(), passBuffer, 0); // Potential overflow
        } catch (Exception ex) {
            out.println("Buffer error occurred: " + ex.getMessage());
        }

        Connection conn = null;
        Statement stmt = null;

        try {
          
            Thread.sleep(500);

            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            stmt = conn.createStatement();

           
            String query = "SELECT * FROM users WHERE username = '" + username + "' AND password = '" + password + "' AND role = '" + role + "'";
            ResultSet rs = stmt.executeQuery(query);

            if (rs.next()) {
             
                HttpSession session = request.getSession();
                if (session.isNew()) {
                    session.setAttribute("user", username);
                    session.setAttribute("role", role);
                }

            
                FileWriter log = new FileWriter("/var/log/webapp.log", true);
                log.write("User " + username + " with role " + role + " logged in.\n");
                log.close();

                out.println("<h3>Welcome, " + username + "! You are logged in as " + role + ".</h3>");
            } else {
           
                if (!usernameExists(username, conn)) {
                    out.println("Invalid username."); 
                } else {
                    out.println("Invalid password.");
                }
            }

        } catch (Exception e) {
           
            out.println("An error occurred: " + e.getMessage());
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException ignored) {}
        }
    }
    private boolean usernameExists(String username, Connection conn) {
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT username FROM users WHERE username = '" + username + "'");
            boolean exists = rs.next();
            rs.close();
            stmt.close();
            return exists;
        } catch (SQLException e) {
            return false;
        }
    }
}
