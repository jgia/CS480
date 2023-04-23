package com.example.mealplanner;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.BounceInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class viewShoppingList extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private int selectedPosition;
    private ArrayList<String> shoppingList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shoppinglist);

        TextView shoppingListview = findViewById(R.id.shopping_list);
        Animation animation = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, -1.0f,
                Animation.RELATIVE_TO_SELF, 2.5f,
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f);
        animation.setDuration(3500);
        animation.setInterpolator(new LinearInterpolator());
        animation.setRepeatCount(Animation.INFINITE);
        animation.setRepeatMode(Animation.RESTART);

        shoppingListview.startAnimation(animation);
        Button updateButton = findViewById(R.id.update_button);
        Button removeButton = findViewById(R.id.remove_button);

        ListView ingredientsList = findViewById(R.id.ingredients_list);
        ingredientsList.setOnItemClickListener(this);
        FloatingActionButton floater = findViewById(R.id.floater_button);

        //Using these two functions to edit the list view.
        //Idea behind this is that it would be easier to edit the values
        //of the list in the hashmap rather than strip the string every time
        HashMap<String, Integer> shoppingListMap = textFileToHashMap();
        shoppingList = convertHashMapToString(shoppingListMap);
        // Set the adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, shoppingList);
        ingredientsList.setAdapter(adapter);
        removeButton.setOnClickListener(view -> {
            if (selectedPosition != -1){
                Toast.makeText(this, Integer.toString(selectedPosition), Toast.LENGTH_SHORT).show();
                shoppingList.remove(selectedPosition);
                adapter.notifyDataSetChanged();
            }
        });

    }
    //Uses the file and strips it into a HashMap.
    private HashMap<String, Integer> textFileToHashMap() {
        HashMap<String, Integer> shoppingListMap = new HashMap<>();
        try {
            // Open the shopping list text file for reading
            File shoppingListFile = new File(getFilesDir(), "ShoppingList.txt");
            if (shoppingListFile.exists()) {
                FileInputStream fis = new FileInputStream(shoppingListFile);
                BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(" x");
                    String ingredient = parts[0];
                    int count = 1;
                    if (parts.length > 1) {
                        count = Integer.parseInt(parts[1]);
                    }
                    shoppingListMap.put(ingredient, count);
                }
                fis.close();
                reader.close();
            }
        } catch(IOException e){
            e.printStackTrace();
        }
        return shoppingListMap;
    }
    //Converts the HashMap of ingredients into String to be put through adapter.
    private ArrayList<String> convertHashMapToString(HashMap<String, Integer> shoppingListMap) {
        String shoppingListString = shoppingListMap.toString();
        shoppingListString = shoppingListString.substring(1, shoppingListString.length() - 1);
        String[] shoppingListArray = shoppingListString.split(", ");
        ArrayList<String> shoppingList = new ArrayList<String>();
        for (String s : shoppingListArray) {
            String[] keyValue = s.split("=");
            String ingredient = keyValue[0];
            String count = keyValue[1];
            shoppingList.add(ingredient);
        }
        return shoppingList;
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


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        // selectedPosition == position
        selectedPosition = position;
        Toast.makeText(this, Integer.toString(selectedPosition), Toast.LENGTH_SHORT).show();
        TextView textView = (TextView) view.findViewById(android.R.id.text1);
        textView.setTextColor(Color.WHITE);
        view.setBackgroundColor(Color.GREEN);
    }
}