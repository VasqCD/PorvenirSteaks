package com.example.porvenirsteaks.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.porvenirsteaks.databinding.ActivityRecoverPasswordBinding;
import com.example.porvenirsteaks.utils.Resource;

public class RecoverPasswordActivity extends AppCompatActivity {
    private ActivityRecoverPasswordBinding binding;
    private AuthViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRecoverPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Configurar listeners
        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnSendCode.setOnClickListener(v -> {
            if (validateForm()) {
                sendCode();
            }
        });
    }

    private boolean validateForm() {
        String email = binding.etEmail.getText().toString().trim();

        // Validar email
        if (TextUtils.isEmpty(email)) {
            binding.etEmail.setError("Ingrese su correo electrónico");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.setError("Correo electrónico inválido");
            return false;
        } else {
            binding.etEmail.setError(null);
        }

        return true;
    }

    private void sendCode() {
        String email = binding.etEmail.getText().toString().trim();

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnSendCode.setEnabled(false);

        // Agregar esta función al AuthRepository y AuthViewModel primero
        viewModel.recuperarPassword(email).observe(this, result -> {
            binding.progressBar.setVisibility(View.GONE);
            binding.btnSendCode.setEnabled(true);

            if (result.status == Resource.Status.SUCCESS && result.data != null) {
                Toast.makeText(this, "Código enviado. Revisa tu correo electrónico.", Toast.LENGTH_LONG).show();

                // Ir a la pantalla de cambio de contraseña
                Intent intent = new Intent(this, ChangePasswordActivity.class);
                intent.putExtra("email", email);
                startActivity(intent);
                finish();
            } else if (result.status == Resource.Status.ERROR) {
                Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}