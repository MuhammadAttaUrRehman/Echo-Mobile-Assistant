package com.example.echo.services.assistant;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.core.content.ContextCompat;

import com.example.echo.data.model.Reminder;
import com.example.echo.data.repositories.ReminderRepository;
import com.example.echo.data.repositories.NoteRepository;
import com.example.echo.data.model.Note;
import com.example.echo.services.reminder.ReminderNotificationService;
import com.example.echo.ui.activities.MapActivity;
import com.example.echo.ui.activities.YouTubeSearchActivity;
import com.example.echo.utils.GeofenceUtils;
import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Locale;

public class CommandHandler {
    private static final String TAG = "CommandHandler";
    private final Context context;
    private final ReminderRepository reminderRepository;
    private final ActivityResultLauncher<Intent> mapActivityLauncher;
    private static final Map<String, WeakReference<CommandCallback>> callbackMap = new HashMap<>();

    public CommandHandler(Context context, ActivityResultLauncher<Intent> mapActivityLauncher) {
        this.context = context;
        this.reminderRepository = new ReminderRepository(context);
        this.mapActivityLauncher = mapActivityLauncher;
        Log.d(TAG, "CommandHandler initialized");
    }

    public interface CommandCallback {
        void onResult(String result);
        void onError(String error);
    }

    public boolean isCommand(String input) {
        return input.contains("set reminder") ||
                input.contains("remind me") ||
                input.contains("create note") ||
                input.contains("send a text") ||
                input.contains("send message") ||
                input.contains("call") ||
                input.contains("open")||
                input.contains("search"); // Add YouTube search command
    }

    public void handleCommand(String input, CommandCallback callback) {
        input = input.toLowerCase().trim();

        if (input.contains("set reminder") || input.contains("remind me")) {
            handleReminderCommand(input, callback);
        } else if (input.contains("create note")) {
            handleNoteCommand(input, callback);
        } else if (input.contains("send a text") || input.contains("send message")) {
            handleMessageCommand(input, callback);
        } else if (input.contains("call")) {
            handleCallCommand(input, callback);
        } else if (input.contains("open")) {
            handleAppLaunchCommand(input, callback);
        } else if (input.contains("search")) {
            handleSearchCommand(input, callback);
        } else {
            callback.onError("Unknown command: " + input);
            Log.e(TAG, "Unknown command: " + input);
        }
    }

    private void handleSearchCommand(String input, CommandCallback callback) {
        String query = input.replace("search", "").trim();
        if (query.isEmpty()) {
            callback.onError("Please specify a search term (e.g., 'search funny videos')");
            return;
        }

        Intent intent = new Intent(context, YouTubeSearchActivity.class);
        intent.putExtra("query", query);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
            callback.onResult("Searching for " + query + " on YouTube...");
            Log.d(TAG, "Launched YouTubeSearchActivity for query: " + query);
        } catch (Exception e) {
            callback.onError("Failed to open YouTube search: " + e.getMessage());
            Log.e(TAG, "YouTube search launch error: " + e.getMessage());
        }
    }

    private void handleReminderCommand(String input, CommandCallback callback) {
        String title = extractTitle(input, "set reminder", "remind me");
        String description = "From voice command";

        if (input.contains(" at ")) {
            String[] parts = input.split(" at ");
            String timeOrLocation = parts.length > 1 ? parts[1].trim() : "";

            Date scheduledTime = parseTimeExpression(timeOrLocation);
            if (scheduledTime != null) {
                Reminder reminder = new Reminder(
                        UUID.randomUUID().toString(),
                        title,
                        description,
                        scheduledTime
                );
                reminder.setType(Reminder.ReminderType.TIME_BASED);

                reminderRepository.saveReminder(reminder, new ReminderRepository.Callback() {
                    @Override
                    public void onSuccess() {
                        ReminderNotificationService.scheduleTimeReminder(context, reminder);
                        callback.onResult("Time-based reminder set: " + title + " at " + timeOrLocation);
                        Log.d(TAG, "Time-based reminder saved: " + title);
                    }

                    @Override
                    public void onError(Exception e) {
                        callback.onError("Error setting reminder: " + e.getMessage());
                        Log.e(TAG, "Error saving reminder: " + e.getMessage());
                    }
                });
            } else {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    callback.onError("Please grant location permission");
                    Log.w(TAG, "ACCESS_FINE_LOCATION permission not granted");
                    return;
                }

                if (mapActivityLauncher == null) {
                    callback.onError("Cannot select location: Activity context required");
                    Log.e(TAG, "MapActivity launcher not initialized");
                    return;
                }

                String reminderId = UUID.randomUUID().toString();
                callbackMap.put(reminderId, new WeakReference<>(callback));
                Intent intent = new Intent(context, MapActivity.class);
                intent.putExtra("pending_reminder_data", new String[]{reminderId, title, description});
                mapActivityLauncher.launch(intent);
                callback.onResult("Please select location for reminder: " + title);
                Log.d(TAG, "Launched MapActivity for location-based reminder: " + title);
            }
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MINUTE, 1);
            Date scheduledTime = calendar.getTime();

            Reminder reminder = new Reminder(
                    UUID.randomUUID().toString(),
                    title,
                    description,
                    scheduledTime
            );
            reminder.setType(Reminder.ReminderType.TIME_BASED);

            reminderRepository.saveReminder(reminder, new ReminderRepository.Callback() {
                @Override
                public void onSuccess() {
                    ReminderNotificationService.scheduleTimeReminder(context, reminder);
                    callback.onResult("Time-based reminder set: " + title);
                    Log.d(TAG, "Time-based reminder saved: " + title);
                }

                @Override
                public void onError(Exception e) {
                    callback.onError("Error setting reminder: " + e.getMessage());
                    Log.e(TAG, "Error saving reminder: " + e.getMessage());
                }
            });
        }
    }

    public void handleMapActivityResult(ActivityResult result, CommandCallback callback) {
        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
            Intent data = result.getData();
            double latitude = data.getDoubleExtra("latitude", 0.0);
            double longitude = data.getDoubleExtra("longitude", 0.0);
            String locationName = data.getStringExtra("location_name");
            int radiusInMeters = data.getIntExtra("radius", 100);
            String[] commandData = data.getStringArrayExtra("pending_reminder_data");
            if (commandData != null) {
                String id = commandData[0];
                String title = commandData[1];
                String description = commandData[2];
                WeakReference<CommandCallback> callbackRef = callbackMap.remove(id);
                CommandCallback storedCallback = callbackRef != null ? callbackRef.get() : callback;
                Reminder reminder = new Reminder(
                        id,
                        title,
                        description,
                        latitude,
                        longitude,
                        locationName,
                        radiusInMeters
                );
                reminder.setType(Reminder.ReminderType.LOCATION_BASED);
                saveLocationBasedReminder(reminder, storedCallback, locationName);
            }
        } else if (result.getData() != null) {
            String[] commandData = result.getData().getStringArrayExtra("pending_reminder_data");
            if (commandData != null) {
                String id = commandData[0];
                WeakReference<CommandCallback> callbackRef = callbackMap.remove(id);
                CommandCallback storedCallback = callbackRef != null ? callbackRef.get() : callback;
                if (storedCallback != null) {
                    storedCallback.onError("Location selection cancelled");
                }
            }
        }
    }

    private void saveLocationBasedReminder(Reminder reminder, CommandCallback callback, String locationName) {
        reminderRepository.saveReminder(reminder, new ReminderRepository.Callback() {
            @Override
            public void onSuccess() {
                GeofenceUtils.addGeofence(context, reminder);
                if (callback != null) {
                    callback.onResult("Location-based reminder set: " + reminder.getTitle() + " at " + locationName);
                }
                Log.d(TAG, "Location-based reminder saved: " + reminder.getTitle());
            }

            @Override
            public void onError(Exception e) {
                if (callback != null) {
                    callback.onError("Error setting reminder: " + e.getMessage());
                }
                Log.e(TAG, "Error saving reminder: " + e.getMessage());
            }
        });
    }

    private void handleNoteCommand(String input, CommandCallback callback) {
        String content = extractTitle(input, "create note");
        String title = content.split("\\.")[0].trim();
        String userId = "default_user";
        Note note = new Note(
                UUID.randomUUID().toString(),
                userId,
                title,
                content,
                System.currentTimeMillis()
        );

        NoteRepository noteRepository = new NoteRepository(context);
        noteRepository.saveNote(note, new NoteRepository.Callback() {
            @Override
            public void onSuccess() {
                callback.onResult("Note saved. Would you like me to read it back to you?");
                Log.d(TAG, "Note saved: " + content);
            }

            @Override
            public void onError(Exception e) {
                callback.onError("Error saving note: " + e.getMessage());
                Log.e(TAG, "Error saving note: " + e.getMessage());
            }
        });
    }

    private void handleMessageCommand(String input, CommandCallback callback) {
        String[] parts = input.split("to");
        if (parts.length < 2) {
            callback.onError("Please specify a contact name");
            return;
        }
        String message = extractTitle(parts[0], "send a text", "send message").trim();
        String contactName = parts[1].trim();

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            callback.onError("Please grant permission to send SMS");
            Log.w(TAG, "SEND_SMS permission not granted");
            return;
        }

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            callback.onError("Please grant permission to read contacts");
            Log.w(TAG, "READ_CONTACTS permission not granted");
            return;
        }

        String phoneNumber = resolveContactPhoneNumber(contactName);
        if (phoneNumber == null) {
            callback.onError("Contact not found: " + contactName);
            return;
        }

        try {
            android.telephony.SmsManager smsManager = android.telephony.SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            callback.onResult("Message sent to " + contactName + ": " + message);
            Log.d(TAG, "SMS sent to " + phoneNumber);
        } catch (Exception e) {
            callback.onError("Error sending message: " + e.getMessage());
            Log.e(TAG, "SMS error: " + e.getMessage());
        }
    }

    private void handleCallCommand(String input, CommandCallback callback) {
        String contactName = extractTitle(input, "call").trim();

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            callback.onError("Please grant permission to make calls");
            Log.w(TAG, "CALL_PHONE permission not granted");
            return;
        }

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            callback.onError("Please grant permission to read contacts");
            Log.w(TAG, "READ_CONTACTS permission not granted");
            return;
        }

        String phoneNumber = resolveContactPhoneNumber(contactName);
        if (phoneNumber == null) {
            callback.onError("Contact not found: " + contactName);
            return;
        }

        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
            callback.onResult("Calling " + contactName);
            Log.d(TAG, "Call initiated to " + phoneNumber);
        } catch (Exception e) {
            callback.onError("Error initiating call: " + e.getMessage());
            Log.e(TAG, "Call error: " + e.getMessage());
        }
    }

    private void handleAppLaunchCommand(String input, CommandCallback callback) {
        String appName = extractTitle(input, "open").trim();
        String packageName = getAppPackageName(appName);

        if (packageName != null) {
            PackageManager pm = context.getPackageManager();
            Intent intent = pm.getLaunchIntentForPackage(packageName);
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    context.startActivity(intent);
                    callback.onResult("Opening " + appName);
                    Log.d(TAG, "App launched: " + appName + " (" + packageName + ")");
                } catch (Exception e) {
                    callback.onError("Failed to open " + appName + ": " + e.getMessage());
                    Log.e(TAG, "App launch error: " + e.getMessage());
                }
            } else {
                callback.onError("App not found: " + appName);
                Log.e(TAG, "No launch intent for package: " + packageName);
            }
        } else {
            callback.onError("App not found: " + appName);
            Log.e(TAG, "No package found for app: " + appName);
        }
    }

    private String extractTitle(String input, String... prefixes) {
        for (String prefix : prefixes) {
            if (input.toLowerCase().startsWith(prefix)) {
                return input.substring(prefix.length()).trim();
            }
        }
        return input.trim();
    }

    private String extractLocation(String input) {
        String[] parts = input.split(" at ");
        return parts.length > 1 ? parts[1].trim() : "Default Location";
    }

    private String resolveContactPhoneNumber(String contactName) {
        ContentResolver contentResolver = context.getContentResolver();
        String phoneNumber = null;
        Cursor cursor = null;

        try {
            cursor = contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    new String[]{
                            ContactsContract.CommonDataKinds.Phone.NUMBER,
                            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
                    },
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " LIKE ?",
                    new String[]{"%" + contactName + "%"},
                    null
            );

            if (cursor != null && cursor.moveToFirst()) {
                int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                phoneNumber = cursor.getString(numberIndex);
                Log.d(TAG, "Found phone number for " + contactName + ": " + phoneNumber);
            } else {
                Log.w(TAG, "No contact found for name: " + contactName);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error querying contacts: " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return phoneNumber;
    }

    private String getAppPackageName(String appName) {
        PackageManager pm = context.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        for (ResolveInfo resolveInfo : pm.queryIntentActivities(intent, 0)) {
            String label = resolveInfo.loadLabel(pm).toString().toLowerCase(Locale.getDefault());
            if (label.contains(appName.toLowerCase(Locale.getDefault()))) {
                String packageName = resolveInfo.activityInfo.packageName;
                Log.d(TAG, "Found matching app: " + label + " (" + packageName + ")");
                return packageName;
            }
        }
        Log.w(TAG, "No matching app found for: " + appName);
        return null;
    }

    private Date parseTimeExpression(String timeExpression) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());

        timeExpression = timeExpression.toLowerCase().trim();

        try {
            if (timeExpression.contains("tomorrow")) {
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                String timePart = timeExpression.replace("tomorrow", "").trim();
                Pattern timePattern = Pattern.compile("(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)?");
                Matcher matcher = timePattern.matcher(timePart);
                if (matcher.find()) {
                    int hour = Integer.parseInt(matcher.group(1));
                    int minute = matcher.group(2) != null ? Integer.parseInt(matcher.group(2)) : 0;
                    String amPm = matcher.group(3) != null ? matcher.group(3).toUpperCase() : "AM";

                    if (amPm.equals("PM") && hour < 12) {
                        hour += 12;
                    } else if (amPm.equals("AM") && hour == 12) {
                        hour = 0;
                    }

                    calendar.set(Calendar.HOUR_OF_DAY, hour);
                    calendar.set(Calendar.MINUTE, minute);
                    calendar.set(Calendar.SECOND, 0);
                    return calendar.getTime();
                }
            } else if (timeExpression.matches("\\d{1,2}(?::\\d{2})?\\s*(am|pm)")) {
                Pattern timePattern = Pattern.compile("(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)");
                Matcher matcher = timePattern.matcher(timeExpression);
                if (matcher.find()) {
                    int hour = Integer.parseInt(matcher.group(1));
                    int minute = matcher.group(2) != null ? Integer.parseInt(matcher.group(2)) : 0;
                    String amPm = matcher.group(3).toUpperCase();

                    if (amPm.equals("PM") && hour < 12) {
                        hour += 12;
                    } else if (amPm.equals("AM") && hour == 12) {
                        hour = 0;
                    }

                    calendar.set(Calendar.HOUR_OF_DAY, hour);
                    calendar.set(Calendar.MINUTE, minute);
                    calendar.set(Calendar.SECOND, 0);
                    return calendar.getTime();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing time expression: " + timeExpression, e);
        }
        return null;
    }
}