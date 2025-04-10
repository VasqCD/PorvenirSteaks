package com.example.porvenirsteaks.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.porvenirsteaks.MainActivity;
import com.example.porvenirsteaks.R;
import com.example.porvenirsteaks.data.preferences.TokenManager;
import com.example.porvenirsteaks.data.preferences.UserManager;
import com.example.porvenirsteaks.databinding.ActivityLoginBinding;
import com.example.porvenirsteaks.utils.Resource;

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

    private void goToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}