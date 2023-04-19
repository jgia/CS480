package com.example.mealplanner;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class foodDescription extends AppCompatActivity {
    private TextView title;
    private TextView description;
    private TextView instructions;
    private ListView ingredients;
    private int recipeID;
    private ArrayList<String> ingredientList;
    ArrayAdapter<String> adapter;
    private String name, descriptionStr, instructionsStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.fooddescription);

        // get recipe id from intent object
        Intent intent = getIntent();
        recipeID = intent.getIntExtra("recipe_id", 0);

        //pull required widgets
        title = (TextView) findViewById(R.id.food_name);
        description = (TextView) findViewById(R.id.food_description);
        instructions = (TextView) findViewById(R.id.instructions);
        ingredients = (ListView) findViewById(R.id.ingredients_list);

        ingredientList = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, ingredientList);
        ingredients.setAdapter(adapter);

        //thread for getting all widgets data from sql query with recipe ID
        Thread t1 = new Thread(ingredientData);
        t1.start();

        Thread t2 = new Thread(mealData);
        t2.start();

        try {
            t1.join();
            t2.join();
            // The thread has finished executing
            adapter.notifyDataSetChanged();
            // put name into widget
            title.setText(name);
            description.setText(descriptionStr);
           // Toast.makeText(foodDescription.this, instructionsStr, Toast.LENGTH_LONG).show();
         //   Toast.makeText(foodDescription.this, ingredientList.toString(), Toast.LENGTH_LONG).show();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.browse:
                startActivity(new Intent(this, searchMeals.class));
                return true;
            case R.id.shopping:
                startActivity(new Intent(this, viewShoppingList.class));
                return true;
            case R.id.nearby:
                // startActivity(new Intent(this, viewShoppingList.class)); NEED TO ADD XML FOR THIS
                return true;
            case R.id.home:
                // Handle click on hidden item
                startActivity(new Intent(this, MainActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    Runnable mealData = new Runnable() {
        @Override
        public void run() {
            String URL = "jdbc:mysql://webdev.bentley.edu:3306/jgiaquinto";
            String username = "jgiaquinto";
            String password = "3740";
            Statement stmt = null;

            try  //create connection to database
                    (Connection con = DriverManager.getConnection(
                            URL,
                            username,
                            password)) {
                // select description, name, and instructions from recipe table
                String query = "SELECT Name, Description, RecipeInstructions FROM jgiaquinto.recipe WHERE RecipeID=?";

                PreparedStatement pstmt = con.prepareStatement(query);

// Set the input parameter for the prepared statement

                pstmt.setInt(1, recipeID);

// Execute the prepared statement
                ResultSet result = pstmt.executeQuery();

// Process the result set as needed
                while (result.next()) {
                    name = result.getString("Name");
                    descriptionStr = result.getString("Description");
                    instructionsStr = result.getString("RecipeInstructions");
                    System.out.println(instructionsStr);
                }


            } catch (SQLException e) {
                e.printStackTrace();
                Log.d("TAG", e.toString());
            }
            }
    };

    Runnable ingredientData = new Runnable() {
        @Override
        public void run() {
            String URL = "jdbc:mysql://webdev.bentley.edu:3306/jgiaquinto";
            String username = "jgiaquinto";
            String password = "3740";
            Statement stmt = null;

            try  //create connection to database
                    (Connection con = DriverManager.getConnection(
                            URL,
                            username,
                            password)) {
                // using recipeID (str), used prepared statement to make jdbc call
                String query = "SELECT i.Name " +
                        "FROM jgiaquinto.recipe_ingredient ri " +
                        "JOIN jgiaquinto.ingredient i ON ri.IngredientID = i.IngredientID " +
                        "WHERE ri.RecipeID = ?";

                PreparedStatement pstmt = con.prepareStatement(query);
                //Toast.makeText(foodDescription.this, Integer.toString(recipeID), Toast.LENGTH_LONG).show();

// Set the input parameter for the prepared statement

                pstmt.setInt(1, recipeID);

// Execute the prepared statement
                ResultSet result = pstmt.executeQuery();

// Process the result set as needed
                while (result.next()) {
                    String ingredientName = result.getString("Name");

                    // add to array list with concatenated str
                    String line = ingredientName;
                    System.out.println("\n\n\n" + ingredientName + "\n\n\n");
                    ingredientList.add(line);
                }

            } catch (SQLException e) {
                e.printStackTrace();
                Log.d("TAG", e.toString());
            }
        }
    };

}










