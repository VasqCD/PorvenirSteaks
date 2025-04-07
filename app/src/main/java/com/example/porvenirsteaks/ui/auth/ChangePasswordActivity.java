package com.example.porvenirsteaks.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.porvenirsteaks.databinding.ActivityChangePasswordBinding;
import com.example.porvenirsteaks.utils.Resource;

public class ChangePasswordActivity extends AppCompatActivity {
    private ActivityChangePasswordBinding binding;
    private AuthViewModel viewModel;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChangePasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Obtener email de los extras
        email = getIntent().getStringExtra("email");
        if (email == null) {
            Toast.makeText(this, "Error: correo electrónico no proporcionado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Configurar listeners
        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnChangePassword.setOnClickListener(v -> {
            if (validateForm()) {
                changePassword();
            }
        });
    }

    private boolean validateForm() {
        String code = binding.etCode.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        String confirmPassword = binding.etConfirmPassword.getText().toString().trim();

        // Validar código
        if (TextUtils.isEmpty(code)) {
            binding.etCode.setError("Ingrese el código de verificación");
            return false;
        } else {
            binding.etCode.setError(null);
        }

        // Validar contraseña
        if (TextUtils.isEmpty(password)) {
            binding.etPassword.setError("Ingrese una nueva contraseña");
            return false;
        } else if (password.length() < 8) {
            binding.etPassword.setError("La contraseña debe tener al menos 8 caracteres");
            return false;
        } else {
            binding.etPassword.setError(null);
        }

        // Validar confirmación de contraseña
        if (TextUtils.isEmpty(confirmPassword)) {
            binding.etConfirmPassword.setError("Confirme su contraseña");
            return false;
        } else if (!password.equals(confirmPassword)) {
            binding.etConfirmPassword.setError("Las contraseñas no coinciden");
            return false;
        } else {
            binding.etConfirmPassword.setError(null);
        }

        return true;
    }

    private void changePassword() {
        String code = binding.etCode.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        String confirmPassword = binding.etConfirmPassword.getText().toString().trim();

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnChangePassword.setEnabled(false);

        // Agregar esta función al AuthRepository y AuthViewModel primero
        viewModel.cambiarPassword(email, code, password, confirmPassword).observe(this, result -> {
            binding.progressBar.setVisibility(View.GONE);
            binding.btnChangePassword.setEnabled(true);

            if (result.status == Resource.Status.SUCCESS && result.data != null) {
                Toast.makeText(this, "Contraseña actualizada exitosamente", Toast.LENGTH_LONG).show();

                // Ir a la pantalla de login
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else if (result.status == Resource.Status.ERROR) {
                Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}