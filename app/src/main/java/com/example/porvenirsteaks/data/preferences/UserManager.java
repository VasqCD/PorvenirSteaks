package com.example.porvenirsteaks.data.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.porvenirsteaks.data.model.User;
import com.google.gson.Gson;

public class UserManager {
    private static final String PREF_NAME = "UserPreferences";
    private static final String KEY_USER = "current_user";

    public static void saveUser(Context context, User user) {
        if (user == null) {
            Log.e("UserManager", "Intentando guardar un usuario nulo");
            return;
        }

        try {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            Gson gson = new Gson();
            String userJson = gson.toJson(user);
            editor.putString(KEY_USER, userJson);
            boolean success = editor.commit(); // Usar commit en lugar de apply para verificar éxito

            if (success) {
                Log.d("UserManager", "Usuario guardado correctamente");
            } else {
                Log.e("UserManager", "Error al guardar usuario en SharedPreferences");
            }
        } catch (Exception e) {
            Log.e("UserManager", "Error guardando usuario: " + e.getMessage(), e);
        }
    }

    public static User getUser(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String userJson = prefs.getString(KEY_USER, null);
        if (userJson == null) {
            return null;
        }
        Gson gson = new Gson();
        return gson.fromJson(userJson, User.class);
    }

    public static void clearUser(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(KEY_USER);
        editor.apply();
    }

    public static String getUserName(Context context) {
        User user = getUser(context);
        if (user != null) {
            return user.getName() + " " + (user.getApellido() != null ? user.getApellido() : "");
        }
        return "";
    }

    public static String getUserEmail(Context context) {
        User user = getUser(context);
        if (user != null) {
            return user.getEmail();
        }
        return "";
    }

    public static String getUserRole(Context context) {
        User user = getUser(context);
        if (user != null) {
            return user.getRol();
        }
        return "cliente"; // Default role
    }
}