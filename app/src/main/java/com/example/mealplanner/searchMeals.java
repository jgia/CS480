// java behind activity_search_meals.xml
//

package com.example.mealplanner;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
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

public class searchMeals extends AppCompatActivity {
    private EditText entry;
    private Button search;
    private ListView recipes;
    private String name, duration, input;
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

                ResultSet result = stmt.executeQuery(
                        "SELECT name, total_time FROM recipes WHERE name LIKE '%Chicken%';");

                while (result.next()){
                    name = result.getString("name");
                    duration = result.getString("total_time");
                    recipeList.add(name + "\t" + duration);
                }
                adapter.notifyDataSetChanged();

            } catch (SQLException e) {
                e.printStackTrace();
            }


        }
    };

    //create function for when button is pressed (onClick) Example --> 09-BrowserView ex w/ button
        // inside function start runnable thread
    //Function must include search to DB for items (thread --> function?)
        // Need query for search based on input (SELECT name, total_time FROM recipes WHERE name LIKE '%Chicken%';) (Maybe add LIMIT 25)

        //
        //This will be the runnable function to be ran in the thread

}