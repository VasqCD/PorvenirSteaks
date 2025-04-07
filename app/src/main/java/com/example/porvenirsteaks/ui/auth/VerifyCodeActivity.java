package com.example.porvenirsteaks.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.porvenirsteaks.databinding.ActivityVerifyCodeBinding;
import com.example.porvenirsteaks.utils.Resource;

public class VerifyCodeActivity extends AppCompatActivity {
    private ActivityVerifyCodeBinding binding;
    private AuthViewModel viewModel;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVerifyCodeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Obtener email de los extras
        email = getIntent().getStringExtra("email");
        if (email == null) {
            Toast.makeText(this, "Error: correo electrónico no proporcionado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        binding.tvEmailInfo.setText("Hemos enviado un código a " + email);

        // Configurar listeners
        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnVerify.setOnClickListener(v -> {
            if (validateCode()) {
                verifyCode();
            }
        });

        binding.btnResendCode.setOnClickListener(v -> {
            resendCode();
        });
    }

    private boolean validateCode() {
        String code = binding.etCode.getText().toString().trim();
        if (TextUtils.isEmpty(code)) {
            binding.etCode.setError("Ingrese el código de verificación");
            return false;
        }

        if (code.length() != 6) {
            binding.etCode.setError("El código debe tener 6 dígitos");
            return false;
        }

        return true;
    }

    private void verifyCode() {
        String code = binding.etCode.getText().toString().trim();

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnVerify.setEnabled(false);

        viewModel.verificarCodigo(email, code).observe(this, result -> {
            binding.progressBar.setVisibility(View.GONE);
            binding.btnVerify.setEnabled(true);

            if (result.status == Resource.Status.SUCCESS && result.data != null) {
                Toast.makeText(this, "Correo verificado exitosamente", Toast.LENGTH_SHORT).show();

                // Redirigir a la pantalla de login
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else if (result.status == Resource.Status.ERROR) {
                Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resendCode() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnResendCode.setEnabled(false);

        // Agregar esta función al AuthRepository y AuthViewModel primero
        viewModel.reenviarCodigo(email).observe(this, result -> {
            binding.progressBar.setVisibility(View.GONE);
            binding.btnResendCode.setEnabled(true);

            if (result.status == Resource.Status.SUCCESS) {
                Toast.makeText(this, "Código reenviado exitosamente", Toast.LENGTH_SHORT).show();
            } else if (result.status == Resource.Status.ERROR) {
                Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}