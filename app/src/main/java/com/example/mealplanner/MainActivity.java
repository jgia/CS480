package com.example.mealplanner;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Menu;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //updated
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //pull widgets into vars

        //run DB call to get weekly menu for user (local storage?) (should we do a call as it opens for all of it)

        //need to select menu for specific day (SUN, MON, TUE, etc...)

        //once select day run smaller query for each day based of big query

        // all items of day should then appear

        // buttons next to each item, can view instructions (alertdialog.xml/foodDescription.java), or delete item
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
}