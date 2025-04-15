package com.example.porvenirsteaks.data.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.porvenirsteaks.data.model.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class UserManager {
    private static final String PREF_NAME = "UserPreferences";
    private static final String KEY_USER = "current_user";

    public static void saveUser(Context context, User user) {
        if (context == null) {
            Log.e("UserManager", "Context es nulo al guardar usuario");
            return;
        }

        if (user == null) {
            Log.e("UserManager", "Se está intentando guardar un usuario nulo");
            return;
        }

        // Logs para depuración
        Log.d("UserManager", "Guardando usuario: ID=" + user.getId() +
                ", Nombre=" + user.getName() +
                ", Email=" + user.getEmail() +
                ", Rol=" + user.getRol());

        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        try {
            // Usar GsonBuilder para manejar mejor nulos
            Gson gson = new GsonBuilder()
                    .serializeNulls() // Esto asegura que los campos nulos también sean serializados
                    .create();

            String userJson = gson.toJson(user);
            Log.d("UserManager", "JSON serializado: " + userJson);

            editor.putString(KEY_USER, userJson);
            boolean success = editor.commit(); // Usar commit en lugar de apply para verificar si se guardó correctamente

            if (success) {
                Log.d("UserManager", "Usuario guardado correctamente");
            } else {
                Log.e("UserManager", "Error al guardar usuario en SharedPreferences");
            }
        } catch (Exception e) {
            Log.e("UserManager", "Error al serializar usuario: " + e.getMessage(), e);
        }
    }

    public static void updateUser(Context context, User newUserData) {
        User currentUser = getUser(context);
        if (currentUser != null && newUserData != null) {
            // Solo actualizar los campos que no son nulos en los nuevos datos
            if (newUserData.getName() != null) currentUser.setName(newUserData.getName());
            if (newUserData.getApellido() != null) currentUser.setApellido(newUserData.getApellido());
            if (newUserData.getEmail() != null) currentUser.setEmail(newUserData.getEmail());
            if (newUserData.getTelefono() != null) currentUser.setTelefono(newUserData.getTelefono());
            if (newUserData.getRol() != null) currentUser.setRol(newUserData.getRol());
            if (newUserData.getFotoPerfil() != null) currentUser.setFotoPerfil(newUserData.getFotoPerfil());
            if (newUserData.getFechaRegistro() != null) currentUser.setFechaRegistro(newUserData.getFechaRegistro());
            if (newUserData.getUltimaConexion() != null) currentUser.setUltimaConexion(newUserData.getUltimaConexion());

            // Guardar el usuario actualizado
            saveUser(context, currentUser);
        } else if (newUserData != null) {
            // Si no hay usuario actual, guardar el nuevo
            saveUser(context, newUserData);
        }
    }

    public static User getUser(Context context) {
        if (context == null) {
            Log.e("UserManager", "Context es nulo al obtener usuario");
            return null;
        }

        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String userJson = prefs.getString(KEY_USER, null);

        Log.d("UserManager", "JSON recuperado: " + userJson);

        if (userJson == null || userJson.isEmpty()) {
            Log.w("UserManager", "No hay usuario guardado en SharedPreferences");
            return null;
        }

        try {
            Gson gson = new Gson();
            User user = gson.fromJson(userJson, User.class);

            if (user == null) {
                Log.e("UserManager", "Usuario deserializado es nulo");
                return null;
            }

            // Verificar campos críticos
            Log.d("UserManager", "Usuario recuperado: ID=" + user.getId() +
                    ", Nombre=" + (user.getName() != null ? user.getName() : "null") +
                    ", Email=" + (user.getEmail() != null ? user.getEmail() : "null") +
                    ", Rol=" + (user.getRol() != null ? user.getRol() : "null"));

            // Si el rol es nulo, establecerlo como "cliente" por defecto
            if (user.getRol() == null) {
                Log.w("UserManager", "Rol del usuario es nulo, estableciendo valor por defecto 'cliente'");
                user.setRol("cliente");
            }

            return user;
        } catch (Exception e) {
            Log.e("UserManager", "Error al deserializar usuario: " + e.getMessage(), e);
            return null;
        }
    }

    public static void clearUser(Context context) {
        if (context == null) {
            Log.e("UserManager", "Context es nulo al limpiar usuario");
            return;
        }

        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(KEY_USER);
        boolean success = editor.commit();

        if (success) {
            Log.d("UserManager", "Usuario eliminado correctamente");
        } else {
            Log.e("UserManager", "Error al eliminar usuario de SharedPreferences");
        }
    }

    public static String getUserName(Context context) {
        User user = getUser(context);
        if (user != null) {
            String nombre = user.getName();
            String apellido = user.getApellido();

            if (nombre == null) nombre = "";
            if (apellido == null) apellido = "";

            return nombre + " " + (apellido.isEmpty() ? "" : apellido);
        }
        return "";
    }

    public static String getUserEmail(Context context) {
        User user = getUser(context);
        if (user != null) {
            return user.getEmail() != null ? user.getEmail() : "";
        }
        return "";
    }

    public static String getUserRole(Context context) {
        User user = getUser(context);
        if (user != null) {
            String rol = user.getRol();
            // Asegurarse de que nunca se devuelva nulo
            return rol != null ? rol : "cliente";
        }
        return "cliente"; // Default role
    }
}