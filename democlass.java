package com.example.tcep;
import javafx.scene.Parent;
import java.sql.*;

public class democlass {
    public static void main(String args[]) throws ClassNotFoundException {

        try {
            Connection cnSQL;
            Class.forName("com.mysql.cj.jdbc.Driver");
            cnSQL=DriverManager.getConnection("jdbc:mysql://localhost:3306/tcep","root","");
            System.out.println("Connection is successful");
            cnSQL.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}