package com.example.mealplanner;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class foodDescription extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener, AdapterView.OnItemClickListener {
    private TextView title;
    private TextView description;
    private TextView instructions;
    private ListView ingredients;

    private Button slist_button;
    private int recipeID;
    private ArrayList<String> ingredientList;
    ArrayAdapter<String> adapter;
    private String name, descriptionStr, instructionsStr;
    private String month, day, year, hour, minute;
    private TextToSpeech speaker;
    private final String file = "ShoppingList.txt";

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
        slist_button = findViewById(R.id.slist_button);

        ingredientList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, ingredientList);
        ingredients.setAdapter(adapter);
        ingredients.setOnItemClickListener(this);

        //set up speaker
        speaker = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                // status can be either TextToSpeech.SUCCESS or TextToSpeech.ERROR.
                if (i == TextToSpeech.SUCCESS) {
                    // Set preferred language to US english.
                    // If a language is not be available, the result will indicate it.
                    int result = speaker.setLanguage(Locale.US);
                    //int result = speaker.setLanguage(Locale.ITALY);

                    if (result == TextToSpeech.LANG_MISSING_DATA ||
                            result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        // Language data is missing or the language is not supported.
                        Log.e("tts", "Language is not available.");
                    } else {
                        // The TTS engine has been successfully initialized
                        Log.i("tts", "TTS Initialization successful.");
                    }
                } else {
                    // Initialization failed.
                    Log.e("tts", "Could not initialize TextToSpeech.");
                }
            }
        });

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

        //Save shopping list button call. nt
        slist_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveShoppingCart(ingredientList);
                Toast.makeText(getApplicationContext(), "Ingredients added to shopping list.", Toast.LENGTH_SHORT).show();
            }
        });


    }


    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        // speak the item
        String text = ingredientList.get(position);
        speak(text);
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
        try {
            // Check if file exists.
            File shoppingListTxt = new File(getFilesDir(), file);
            boolean fileExists = shoppingListTxt.exists();
            //Hashmap to store ingredient name, plus how much you need.
            Map<String, Integer> ingredientMap = new HashMap<String, Integer>();

            //Read existing file into map if exists.
            if (fileExists) {
                FileInputStream fis = new FileInputStream(shoppingListTxt);
                BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(" x");
                    String ingredient = parts[0];
                    int count = 1;
                    if (parts.length > 1) {
                        count = Integer.parseInt(parts[1]) + 1;
                    }
                    ingredientMap.put(ingredient, count);
                }
                fis.close();
                reader.close();
            }

            // Add count to ingredients in map with quantity key.
            for (String ingredient : ingredientsList) {
                if (ingredientMap.containsKey(ingredient)) {
                    int count = ingredientMap.get(ingredient) + 1;
                    ingredientMap.put(ingredient, count);
                } else {
                    ingredientMap.put(ingredient, 1);
                }
            }

            // Write map back out
            FileOutputStream fos = new FileOutputStream(shoppingListTxt);
            OutputStreamWriter writer = new OutputStreamWriter(fos);
            for (Map.Entry<String, Integer> entry : ingredientMap.entrySet()) {
                String ingredient = entry.getKey();
                int count = entry.getValue();
                String line = ingredient;
                //Append key count
                if (count > 1) {
                    line += " x" + count;
                }
                writer.write(line + "\n");
            }
            writer.close();
            fos.close();





            Toast.makeText(getApplicationContext(), "Shopping list saved.", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //nt
    }








    public void speak(String output){
        speaker.speak(output, TextToSpeech.QUEUE_FLUSH, null, "Id 0");
    }
    public void onDestroy(){

        // shut down TTS engine
        if(speaker != null){
            speaker.stop();
            speaker.shutdown();
        }
        super.onDestroy();
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