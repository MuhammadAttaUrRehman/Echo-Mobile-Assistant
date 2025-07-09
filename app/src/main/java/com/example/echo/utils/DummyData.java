package com.example.echo.utils;

import com.example.echo.data.model.Note;
import com.example.echo.data.model.Reminder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DummyData {
    
    public static List<Note> getDummyNotes() {
        List<Note> notes = new ArrayList<>();
        
        notes.add(new Note("1", "Shopping List", 
            "1. Groceries\n2. New headphones\n3. Birthday gift for Mom"));
        
        notes.add(new Note("2", "Project Ideas", 
            "- Mobile app for plant care\n- Smart home automation system\n- Fitness tracking website"));
        
        notes.add(new Note("3", "Meeting Notes", 
            "Team meeting points:\n- Project timeline review\n- Resource allocation\n- Next sprint planning"));
        
        notes.add(new Note("4", "Book Recommendations", 
            "1. The Pragmatic Programmer\n2. Clean Code\n3. Design Patterns"));
        
        return notes;
    }
    
    public static List<Reminder> getDummyReminders() {
        List<Reminder> reminders = new ArrayList<>();
        
        // Time-based reminders
        Calendar cal = Calendar.getInstance();
        
        // Tomorrow at 10 AM
        cal.add(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 10);
        cal.set(Calendar.MINUTE, 0);
        reminders.add(new Reminder("1", "Team Meeting", 
            "Weekly sync-up with the development team", cal.getTime()));
        
        // Day after tomorrow at 3 PM
        cal.add(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 15);
        reminders.add(new Reminder("2", "Dentist Appointment", 
            "Regular checkup and cleaning", cal.getTime()));
        
        // Location-based reminders
        reminders.add(new Reminder("3", "Buy Groceries", 
            "Pick up items from the shopping list",
            37.7749, -122.4194, "Supermarket", 100));
        
        reminders.add(new Reminder("4", "Gym Workout", 
            "Don't forget your workout routine!",
            37.7833, -122.4167, "Fitness Center", 50));
        
        return reminders;
    }
} 