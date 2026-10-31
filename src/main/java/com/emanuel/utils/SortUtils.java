package com.emanuel.utils;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

import com.emanuel.Schedulable;
import com.emanuel.Appointment;

public class SortUtils
{
    private static final Collator COLLATOR = Collator.getInstance(Locale.forLanguageTag("pt-BR"));

    static
    {
        COLLATOR.setStrength(Collator.SECONDARY); // ignora maiúsculas/minúsculas, respeita acentos
    }

    public static Comparator<Schedulable> byName(boolean ascending)
    {
        Comparator<Schedulable> comparator =
            (a, b) -> COLLATOR.compare(a.getIdentifier(), b.getIdentifier());

        return ascending ? comparator : comparator.reversed();
    }

    public static Comparator<Schedulable> byDate(boolean ascending)
    {
        Comparator<Schedulable> comparator = Comparator
            .comparing((Schedulable s) -> ((Appointment) s).getDateTime())
            .thenComparing(byName(true));

        return ascending ? comparator : comparator.reversed();
    }
}