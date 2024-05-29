package com.example.fasipemobilej.network;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateFormatter {

    public static String formatDateTime(String originalDateTime) {

        SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());


        SimpleDateFormat targetFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        try {

            Date date = originalFormat.parse(originalDateTime);

            return targetFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return "Data inválida";
        }
    }

}
