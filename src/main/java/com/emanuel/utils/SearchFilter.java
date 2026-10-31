package com.emanuel.utils;

import com.emanuel.Appointment;
import com.emanuel.Contact;
import com.emanuel.Schedulable;

public class SearchFilter
{
    public static boolean matches(Schedulable item, String text)
    {
        if (text == null || text.isEmpty())
        {
            return true;
        }

        String filter = text.toLowerCase();

        if (item.getIdentifier().toLowerCase().contains(filter))
        {
            return true;
        }   

        if (item instanceof Contact c)
        {
            return c.getPhone().contains(filter);
        }

        if (item instanceof Appointment a)
        {
            return a.getDate().contains(filter) || a.getTime().contains(filter);            
        }

        return false;
    }
}

