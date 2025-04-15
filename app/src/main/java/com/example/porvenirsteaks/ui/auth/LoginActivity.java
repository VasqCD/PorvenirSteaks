package com.example.porvenirsteaks.ui.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.porvenirsteaks.MainActivity;
import com.example.porvenirsteaks.R;
import com.example.porvenirsteaks.api.ApiService;
import com.example.porvenirsteaks.api.RetrofitClient;
import com.example.porvenirsteaks.data.preferences.TokenManager;
import com.example.porvenirsteaks.data.preferences.UserManager;
import com.example.porvenirsteaks.databinding.ActivityLoginBinding;
import com.example.porvenirsteaks.utils.Resource;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private AuthViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Verificar si ya tiene sesión
        if (TokenManager.hasToken(this)) {
            goToMainActivity();
            return;
        }

        // Configurar listeners
        binding.btnLogin.setOnClickListener(v -> {
            if (validateForm()) {
                login();
            }
        });

        binding.tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });

        binding.tvForgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(this, RecoverPasswordActivity.class));
        });
    }

    private boolean validateForm() {
        boolean valid = true;

        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        // Validar email
        if (TextUtils.isEmpty(email)) {
            binding.etEmail.setError("Ingrese su email");
            valid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.setError("Email inválido");
            valid = false;
        } else {
            binding.etEmail.setError(null);
        }

        // Validar contraseña
        if (TextUtils.isEmpty(password)) {
            binding.etPassword.setError("Ingrese su contraseña");
            valid = false;
        } else {
            binding.etPassword.setError(null);
        }

        return valid;
    }

    private void login() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        // Mostrar loading
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnLogin.setEnabled(false);

        viewModel.login(email, password).observe(this, result -> {
            binding.progressBar.setVisibility(View.GONE);
            binding.btnLogin.setEnabled(true);

            if (result.status == Resource.Status.SUCCESS && result.data != null) {
                // Guardar token
                TokenManager.saveToken(this, result.data.getToken());

                // Guardar info del usuario
                UserManager.saveUser(this, result.data.getUser());

                // Enviar token FCM pendiente si existe
                enviarTokenFcmPendiente();

                // Ir a MainActivity
                goToMainActivity();
                
            } else if (result.status == Resource.Status.VERIFICATION_REQUIRED) {
                Toast.makeText(this, result.message, Toast.LENGTH_LONG).show();

                // Redirigir a la pantalla de verificación
                Intent intent = new Intent(this, VerifyCodeActivity.class);
                intent.putExtra("email", result.email);
                startActivity(intent);
            } else if (result.status == Resource.Status.ERROR) {
                Toast.makeText(this, result.message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void enviarTokenFcmPendiente() {
        SharedPreferences prefs = getSharedPreferences("FCM", MODE_PRIVATE);
        String pendingToken = prefs.getString("pending_token", null);

        if (pendingToken != null && TokenManager.hasToken(this)) {
            ApiService apiService = RetrofitClient.getClient(TokenManager.getToken(this))
                    .create(ApiService.class);

            Map<String, String> request = new HashMap<>();
            request.put("token", pendingToken);
            request.put("device_type", "android");

            apiService.registerFcmToken(request).enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                    if (response.isSuccessful()) {
                        Log.d("FCM", "Token FCM pendiente registrado exitosamente");
                        prefs.edit().remove("pending_token").apply();
                    } else {
                        Log.e("FCM", "Error al registrar token FCM pendiente: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                    Log.e("FCM", "Error de conexión al registrar token FCM pendiente: " + t.getMessage());
                }
            });
        }
    }

    private void goToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}