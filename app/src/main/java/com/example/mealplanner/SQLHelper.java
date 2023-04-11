package com.example.mealplanner;

//This class is not an Activity. It is a helper class
// used to execute the SQL statements on SQLite.

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;

/** Helper to the database, manages versions and creation */
public class SQLHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "meal.db";
    public static final int DATABASE_VERSION = 4;
    public static final String TABLE_NAME = "MEALS";
    public static final String KEY_RECIPE = "recipeID";
    public static final String KEY_DATETIME = "datetime";
    public static final String KEY_MEAL_ID = "id integer primary key autoincrement";
    public static final String CREATE_TABLE = "CREATE TABLE meals ("
            + KEY_MEAL_ID + "," + KEY_RECIPE + " integer,"
            + KEY_DATETIME + " text);";

    private ContentValues values;
    private ArrayList<Meal> mealList;
    private Cursor cursor;

    public SQLHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //called to create table
    //NB: this is not a lifecycle method because this class is not an Activity
    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = CREATE_TABLE;
        Log.d("SQLiteDemo", "onCreate: " + sql);
        db.execSQL(CREATE_TABLE);
    }

    //called when database version mismatch
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        if (oldVersion >= newVersion) return;

        Log.d("SQLiteDemo", "onUpgrade: Version = " + newVersion);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);   //not calling a lifecycle method
    }

    //drop table
    public void dropTable(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);   //not calling a lifecycle method
    }

    //add meal to database
    public void addMeal(Meal item) {
        SimpleDateFormat formatter = new SimpleDateFormat("YYYY-MM-DD HH:MM:SS.SSS");
        SQLiteDatabase db = this.getWritableDatabase();
        values = new ContentValues();
        values.put(KEY_RECIPE, item.getRecipeID());
        values.put(KEY_DATETIME, formatter.format(item.getDateTime()));
        db.insert(TABLE_NAME, null, values);
        Log.d("SQLiteDemo", item.getRecipeID() + " added");
        db.close();
    }

    //update meal name in database
    public void updateMeal(Meal item, Meal newItem){
        SQLiteDatabase db = this.getWritableDatabase();
        values = new ContentValues();
        values.put(KEY_RECIPE, newItem.getRecipeID());
        db.update(TABLE_NAME, values, KEY_RECIPE + "=?", new String[] {String.valueOf(item.getRecipeID())});
        Log.d("SQLiteDemo", item.getRecipeID() + " updated");
        db.close();
    }

    //delete meal from database
    public void deleteMeal(Meal item){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, KEY_RECIPE + "=?", new String[] {String.valueOf(item.getRecipeID())});
        Log.d("SQLiteDemo", item.getRecipeID() + " deleted");
        db.close();
    }

    //query database and return ArrayList of all animals
    public ArrayList<Meal> getMealList () {

        SQLiteDatabase db = this.getWritableDatabase();
        cursor = db.query(TABLE_NAME,
                new String[] {KEY_RECIPE, KEY_DATETIME},
                null, null, null, null, KEY_RECIPE);

        //write contents of Cursor to list
        mealList = new ArrayList<Meal>();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        while (cursor.moveToNext()) {
            int recipe = cursor.getInt(cursor.getColumnIndex(KEY_RECIPE));
            String textDate = cursor.getString(cursor.getColumnIndex(KEY_DATETIME));
            try {
                Date date = dateFormat.parse(textDate);
                mealList.add(new Meal(recipe, date));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        };
        db.close();
        return mealList;
    }
}