// java behind activity_search_meals.xml
//

package com.example.mealplanner;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class searchMeals extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.browse_meals_search);

        // grab widgets: enterText (EditText), searchButton (AppCompatButton?), recipes (ListView)

        //list must be scrollable

        //make sure top menu is there

    }

    //create function for when button is pressed (onClick) Example --> 09-BrowserView ex w/ button
    //Function must include search to DB for items (thread --> function?)

}