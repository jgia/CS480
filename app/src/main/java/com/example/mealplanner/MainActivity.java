package com.example.mealplanner;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
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
    private String dateStr = ""; // Default date is blank
    private int recipeID = -1; // Default recipeID is -1 (an ID not possible in the database)

    private View lastSelectedView; // The last meal item a user selected in the list
    public static ArrayList<Meal> meals = new ArrayList<>();
    public static ArrayAdapter<Meal> adapt;
    public static String NOTIFICATION_CHANNEL_ID = "1001";
    public static String default_notification_id = "default";

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    scheduleNotification(getNotification("Notifications enabled for Android 13"), 0); // API 33 requires an extra permission for notifications
                } else {
                    // Warn the user if notifications are disabled. Reminders can't be pushed if notifications are disabled
                    Toast toast = Toast.makeText(getApplicationContext(), "Warning: notifications disabled! Reminders will not work until notifications enabled.", Toast.LENGTH_SHORT);
                    toast.show();
                }
            });

    @SuppressLint("NonConstantResourceId")
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
                deleteMeal(); // Delete is based on dateStr and recipeID. If for some reason a user has two of the exact same recipes scheduled at the same time, it will delete both
            } else {
                Toast toast = Toast.makeText(getApplicationContext(), "Nothing selected to delete!", Toast.LENGTH_SHORT);
                toast.show();
            }
        });

        Button recipe_button = findViewById(R.id.recipe_button);
        recipe_button.setOnClickListener(view -> {
            if (recipeID != -1) {
                Intent intent = new Intent(MainActivity.this, foodDescription.class);
                intent.putExtra("recipe_id", recipeID); // Lookup the food description based on recipeID
                startActivity(intent);
            } else {
                Toast toast = Toast.makeText(getApplicationContext(), "You must select a meal to view the recipe!", Toast.LENGTH_SHORT);
                toast.show();
            }
        });

        Button reminder_button = findViewById(R.id.reminder_button);
        reminder_button.setOnClickListener(view -> {
            // Create and expand the popup menu for setting reminders
            PopupMenu popupMenu = new PopupMenu(this, view);
            popupMenu.inflate(R.menu.reminder_menu);

            // The popup menu uses the items from reminder_menu.xml
            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.halfHourReminder:
                        if (!dateStr.equals("") && recipeID != -1) {
                            LocalDateTime currentDateTime = LocalDateTime.now(); // Get the current date and time
                            LocalDateTime mealTime = LocalDateTime.parse(dateStr, dateFormat); // Get the date and time of the meal
                            LocalDateTime reminderTime = mealTime.minusMinutes(30); // Get the date and time of the reminder (30 minutes before the meal in this case)
                            if (reminderTime.isAfter(currentDateTime)) { // If the user is trying to set a reminder for a time that has already passed, tell them they cannot
                                Duration timeUntilReminder = Duration.between(currentDateTime, reminderTime); // Otherwise, get the time until the reminder (duration between now and the reminder)
                                checkNotificationPermissions(); // Check that it's possible to show notifications
                                Toast toast = Toast.makeText(getApplicationContext(), "Reminder set for " + dateFormat.format(reminderTime), Toast.LENGTH_SHORT); // Tell the user when the reminder is set for
                                scheduleNotification(getNotification("Meal in 30 minutes!"), (int) timeUntilReminder.toMillis()); // Set the reminder to be sent at the reminder time (the delay / duration between now and then is in milliseconds)
                                toast.show();
                            } else {
                                Toast toast = Toast.makeText(getApplicationContext(), "The meal is in less than 30 minutes! Reminder not set.", Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        } else {
                            Toast toast = Toast.makeText(getApplicationContext(), "Select a meal before setting a reminder!", Toast.LENGTH_SHORT); // The user must select a meal before scheduling a reminder
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
                                checkNotificationPermissions();
                                Toast toast = Toast.makeText(getApplicationContext(), "Reminder set for " + dateFormat.format(reminderTime), Toast.LENGTH_SHORT);
                                scheduleNotification(getNotification("Meal in 1 hour!"), (int) timeUntilReminder.toMillis());
                                toast.show();
                            } else {
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
                                checkNotificationPermissions();
                                Toast toast = Toast.makeText(getApplicationContext(), "Reminder set for " + dateFormat.format(reminderTime), Toast.LENGTH_SHORT);
                                scheduleNotification(getNotification("Meal in 3 hours!"), (int) timeUntilReminder.toMillis());
                                toast.show();
                            } else {
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
                                checkNotificationPermissions();
                                Toast toast = Toast.makeText(getApplicationContext(), "Reminder set for " + dateFormat.format(reminderTime), Toast.LENGTH_SHORT);
                                scheduleNotification(getNotification("Meal in 6 hours!"), (int) timeUntilReminder.toMillis());
                                toast.show();
                            } else {
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

    // Menu containing "Browse Meals", "Shopping List", "Nearby Stores", and "Home"
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        if (getSupportActionBar() != null && getSupportActionBar().getTitle() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false); // Remove the app title from the menu
        }
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

    // onItemClick deals with the list of meals
    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        // If a list item has been selected before, it has a blue background with white text to indicate that it is selected
        // When we select a new item, we want to reset colors on the old item (as it is no longer selected)
        if (lastSelectedView != null && lastSelectedView != v) {
            TextView lastTextView = lastSelectedView.findViewById(android.R.id.text1);
            lastTextView.setTextColor(Color.BLACK);
            lastSelectedView.setBackgroundColor(Color.TRANSPARENT);
        }

        // Grab the date and recipeID of the meal that was just selected
        dateStr = dateFormat.format(meals.get(position).getDateTime());
        recipeID = meals.get(position).getRecipeID();

        // Grab the text and background of the meal that was just selected
        TextView textView = v.findViewById(android.R.id.text1);
        Drawable background = v.getBackground();

        // If an item has never been selected before, it's background it technically null
        // However, we'll say the default background has the color Color.TRANSPARENT (it looks the same as a null background)
        int backgroundColor = Color.TRANSPARENT;
        // If the background is not null, it's either TRANSPARENT or BLUE. We'll check that here
        if (background != null) {
            backgroundColor = ((ColorDrawable) background).getColor();
        }

        // If the background color is transparent, this list item was not previously selected.
        // Now that the user has selected it, we can update its colors to indicate it has been selected
        if (backgroundColor == Color.TRANSPARENT) {
            lastSelectedView = v;
            textView.setTextColor(Color.WHITE);
            v.setBackgroundColor(Color.BLUE);
        } else { // If the background color was already blue, the user wants to unselect it
            recipeID = -1; // We can clear recipeID and dateStr and reset the colors of the list item
            dateStr = "";
            textView.setTextColor(Color.BLACK);
            v.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    public void deleteMeal() {
        try (SQLHelper helper = new SQLHelper(this)) {
            // Delete the meal based on recipeID and dateStr
            helper.deleteMeal(recipeID, dateStr);

            // Query the SQLite database to get the new list of meals
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

    // Check that the application has the permission to send notifications in API 33
    private void checkNotificationPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // API 33 requires a manifest permission for notifications. The user must manually click "allow" for notifications
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // If permissions for notifications are not granted, request permission from the user
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }
    // The following two methods were created based on code from the following KDTechs YouTube video: https://www.youtube.com/watch?v=Ijv0vcxNk78

    // scheduleNotification Schedules a notification to occur in a specific amount of milliseconds (delay) using the MyNotificationPublisher class
    private void scheduleNotification(Notification notification, int delay) {
        Intent notificationIntent = new Intent(this, MyNotificationPublisher.class);
        notificationIntent.putExtra(MyNotificationPublisher.NOTIFICATIONID, 1);
        notificationIntent.putExtra(MyNotificationPublisher.NOTIFICATION, notification);
        // PendingIntent is sent as broadcast when the alarm is triggered
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        long futureMillis = SystemClock.elapsedRealtime() + delay; // Notification is shown at this time
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        assert alarmManager != null;
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureMillis, pendingIntent); // Wake the device if it's asleep
    }

    private Notification getNotification(String content) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, default_notification_id);
        builder.setContentText(content); // Set the message for the notification
        builder.setSmallIcon(R.drawable.droid); // Use droid icon
        builder.setAutoCancel(true);
        builder.setChannelId(NOTIFICATION_CHANNEL_ID);

        return builder.build();
    }
}