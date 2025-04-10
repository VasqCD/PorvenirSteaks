package com.example.porvenirsteaks.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.porvenirsteaks.databinding.ActivityRegisterBinding;
import com.example.porvenirsteaks.utils.Resource;
import com.example.porvenirsteaks.utils.ToastUtils;

public class RegisterActivity extends AppCompatActivity {
    private ActivityRegisterBinding binding;
    private AuthViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        binding.btnRegister.setOnClickListener(v -> {
            if (validateForm()) {
                register();
            }
        });

        binding.tvLogin.setOnClickListener(v -> {
            finish(); // Volver a login
        });


    }

    private boolean validateForm() {
        boolean valid = true;

        String name = binding.etName.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        String confirmPassword = binding.etConfirmPassword.getText().toString().trim();
        String telefono = binding.etTelefono.getText().toString().trim();

        // Validar nombre
        if (TextUtils.isEmpty(name)) {
            binding.etName.setError("Ingrese su nombre");
            valid = false;
        } else {
            binding.etName.setError(null);
        }

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
            binding.etPassword.setError("Ingrese una contraseña");
            valid = false;
        } else if (password.length() < 8) {
            binding.etPassword.setError("La contraseña debe tener al menos 8 caracteres");
            valid = false;
        } else {
            binding.etPassword.setError(null);
        }

        // Validar confirmación de contraseña
        if (TextUtils.isEmpty(confirmPassword)) {
            binding.etConfirmPassword.setError("Confirme su contraseña");
            valid = false;
        } else if (!password.equals(confirmPassword)) {
            binding.etConfirmPassword.setError("Las contraseñas no coinciden");
            valid = false;
        } else {
            binding.etConfirmPassword.setError(null);
        }

        // Validar teléfono (si no está vacío)
        if (!TextUtils.isEmpty(telefono)) {
            // Eliminar cualquier carácter no numérico para la validación
            String telefonoNumbers = telefono.replaceAll("[^0-9]", "");

            if (telefonoNumbers.length() != 8) {
                binding.etTelefono.setError("El teléfono debe tener exactamente 8 dígitos");
                valid = false;
            } else {
                binding.etTelefono.setError(null);
            }
        } else {
            binding.etTelefono.setError(null);
        }

        return valid;
    }

    private void register() {
        String name = binding.etName.getText().toString().trim();
        String apellido = binding.etApellido.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        String telefono = binding.etTelefono.getText().toString().trim();

        // Mostrar loading
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnRegister.setEnabled(false);

        viewModel.register(name, apellido, email, password, telefono).observe(this, result -> {
            binding.progressBar.setVisibility(View.GONE);
            binding.btnRegister.setEnabled(true);

            if (result.status == Resource.Status.SUCCESS && result.data != null) {
                //Toast.makeText(this, "Registro exitoso. Verifica tu correo.", Toast.LENGTH_LONG).show();
                ToastUtils.showSuccessToast(this, "Registro exitoso. Verifica tu correo.");

                // Ir a la pantalla de verificación
                Intent intent = new Intent(this, VerifyCodeActivity.class);
                intent.putExtra("email", email);
                startActivity(intent);
                finish();

            } else if (result.status == Resource.Status.ERROR) {
                Toast.makeText(this, result.message, Toast.LENGTH_LONG).show();
            }
        });
    }
}