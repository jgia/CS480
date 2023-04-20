package com.example.mealplanner;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm");
    private String dateStr = "";
    private int recipeID = -1;

    public static ArrayList<Meal> meals = new ArrayList<>();
    public static ArrayAdapter<Meal> adapt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // LIST
        ListView weeklyMealsList = findViewById(R.id.weeklyMeals);
        weeklyMealsList.setOnItemClickListener(this); // Listener for when user clicks on list elements

        // The weeklyMealsList ListView is based off the the meals ArrayList
        adapt = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, meals);
        weeklyMealsList.setAdapter(adapt);

        Button delete_button = findViewById(R.id.delete_button);
        delete_button.setOnClickListener(view -> {
            if (!dateStr.equals("") && recipeID != -1) {
                deleteMeal();
            } else {
                Toast toast = Toast.makeText(getApplicationContext(), "Nothing selected to delete!", Toast.LENGTH_SHORT);
                toast.show();
            }
        });

        Button recipe_button = findViewById(R.id.recipe_button);
        recipe_button.setOnClickListener(view -> {
            if (recipeID != -1) {
                Intent intent = new Intent(MainActivity.this, foodDescription.class);
                intent.putExtra("recipe_id", recipeID);
                startActivity(intent);
            } else {
                Toast toast = Toast.makeText(getApplicationContext(), "You must select a meal to view the recipe!", Toast.LENGTH_SHORT);
                toast.show();
            }
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

    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        dateStr = dateFormat.format(meals.get(position).getDateTime());
        recipeID = meals.get(position).getRecipeID();
    }

    public void deleteMeal() {
        try (SQLHelper helper = new SQLHelper(this)) {

            // Insert 2 test meals into the SQLite database
            helper.deleteMeal(recipeID, dateStr);

            // Query the SQLite database to get the list of meals
            meals = helper.getMealList();

            // Update the weeklyMealsList adapter with meals from databases
            adapt.clear();
            adapt.addAll(meals);
            adapt.notifyDataSetChanged();

            // Clear recipeID and dateStr
            recipeID = -1;
            dateStr = "";
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}