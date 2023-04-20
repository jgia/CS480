package com.example.mealplanner;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class foodDescription extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    private TextView title;
    private TextView description;
    private TextView instructions;
    private ListView ingredients;
    private int recipeID;
    private ArrayList<String> ingredientList;
    ArrayAdapter<String> adapter;
    private String name, descriptionStr, instructionsStr;
    private String month, day, year, hour, minute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.fooddescription);

        // get recipe id from intent object
        Intent intent = getIntent();
        recipeID = intent.getIntExtra("recipe_id", 0);

        //pull required widgets
        title = findViewById(R.id.food_name);
        description = findViewById(R.id.food_description);
        instructions = findViewById(R.id.instructions);
        ingredients = findViewById(R.id.ingredients_list);

        ingredientList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, ingredientList);
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
            instructionsStr = fixInstructions(instructionsStr);
            instructions.setText(instructionsStr);
            Log.e("tag", instructionsStr);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Button meal_button = findViewById(R.id.meal_button);
        meal_button.setOnClickListener(view -> {
            LocalDateTime now = LocalDateTime.now();
            int currentYear = now.getYear();
            int currentMonth = now.getMonthValue();
            int currentDay = now.getDayOfMonth();
            System.out.println(currentYear + " " + currentMonth + " " + currentDay);

            DatePickerDialog dateDialog = new DatePickerDialog(this, this, currentYear, currentMonth - 1, currentDay);
            dateDialog.show();
        });

    }

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
                    ingredientList.add(ingredientName);
                }

            } catch (SQLException e) {
                e.printStackTrace();
                Log.d("TAG", e.toString());
            }
        }
    };

    public String fixInstructions(String in) {
        StringBuilder sb = new StringBuilder();

        // Remove the "c(" and ")" characters from the string
        in = in.substring(2, in.length() - 2);

        // Split the string into separate instructions
        String[] instructions = in.split("\", \"");

        // Loop through the instructions and add them to the string with a + \n
        for (int i = 0; i < instructions.length; i++) {
            String instruction = instructions[i];

            // Remove any extra quotation marks
            instruction = instruction.replace("\"", "");

            // Add the instruction to the string with a + \n
            sb.append(i + 1).append(". ").append(instruction).append(" \n");
        }

        return sb.toString();
    }

    public void saveShoppingCart(ArrayList<String> ingredientsList) {
        ArrayList<String> shoppingList = new ArrayList<>();
        try {
            //  connect in stream
            //open stream for reading from file
            InputStream in = openFileInput("shoppingList.txt");
            InputStreamReader isr = new InputStreamReader(in);
            BufferedReader reader = new BufferedReader(isr);
            String str;

            while ((str = reader.readLine()) != null) {
                //read existing items into shoppingList str is line
                shoppingList.add(str);
            }

            //close in stream
            reader.close();

            //Add existing items to shoppingList from ingredientsList if not already in there
            for (String ingredient : ingredientsList) {
                if (!shoppingList.contains(ingredient)) {
                    shoppingList.add(ingredient);
                }
            }

            //open out stream
            OutputStreamWriter out = new OutputStreamWriter(openFileOutput("shoppingList.txt", MODE_PRIVATE));
            out.write("");
            for (String item : shoppingList) {
                out.write(item + "\n");
            }

            //write all list items to data
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        this.year = String.valueOf(year);
        if ((month / 10) >= 1) {
            this.month = String.valueOf(month + 1); // For whatever reason, month is always 1 less than it should be (e.g., April will be month 3 by default)
        } else {
            this.month = "0" + (month + 1);
        }
        if ((day / 10) >= 1) {
            this.day = String.valueOf(day);
        } else {
            this.day = "0" + day;
        }


        TimePickerDialog timeDialog = new TimePickerDialog(this, this, 00, 00, true);
        timeDialog.show();
    }

    @Override
    public void onTimeSet(TimePicker timePicker, int hour, int minute) {
        if ((hour / 10) >= 1) {
            this.hour = String.valueOf(hour);
        } else {
            this.hour = "0" + hour;
        }
        if ((minute / 10) >= 1) {
            this.minute = String.valueOf(minute);
        } else {
            this.minute = "0" + minute;
        }
        createMeal();
    }

    public void createMeal() {
        try (SQLHelper helper = new SQLHelper(this)) {
            helper.addMeal(new Meal(recipeID, LocalDateTime.parse(month + "-" + day + "-" + year + " " + hour + ":" + minute, MainActivity.dateFormat)));

            // Update the meals ArrayList with the new meal from the database
            MainActivity.meals = helper.getMealList();

            MainActivity.adapt.clear();
            MainActivity.adapt.addAll(MainActivity.meals);
            MainActivity.adapt.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}