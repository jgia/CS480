// java behind activity_search_meals.xml
//

package com.example.mealplanner;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
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

            }
        });

    }
    Runnable databaseCall = new Runnable() {
        @Override
        public void run() {
// MUST CHANGE DB LOGIN
            String URL = "jdbc:mysql://frodo.bentley.edu:3306/world";
            String username = "harry";
            String password = "harry";
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
                //CHANGE TO PREPARED
                ResultSet result = stmt.executeQuery(
                        "SELECT name, total_time, ID FROM recipes WHERE name LIKE '%' + input + '%';");

                while (result.next()){
                    name = result.getString("name");
                    duration = result.getString("total_time");
                    id = result.getString("ID");
                    ArrayList<String> line = new ArrayList<String>();
                    line.add(name);
                    line.add(duration);
                    line.add(id);
                    recipeList.add(String.valueOf(line));
                }
                adapter.notifyDataSetChanged();

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    };

    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        //get item specified
        String recipeID = recipeList.get(position);
        int recID = Integer.parseInt(recipeID);
        //send intent to start food description with correct item in list
        Intent intent = new Intent(searchMeals.this, foodDescription.class);
        intent.putExtra("recipe_id", recID);
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