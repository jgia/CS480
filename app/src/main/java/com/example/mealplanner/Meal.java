package com.example.mealplanner;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Meal {
    private int mealID;
    private int recipeID;
    private Date datetime = new Date();

    //accessors and mutators
    public int getMealID() {
        return mealID;
    }
    public void setMealID(int id) {
        this.mealID = id;
    }

    public int getRecipeID() {
        return recipeID;
    }

    public void setRecipeID(int id) {
        this.recipeID = id;
    }

    public Date getDateTime() {
        return datetime;
    }

    public void setDateTime(Date dt) {
        this.datetime = dt;
    }

    //constructor
    public Meal(int recipeID, Date datetime){
        super();
        this.recipeID = recipeID;
        this.datetime = datetime;
    }

    //toString
    @Override
    public String toString() {
        DateFormat dateFormat = new SimpleDateFormat("MM/dd @ HH:mm");
        return (dateFormat.format(getDateTime()) + "   -   " + "Recipe #" + getRecipeID());
    }
}
