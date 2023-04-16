package com.example.mealplanner;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;

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

    public SQLHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //called to create table
    //NB: this is not a lifecycle method because this class is not an Activity
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("SQLiteDemo", "onCreate: " + CREATE_TABLE);
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
    public void dropTable() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);   //ot calling a lifecycle method
    }

    // Add meal to the database
    public void addMeal(Meal item) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm");
        SQLiteDatabase db = this.getWritableDatabase();
        values = new ContentValues();
        values.put(KEY_RECIPE, item.getRecipeID());
        values.put(KEY_DATETIME, formatter.format(item.getDateTime()));
        db.insert(TABLE_NAME, null, values);
        Log.d("SQLiteDemo", item.getRecipeID() + " added");
        db.close();
    }

    // Update meal name in the database
    public void updateMeal(Meal item, Meal newItem) {
        SQLiteDatabase db = this.getWritableDatabase();
        values = new ContentValues();
        values.put(KEY_RECIPE, newItem.getRecipeID());
        db.update(TABLE_NAME, values, KEY_RECIPE + "=?", new String[]{String.valueOf(item.getRecipeID())});
        Log.d("SQLiteDemo", item.getRecipeID() + " updated");
        db.close();
    }

    // Delete meal from database
    public void deleteMeal(int recipeID, String dateStr) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, KEY_RECIPE + "=? AND " + KEY_DATETIME +  "=?", new String[]{String.valueOf(recipeID),dateStr});
        Log.d("SQLiteDemo","item deleted");
        db.close();
    }

    // Query database and return ArrayList of all meals
    public ArrayList<Meal> getMealList() {

        SQLiteDatabase db = this.getWritableDatabase();

        // Get the start date and end date of the current week
        LocalDate today = LocalDate.now();
        LocalDate sundayDate = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)); // Set startDate as Sunday of the current week
        LocalDateTime sundayDateTime = sundayDate.atTime(0, 0); // Set the time to midnight
        LocalDate saturdayDate = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY)); // Set endDate as Saturday of the current week
        LocalDateTime saturdayDateTime = saturdayDate.atTime(23, 59); // Set the time to 23:59

        // The SQLLite query will use a date format of yyyy-MM-dd for for selecting within a date range
        DateTimeFormatter queryDateFormat = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm");
        String stringSundayDate = queryDateFormat.format(sundayDateTime);
        String stringSaturdayDate = queryDateFormat.format(saturdayDateTime);
        // We want to select only meals that are between Sunday and Saturday of the current week
        String selection = KEY_DATETIME + " BETWEEN ? AND ?";
        String[] selectionArgs = {stringSundayDate, stringSaturdayDate};

        Cursor cursor = db.query(TABLE_NAME,
                new String[]{KEY_RECIPE, KEY_DATETIME},
                selection, selectionArgs, null, null, KEY_RECIPE);

        // Write contents of the cursor to mealList
        ArrayList<Meal> mealList = new ArrayList<>();
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm");
        if (cursor != null && cursor.moveToFirst()) { // Ensure the cursor is not null
            do {
                int recipeIndex = cursor.getColumnIndex(KEY_RECIPE);
                int datetimeIndex = cursor.getColumnIndex(KEY_DATETIME);

                if (recipeIndex != -1 && datetimeIndex != -1) { // Resolves the error "Value must be ≥ 0 but `getColumnIndex` can be -1" on cursor.getColumnIndex(KEY_RECIPE)
                    int recipe = cursor.getInt(recipeIndex);
                    String textDate = cursor.getString(datetimeIndex);
                    LocalDateTime date = LocalDateTime.parse(textDate, dateFormat);
                    mealList.add(new Meal(recipe, date));
                }
            } while (cursor.moveToNext());
        }

        if (cursor != null) {
            cursor.close();
        }
        db.close();
        return mealList;
    }
}