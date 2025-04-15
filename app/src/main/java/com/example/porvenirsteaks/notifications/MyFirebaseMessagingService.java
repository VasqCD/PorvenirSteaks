package com.example.porvenirsteaks.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.porvenirsteaks.MainActivity;
import com.example.porvenirsteaks.R;
import com.example.porvenirsteaks.api.ApiService;
import com.example.porvenirsteaks.api.RetrofitClient;
import com.example.porvenirsteaks.data.preferences.TokenManager;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "FCM_Service";
    private static final String CHANNEL_ID = "porvenir_channel";
    private static final String CHANNEL_NAME = "Porvenir Steaks";
    private static final String CHANNEL_DESC = "Notificaciones de Porvenir Steaks";

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "Refreshed token: " + token);

        // Registrar el nuevo token en el servidor
        enviarTokenAlServidor(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Verificar si el mensaje contiene datos
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            // Manejar los datos de la notificación
            handleNotificationData(remoteMessage.getData());
        }

        // Verificar si el mensaje contiene una notificación
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());

            // Mostrar la notificación
            mostrarNotificacion(
                    remoteMessage.getNotification().getTitle(),
                    remoteMessage.getNotification().getBody(),
                    remoteMessage.getData()
            );
        }
    }

    private void handleNotificationData(Map<String, String> data) {
        String type = data.get("type");
        String pedidoId = data.get("pedido_id");

        // Personalizar el manejo según el tipo de notificación
        if (type != null) {
            switch (type) {
                case "nuevo_pedido":
                case "pedido_en_cocina":
                case "pedido_en_camino":
                case "pedido_entregado":
                    // Notificaciones de estado de pedido
                    if (pedidoId != null) {
                        // Aquí podrías hacer algo específico si es necesario
                    }
                    break;
                case "solicitud_repartidor":
                    // Notificación de solicitud de repartidor (para administradores)
                    // Podríamos hacer algo específico si es necesario
                    break;
                case "promocion":
                    // Notificaciones de promociones
                    break;
                // Otros tipos de notificaciones...
            }
        }
    }

    private void mostrarNotificacion(String title, String body, Map<String, String> data) {
        Log.d(TAG, "Mostrando notificación: " + title + " - " + body);
        Log.d(TAG, "Datos: " + data);

        // Crear intent para abrir la actividad correcta
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Agregar datos extras al intent
        if (data != null && !data.isEmpty()) {
            for (Map.Entry<String, String> entry : data.entrySet()) {
                intent.putExtra(entry.getKey(), entry.getValue());
                Log.d(TAG, "Agregando extra: " + entry.getKey() + " = " + entry.getValue());
            }
        }

        // Crear PendingIntent con FLAG_IMMUTABLE para Android 12+
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Configurar sonido de notificación
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // Crear canal de notificación (Android 8.0+)
        createNotificationChannel();

        // Construir notificación
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setPriority(NotificationCompat.PRIORITY_HIGH) // Alta prioridad
                        .setContentIntent(pendingIntent)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC); // Mostrar en pantalla de bloqueo

        // Si el cuerpo del mensaje es largo, usar estilo expandido
        if (body != null && body.length() > 40) {
            notificationBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(body));
        }

        // Mostrar notificación
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Generar ID único para la notificación
        int notificationId = (int) System.currentTimeMillis();

        // Mostrar notificación
        notificationManager.notify(notificationId, notificationBuilder.build());

        Log.d(TAG, "Notificación mostrada con ID: " + notificationId);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Canal principal
            NotificationChannel channelPrincipal = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH); // Cambiar a HIGH para asegurar que se muestre la notificación

            channelPrincipal.setDescription(CHANNEL_DESC);
            channelPrincipal.enableLights(true);
            channelPrincipal.setLightColor(Color.RED);
            channelPrincipal.enableVibration(true);
            channelPrincipal.setVibrationPattern(new long[]{100, 200, 300, 400, 500});
            channelPrincipal.setShowBadge(true);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channelPrincipal);
        }
    }

    private void enviarTokenAlServidor(String token) {
        // Obtener el token de autenticación
        String authToken = TokenManager.getToken(this);

        if (authToken == null) {
            // No hay usuario autenticado, guardar el token localmente
            // para enviarlo cuando el usuario inicie sesión
            guardarTokenParaEnviarDespues(token);
            return;
        }

        // Crear cliente de la API con el token de autenticación
        ApiService apiService = RetrofitClient.getClient(authToken)
                .create(ApiService.class);

        // Preparar datos para la solicitud
        Map<String, String> request = new HashMap<>();
        request.put("token", token);
        request.put("device_type", "android");

        // Enviar solicitud al servidor
        apiService.registerFcmToken(request).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Token FCM registrado exitosamente en el servidor");
                } else {
                    Log.e(TAG, "Error al registrar token FCM: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Log.e(TAG, "Error de conexión al registrar token FCM: " + t.getMessage());
            }
        });
    }

    private void guardarTokenParaEnviarDespues(String token) {
        // Guardar en SharedPreferences para enviarlo cuando el usuario inicie sesión
        getSharedPreferences("FCM", Context.MODE_PRIVATE)
                .edit()
                .putString("pending_token", token)
                .apply();
    }
}