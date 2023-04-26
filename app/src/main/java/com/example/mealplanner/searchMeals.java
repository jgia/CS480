package com.example.mealplanner;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class searchMeals extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private EditText entry;
    private ArrayList<String> recipeList;
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.browse_meals_search);

        // grab widgets
        entry = (EditText) findViewById(R.id.enterText);
        Button search = (Button) findViewById(R.id.searchButton);
        ListView recipes = (ListView) findViewById(R.id.recipes);
        recipes.setOnItemClickListener(this);
        recipeList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, recipeList);
        recipes.setAdapter(adapter);

        //set onclick listener
        search.setOnClickListener(view -> {

            //Run thread
            Thread t = new Thread(databaseCall);
            t.start();

            try {
                t.join();
                // The thread has finished executing
                adapter.notifyDataSetChanged();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

    }

    Runnable databaseCall = new Runnable() {
        @Override
        public void run() {
            String URL = "jdbc:mysql://webdev.bentley.edu:3306/jgiaquinto";
            String username = "jgiaquinto";
            String password = "3740";

            // Note: try with resources block
            try (Connection con = DriverManager.getConnection( // Create connection to database
                    URL,
                    username,
                    password)) {
                // Create a prepared statement for the SELECT query
                String query = "SELECT Name, TotalTime, RecipeID FROM recipe WHERE LOWER(Name) LIKE ?";
                PreparedStatement pstmt = con.prepareStatement(query);

                // Set the input parameter for the prepared statement
                String input = entry.getText().toString().toLowerCase();
                pstmt.setString(1, "%" + input + "%");
                ResultSet result = pstmt.executeQuery();

                while (result.next()) {
                    String name = result.getString("Name");
                    String duration = result.getString("TotalTime");
                    String id = result.getString("RecipeID");
                    String line = (name + " | " + id + " | " + "\n\uD83D\uDD51 " + duration);
                    recipeList.add(line);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                Log.d("TAG", e.toString());
            }
        }
    };

    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        // Get item specified
        String input = recipeList.get(position);
        String[] words = input.split(" \\| ");
        int recipeID = Integer.parseInt(words[1]); // Get just the recipeID

        // Send intent to start food description with correct item in list
        Intent intent = new Intent(searchMeals.this, foodDescription.class);
        intent.putExtra("recipe_id", recipeID);
        startActivity(intent);
    }

    // Code for top menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
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
                startActivity(new Intent(this, nearbyStores.class));
                return true;
            case R.id.home:
                // Handle click on hidden item
                startActivity(new Intent(this, MainActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}