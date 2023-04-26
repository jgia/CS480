package com.example.mealplanner;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class viewShoppingList extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private int selectedPosition;
    private ArrayList<String> shoppingList;
    private View lastSelectedIngredientView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shoppinglist);

        File shoppingListFile = new File(getFilesDir(), "ShoppingList.txt");
        if (!shoppingListFile.exists()) {
            try {
                shoppingListFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

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

        //Using these two functions to edit the list view.
        //Idea behind this is that it would be easier to edit the values
        //of the list in the hashmap rather than strip the string every time
        HashMap<String, Integer> shoppingListMap = textFileToHashMap();
        shoppingList = convertHashMapToString(shoppingListMap);
        // Set the adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, shoppingList);
        ingredientsList.setAdapter(adapter);
        removeButton.setOnClickListener(view -> {
            if (!shoppingList.isEmpty() && selectedPosition != -1) {
                removeIngredientFromShoppingList(selectedPosition);
            }
        });

        updateButton.setOnClickListener(view -> {
            if (selectedPosition != -1) {
                showUpdateDialog();
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
        } catch (IOException e) {
            e.printStackTrace();
        }
        return shoppingListMap;
    }

    //Converts the HashMap of ingredients into String to be put through adapter.
    private ArrayList<String> convertHashMapToString(HashMap<String, Integer> shoppingListMap) {
        ArrayList<String> shoppingList = new ArrayList<>();
        if (shoppingListMap == null) {
            return shoppingList;
        }
        String shoppingListString = shoppingListMap.toString();
        shoppingListString = shoppingListString.substring(1, shoppingListString.length() - 1);
        String[] shoppingListArray = shoppingListString.split(", ");
        for (String s : shoppingListArray) {
            String[] keyValue = s.split("=");
            if (keyValue.length == 2) {
                String ingredient = keyValue[0];
                String count = keyValue[1];
                shoppingList.add(ingredient + " x" + count);
            }
        }
        return shoppingList;
    }

    private void removeIngredientFromShoppingList(int selectedPosition) {
        // Get the ingredient and count from the selected item
        String selectedItem = shoppingList.get(selectedPosition);
        String[] parts = selectedItem.split(" x");
        String ingredient = parts[0];
        // Update the shopping list HashMap
        HashMap<String, Integer> shoppingListMap = textFileToHashMap();
        shoppingListMap.remove(ingredient);
        // Update the shopping list file from Hashmap
        try {
            FileOutputStream fos = openFileOutput("ShoppingList.txt", Context.MODE_PRIVATE);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));
            for (Map.Entry<String, Integer> entry : shoppingListMap.entrySet()) {
                writer.write(entry.getKey() + " x" + entry.getValue() + "\n");
            }
            writer.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Update the shopping list adapter
        shoppingList = convertHashMapToString(shoppingListMap);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, shoppingList);
        ListView ingredientsList = findViewById(R.id.ingredients_list);
        ingredientsList.setAdapter(adapter);
    }

    private void showUpdateDialog() {
        // Get the selected ingredient and quantity from ListView
        String selectedIngredient = (String) shoppingList.get(selectedPosition);
        String[] parts = selectedIngredient.split(" x");
        String ingredient = parts[0];
        int quantity = Integer.parseInt(parts[1]);

        // Create an EditText for the user input
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setText(Integer.toString(quantity));

        // Pop dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update Quantity")
                .setMessage("Enter the new quantity for " + ingredient)
                .setView(input)
                .setPositiveButton("Done", (dialog, which) -> {
                    // Update the quantity in the HashMap.
                    int newQuantity = Integer.parseInt(input.getText().toString());
                    HashMap<String, Integer> shoppingListMap = textFileToHashMap();
                    shoppingListMap.put(ingredient, newQuantity);
                    // Update the ListView
                    shoppingList = convertHashMapToString(shoppingListMap);
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, shoppingList);
                    ListView ingredientsList = findViewById(R.id.ingredients_list);
                    ingredientsList.setAdapter(adapter);
                })
                .setNegativeButton("Cancel", null)
                .show();
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
        // If a list item has been selected before, it has a green background with red text to indicate that it is selected
        // When we select a new item, we want to reset colors on the old item (as it is no longer selected)
        if (lastSelectedIngredientView != null && lastSelectedIngredientView != view) {
            TextView lastTextView = lastSelectedIngredientView.findViewById(android.R.id.text1);
            lastTextView.setTextColor(Color.BLACK);
            lastSelectedIngredientView.setBackgroundColor(Color.TRANSPARENT);
        }

        selectedPosition = position;

        // Grab the text and background of the ingredient that was just selected
        TextView textView = view.findViewById(android.R.id.text1);
        Drawable background = view.getBackground();

        // If an item has never been selected before, it's background it technically null
        // However, we'll say the default background has the color Color.TRANSPARENT (it looks the same as a null background)
        int backgroundColor = Color.TRANSPARENT;
        // If the background is not null, it's either TRANSPARENT or GREEN. We'll check that here
        if (background != null) {
            backgroundColor = ((ColorDrawable) background).getColor();
        }

        // If the background color is transparent, this list item was not previously selected.
        // Now that the user has selected it, we can update its colors to indicate it has been selected
        if (backgroundColor == Color.TRANSPARENT) {
            lastSelectedIngredientView = view;
            textView.setTextColor(Color.RED);
            view.setBackgroundColor(Color.GREEN);
        } else { // If the background color was already green, the user wants to unselect it
            selectedPosition = -1; // We can clear selectedPosition and reset the colors of the list item
            textView.setTextColor(Color.BLACK);
            view.setBackgroundColor(Color.TRANSPARENT);
        }
    }
}