package com.example.porvenirsteaks.utils;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtils {
    private static final String TAG = "DateUtils";

    private static final String[] DATE_FORMATS = {
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX",
            "yyyy-MM-dd'T'HH:mm:ssXXX",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd"
    };

    public static Date parseDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return null;
        }

        // Intentar cada formato
        for (String format : DATE_FORMATS) {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.US);
                return dateFormat.parse(dateString);
            } catch (ParseException e) {
                // Ignorar la excepción y continuar con el siguiente formato
                Log.d(TAG, "Error al parsear la fecha con el formato: " + format, e);
            }
        }

        Log.e(TAG, "No se pudo parsear la fecha: " + dateString);
        return null;
    }

    public static String formatDate(Date date, String outputPattern) {
        if (date == null) {
            return "No disponible";
        }

        SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern, Locale.getDefault());
        return outputFormat.format(date);
    }

    public static String formatDateString(String dateString, String outputPattern) {
        Date date = parseDate(dateString);
        return formatDate(date, outputPattern);
    }
}