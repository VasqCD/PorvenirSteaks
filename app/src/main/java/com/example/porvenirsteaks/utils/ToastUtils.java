package com.example.porvenirsteaks.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.porvenirsteaks.R;

public class ToastUtils {

    public static void showToast(Context context, String message, int iconResId) {
        // Inflar el layout personalizado para el Toast
        LayoutInflater inflater = LayoutInflater.from(context);
        View layout = inflater.inflate(R.layout.custom_toast_layout, null);

        // Configurar el mensaje
        TextView text = layout.findViewById(R.id.toast_text);
        text.setText(message);

        // Configurar el icono
        ImageView icon = layout.findViewById(R.id.toast_icon);
        if (iconResId != 0) {
            icon.setImageResource(iconResId);
            icon.setVisibility(View.VISIBLE);
        } else {
            icon.setVisibility(View.GONE);
        }

        // Crear y mostrar el Toast
        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }

    public static void showSuccessToast(Context context, String message) {
        showToast(context, message, R.drawable.ic_success);
    }

    public static void showErrorToast(Context context, String message) {
        showToast(context, message, R.drawable.ic_error);
    }

    public static void showInfoToast(Context context, String message) {
        showToast(context, message, R.drawable.ic_info);
    }

    public static void showWarningToast(Context context, String message) {
        showToast(context, message, R.drawable.ic_warning);
    }
}