package com.example.mealplanner;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class foodDescription extends AppCompatActivity {
    private TextView title;
    private TextView description;
    private TextView instructions;
    private ListView ingredients;
    private String recipeID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.fooddescription);

        // get recipe id from intent object
        Intent intent = getIntent();
        recipeID = intent.getStringExtra("recipe_id");

        //pull required widgets
        title = (TextView) findViewById(R.id.food_name);
        description = (TextView) findViewById(R.id.food_description);
        instructions = (TextView) findViewById(R.id.instructions);
        ingredients = (ListView) findViewById(R.id.ingredients_list);

        //thread for getting all widgets data from sql query with recipe ID

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
    Runnable foodInfo = new Runnable() {
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
                String query = "SELECT ";

            } catch (SQLException e) {
                e.printStackTrace();
                Log.d("TAG", e.toString());
            }
        }
    };

}










