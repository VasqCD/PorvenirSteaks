package com.example.porvenirsteaks;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.porvenirsteaks.data.preferences.TokenManager;
import com.example.porvenirsteaks.databinding.ActivitySplashBinding;
import com.example.porvenirsteaks.ui.auth.LoginActivity;

public class SplashActivity extends AppCompatActivity {
    private static final long SPLASH_DELAY = 2000; // 2 segundos
    private ActivitySplashBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Mostrar la animación del logo
        binding.ivLogo.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(1000)
                .start();

        // Verificar token después de un retraso
        new Handler(Looper.getMainLooper()).postDelayed(this::verificarSesion, SPLASH_DELAY);
    }

    private void verificarSesion() {
        if (TokenManager.hasToken(this)) {
            // Si hay un token, ir a MainActivity
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        } else {
            // Si no hay token, ir a LoginActivity
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }

        // Cerrar SplashActivity
        finish();
    }
}