// java behind activity_search_meals.xml
//

package com.example.mealplanner;

import androidx.appcompat.app.AppCompatActivity;

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
import android.widget.Toast;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class searchMeals extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private EditText entry;
    private Button search;
    private ListView recipes;
    private String name, duration, input, id;
    private ArrayList<String> recipeList;
    ArrayAdapter<String> adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.browse_meals_search);

        // grab widgets: enterText (EditText), searchButton (AppCompatButton?), recipes (ListView)
        entry = (EditText)findViewById(R.id.enterText);
        search = (Button) findViewById(R.id.searchButton);
        recipes = (ListView) findViewById(R.id.recipes);
        recipes.setOnItemClickListener(this);
        recipeList = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, recipeList);
        recipes.setAdapter(adapter);

        //make sure top menu is there

        //set onclick listener
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

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
            }
        });

    }
    Runnable databaseCall = new Runnable() {
        @Override
        public void run() {
// MUST CHANGE DB LOGIN
            String URL = "jdbc:mysql://webdev.bentley.edu:3306/jgiaquinto";
            String username = "jgiaquinto";
            String password = "3740";
            Statement stmt = null;

            //Note try with resources block
            try  //create connection to database
                    (Connection con = DriverManager.getConnection(
                            URL,
                            username,
                            password)) {
                stmt = con.createStatement();

                //pull the
                input = entry.getText().toString();
// *** NEED TO CHANGE VARNAMES FOR COLS AND TABLE
                // Create a prepared statement for the SELECT query
                String query = "SELECT Name, TotalTime, RecipeID FROM recipe WHERE LOWER(Name) LIKE ?";
                PreparedStatement pstmt = con.prepareStatement(query);

                // Set the input parameter for the prepared statement
                String input = entry.getText().toString().toLowerCase();
                pstmt.setString(1, "%" + input + "%");

                //CHANGE TO PREPARED
                ResultSet result = pstmt.executeQuery();

                while (result.next()){
                    name = result.getString("Name");
                    duration = result.getString("TotalTime");
                    id = result.getString("RecipeID");
                    /*
                    ArrayList<String> line = new ArrayList<String>();
                    line.add(name);
                    line.add(duration);
                    line.add(id);*/
                    String line = String.join(",", name, duration, id);
                    recipeList.add(line);
                }


            } catch (SQLException e) {
                e.printStackTrace();
                Log.d("TAG", e.toString());
            }
        }
    };

    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        //get item specified
        String input = recipeList.get(position);
        String[] words = input.split(",");
        String recipeID = words[2];

        //send intent to start food description with correct item in list
        Intent intent = new Intent(searchMeals.this, foodDescription.class);
        intent.putExtra("recipe_id", recipeID);
        startActivity(intent);
    }

    //code for menu

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


}