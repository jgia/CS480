package com.example.mealplanner;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class viewShoppingList extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.shoppinglist);

        //pull necessary widgets

        //need to make function that pulls all ingredients (cumulative), returns hashmap? of ingredients with amounts (or two lists)

        //need to implement the checkbox.xml

        //need map to use phones location, and find top five supermarkets nearby with google maps (use cloud api)
    }
}