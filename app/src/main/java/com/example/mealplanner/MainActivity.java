package com.example.mealplanner;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private SQLiteDatabase db;
    private ContentValues values;
    private Cursor cursor;
    private SQLHelper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //updated
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        helper = new SQLHelper(this);

        //create database
        try {
            db = helper.getWritableDatabase();
        } catch(SQLException e) {
            Log.d("SQLiteDemo", "Create database failed");
        }

        //drop existing table and recreate
        helper.dropTable();

        //insert records
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        try {
            helper.addMeal(new Meal(1, dateFormat.parse("2023-04-11 15:23:45.123")));
            helper.addMeal(new Meal(2, dateFormat.parse("2023-04-10 15:23:45.123")));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        ArrayList<Meal> mealList = helper.getMealList();
        for (Meal item : mealList) {
            System.out.println(item.getRecipeID() + " " + item.getDateTime() + "\n" );
        }


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