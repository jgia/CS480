package com.example.mealplanner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.NotificationCompat;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.time.format.DateTimeFormatter;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    public final static DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm");
    private String dateStr = "";
    private int recipeID = -1;
    public static ArrayList<Meal> meals = new ArrayList<>();
    public static ArrayAdapter<Meal> adapt;
    public static String NOTIFICATION_CHANNEL_ID = "1001";
    public static String default_notification_id = "default";

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

        // Load the meals that may have been left over from the last time the app was used
        try (SQLHelper helper = new SQLHelper(this)) {
            meals = helper.getMealList();

            if (!meals.isEmpty()) {
                MainActivity.adapt.clear();
                MainActivity.adapt.addAll(MainActivity.meals);
                MainActivity.adapt.notifyDataSetChanged();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

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

        Button reminder_button = findViewById(R.id.reminder_button);
        reminder_button.setOnClickListener(view ->{
            // Create and expand the popup menu for setting reminders
            PopupMenu popupMenu = new PopupMenu(this, view);
            popupMenu.inflate(R.menu.reminder_menu);

            // The popup  menu uses the items from reminder_menu.xml
            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.halfHourReminder:
                        if (!dateStr.equals("") && recipeID != -1) {
                            LocalDateTime currentDateTime = LocalDateTime.now();
                            LocalDateTime mealTime = LocalDateTime.parse(dateStr, dateFormat);
                            LocalDateTime reminderTime = mealTime.minusMinutes(30);
                            if (reminderTime.isAfter(currentDateTime)) {
                                Duration timeUntilReminder = Duration.between(currentDateTime, reminderTime);
                                Toast toast = Toast.makeText(getApplicationContext(), "Reminder set for " + dateFormat.format(reminderTime), Toast.LENGTH_SHORT);
                                scheduleNotification(getNotification("Meal in 30 minutes!"), (int) timeUntilReminder.toMillis());
                                toast.show();
                            }
                            else{
                                Toast toast = Toast.makeText(getApplicationContext(), "The meal is in less than 30 minutes! Reminder not set.", Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        } else {
                            Toast toast = Toast.makeText(getApplicationContext(), "Select a meal before setting a reminder!", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                        return true;
                    case R.id.oneHourReminder:
                        if (!dateStr.equals("") && recipeID != -1) {
                            LocalDateTime currentDateTime = LocalDateTime.now();
                            LocalDateTime mealTime = LocalDateTime.parse(dateStr, dateFormat);
                            LocalDateTime reminderTime = mealTime.minusMinutes(60);
                            if (reminderTime.isAfter(currentDateTime)) {
                                Duration timeUntilReminder = Duration.between(currentDateTime, reminderTime);
                                Toast toast = Toast.makeText(getApplicationContext(), "Reminder set for " + dateFormat.format(reminderTime), Toast.LENGTH_SHORT);
                                scheduleNotification(getNotification("Meal in 1 hour!"), (int) timeUntilReminder.toMillis());
                                toast.show();
                            }
                            else{
                                Toast toast = Toast.makeText(getApplicationContext(), "The meal is in less than 1 hour! Reminder not set.", Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        } else {
                            Toast toast = Toast.makeText(getApplicationContext(), "Select a meal before setting a reminder!", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                        return true;
                    case R.id.threeHourReminder:
                        if (!dateStr.equals("") && recipeID != -1) {
                            LocalDateTime currentDateTime = LocalDateTime.now();
                            LocalDateTime mealTime = LocalDateTime.parse(dateStr, dateFormat);
                            LocalDateTime reminderTime = mealTime.minusMinutes(180);
                            if (reminderTime.isAfter(currentDateTime)) {
                                Duration timeUntilReminder = Duration.between(currentDateTime, reminderTime);
                                Toast toast = Toast.makeText(getApplicationContext(), "Reminder set for " + dateFormat.format(reminderTime), Toast.LENGTH_SHORT);
                                scheduleNotification(getNotification("Meal in 3 hours!"), (int) timeUntilReminder.toMillis());
                                toast.show();
                            }
                            else{
                                Toast toast = Toast.makeText(getApplicationContext(), "The meal is in less than 3 hours! Reminder not set.", Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        } else {
                            Toast toast = Toast.makeText(getApplicationContext(), "Select a meal before setting a reminder!", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                        return true;
                    case R.id.sixHourReminder:
                        if (!dateStr.equals("") && recipeID != -1) {
                            LocalDateTime currentDateTime = LocalDateTime.now();
                            LocalDateTime mealTime = LocalDateTime.parse(dateStr, dateFormat);
                            LocalDateTime reminderTime = mealTime.minusMinutes(360);
                            if (reminderTime.isAfter(currentDateTime)) {
                                Duration timeUntilReminder = Duration.between(currentDateTime, reminderTime);
                                Toast toast = Toast.makeText(getApplicationContext(), "Reminder set for " + dateFormat.format(reminderTime), Toast.LENGTH_SHORT);
                                scheduleNotification(getNotification("Meal in 6 hours!"), (int) timeUntilReminder.toMillis());
                                toast.show();
                            }
                            else{
                                Toast toast = Toast.makeText(getApplicationContext(), "The meal is in less than 6 hours! Reminder not set.", Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        } else {
                            Toast toast = Toast.makeText(getApplicationContext(), "Select a meal before setting a reminder!", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                        return true;
                    default:
                        Toast toast = Toast.makeText(getApplicationContext(), "Error: menu selection failed. Please try again!", Toast.LENGTH_SHORT);
                        toast.show();
                        return false;
                }
            });
            popupMenu.show();
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
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        dateStr = dateFormat.format(meals.get(position).getDateTime());
        recipeID = meals.get(position).getRecipeID();
        // Set the new selected item position and change its styling
        TextView textView = (TextView) v.findViewById(android.R.id.text1);
        textView.setTextColor(Color.WHITE);
        v.setBackgroundColor(Color.BLUE);
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

    // The following two methods were created based on code from the following KDTechs YouTube video: https://www.youtube.com/watch?v=Ijv0vcxNk78

    // scheduleNotification Schedules a notification to occur in a specific amount of milliseconds (delay) using the MyNotificationPublisher class
    private void scheduleNotification(Notification notification, int delay){
        Intent notificationIntent = new Intent(this, MyNotificationPublisher.class);
        notificationIntent.putExtra(MyNotificationPublisher.NOTIFICATIONID,1);
        notificationIntent.putExtra(MyNotificationPublisher.NOTIFICATION, notification);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        long futureMillis = SystemClock.elapsedRealtime()+delay;
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        assert alarmManager != null;
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureMillis, pendingIntent);
    }

    private Notification getNotification(String content){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, default_notification_id);
        builder.setContentText(content);
        builder.setSmallIcon(R.drawable.ic_launcher_foreground);
        builder.setAutoCancel(true);
        builder.setChannelId(NOTIFICATION_CHANNEL_ID);

        return builder.build();
    }
}