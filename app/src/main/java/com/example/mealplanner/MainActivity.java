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
import android.widget.ListView;

import java.util.ArrayList;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private ArrayList<Meal> meals = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm");

        // LIST
        ListView weeklyMealsList = findViewById(R.id.weeklyMeals);
        weeklyMealsList.setOnItemClickListener(this); // Listener for when user clicks on list elements

        // The weeklyMealsList ListView is based off the the meals ArrayList
        ArrayAdapter<Meal> adapt = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, meals);
        weeklyMealsList.setAdapter(adapt);

        // SQLITE
        try (SQLHelper helper = new SQLHelper(this)) {
            // Drop the existing table in the SQLite database and recreate it
            helper.dropTable();

            // Insert 2 test meals into the SQLite database
            helper.addMeal(new Meal(38, LocalDateTime.parse("04-17-2023 15:23", dateFormat)));
            helper.addMeal(new Meal(39, LocalDateTime.parse("04-18-2023 15:23", dateFormat)));

            // Query the SQLite database to get the list of meals
            meals = helper.getMealList();

            // Update the weeklyMealsList adapter with meals from databases
            adapt.clear();
            adapt.addAll(meals);
            adapt.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        // When a list item is clicked, grab its position
    }
}