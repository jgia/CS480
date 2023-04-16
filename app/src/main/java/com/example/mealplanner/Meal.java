package com.example.mealplanner;

import android.util.Log;

import androidx.annotation.NonNull;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

public class Meal {
    private String recipeName;
    private int mealID;
    private int recipeID;
    private LocalDateTime datetime;

    //accessors and mutators
    public int getMealID() {
        return mealID;
    }

    public void setMealID(int id) {
        this.mealID = id;
    }

    public int getRecipeID() {
        return recipeID;
    }

    public void setRecipeID(int id) {
        this.recipeID = id;
    }

    public LocalDateTime getDateTime() {
        return datetime;
    }

    public void setDateTime(LocalDateTime dt) {
        this.datetime = dt;
    }

    //constructor
    public Meal(int recipeID, LocalDateTime datetime) {
        super();
        this.recipeID = recipeID;
        this.datetime = datetime;
    }


    private String identifyRecipeName() throws InterruptedException {
        // Create a new thread to perform the database operation
        Thread t = new Thread(() -> {
            String URL = "jdbc:mysql://webdev.bentley.edu:3306/jgiaquinto";
            String username = "jgiaquinto";
            String password = "3740";

            Statement stmt = null;
            Connection con = null;

            try { //create connection and statement objects
                con = DriverManager.getConnection(
                        URL,
                        username,
                        password);
                stmt = con.createStatement();
            } catch (SQLException e) {
                Log.e("JDBC", "problem connecting");
            }

            try {
                if (stmt != null) {
                    ResultSet result = stmt.executeQuery("select * from recipe where RecipeID like " + getRecipeID() + ";");

                    //read result set, write data to Log
                    while (result.next()) {
                        String name = result.getString("Name");
                        recipeName = name;
                        Log.e("JDBC", name);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                try { //close connection, may throw checked exception
                    if (con != null)
                        con.close();
                } catch (SQLException e) {
                    Log.e("JDBC", "close connection failed");
                }
            }
        });
        // Start the thread
        t.start();
        t.join();

        return recipeName;
    }

    //toString
    @NonNull
    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd @ HH:mm");

        try {
            return (formatter.format(getDateTime()) + "   -   " + identifyRecipeName());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}