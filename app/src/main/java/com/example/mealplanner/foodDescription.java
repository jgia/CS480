package com.example.mealplanner;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Locale;

public class foodDescription extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private TextView title;
    private TextView description;
    private TextView instructions;
    private ListView ingredients;
    private int recipeID;
    private ArrayList<String> ingredientList;
    ArrayAdapter<String> adapter;
    private String name, descriptionStr, instructionsStr;
    private TextToSpeech speaker;

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
        ingredients.setOnItemClickListener(this);

        ingredientList = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, ingredientList);
        ingredients.setAdapter(adapter);

        // get text to speech ready
        speaker = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                // status can be either TextToSpeech.SUCCESS or TextToSpeech.ERROR.
                if (i == TextToSpeech.SUCCESS) {
                    // Set preferred language to US english.
                    // If a language is not be available, the result will indicate it.
                    int result = speaker.setLanguage(Locale.US);

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

    }
    //speaker functions needed
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
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        // when list item clicked, get the position of item, set it to edit text widget
        String text = ingredientList.get(position);
        speak(text);
    }
    public String fixInstructions(String in){
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
            sb.append((i + 1) + ". " + instruction + " \n");
        }

        return sb.toString();
    }

    public void saveShoppingCart(ArrayList<String> ingredientsList){
        ArrayList<String> shoppingList = new ArrayList<>();
        try{
            //  connect in stream
            //open stream for reading from file
            InputStream in = openFileInput("shoppingList.txt");
            InputStreamReader isr = new InputStreamReader(in);
            BufferedReader reader = new BufferedReader(isr);
            String str = null;

            while ((str = reader.readLine()) != null) {
                //read existing items into shoppingList str is line
                shoppingList.add(str);
            }

            //close in stream
            reader.close();

            //Add existing items to shoppingList from ingredientsList if not already in there
            for (String ingredient : ingredientsList) {
                if (shoppingList.contains(ingredient)){
                } else{
                    shoppingList.add(ingredient);
                }
            }

            //open out stream
            OutputStreamWriter out = new OutputStreamWriter(openFileOutput("shoppingList.txt", MODE_PRIVATE));
            out.write("");
            for (String item : shoppingList){
                out.write(item + "\n");
            }

            //write all list items to data
        }catch (IOException e){
            e.printStackTrace();
        }
    }



}










