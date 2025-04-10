package com.example.porvenirsteaks.ui.perfil;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.porvenirsteaks.R;
import com.example.porvenirsteaks.data.model.User;
import com.example.porvenirsteaks.data.preferences.UserManager;
import com.example.porvenirsteaks.databinding.DialogCambiarContrasenaBinding;
import com.example.porvenirsteaks.databinding.DialogEditarPerfilBinding;
import com.example.porvenirsteaks.databinding.DialogSolicitarRepartidorBinding;
import com.example.porvenirsteaks.databinding.FragmentPerfilBinding;
import com.example.porvenirsteaks.ui.auth.LoginActivity;
import com.example.porvenirsteaks.utils.Constants;
import com.example.porvenirsteaks.utils.ImageUtils;
import com.example.porvenirsteaks.utils.Resource;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PerfilFragment extends Fragment {
    private FragmentPerfilBinding binding;
    private PerfilViewModel viewModel;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPerfilBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(PerfilViewModel.class);

        setupUI();
        cargarDatosPerfil();
    }

    private void setupUI() {
        // Configurar botón editar foto
        binding.fabEditFoto.setOnClickListener(v -> {
            openImagePicker();
        });

        // Configurar botones de acciones
        binding.btnEditarPerfil.setOnClickListener(v -> {
            mostrarDialogoEditarPerfil();
        });

        binding.btnCambiarContrasena.setOnClickListener(v -> {
            mostrarDialogoCambiarContrasena();
        });

        binding.btnSolicitarSerRepartidor.setOnClickListener(v -> {
            mostrarDialogoSolicitarRepartidor();
        });

        binding.btnCerrarSesion.setOnClickListener(v -> {
            confirmarCerrarSesion();
        });
    }

    private void cargarDatosPerfil() {
        binding.progressBar.setVisibility(View.VISIBLE);

        viewModel.getUserProfile().observe(getViewLifecycleOwner(), result -> {
            binding.progressBar.setVisibility(View.GONE);

            if (result.status == Resource.Status.SUCCESS && result.data != null) {
                actualizarUIConDatosUsuario(result.data);
            } else if (result.status == Resource.Status.ERROR) {
                Toast.makeText(requireContext(), "Error: " + result.message, Toast.LENGTH_SHORT).show();
                // Intentar cargar desde la caché local
                User cachedUser = UserManager.getUser(requireContext());
                if (cachedUser != null) {
                    actualizarUIConDatosUsuario(cachedUser);
                }
            }
        });
    }

    private void actualizarUIConDatosUsuario(User user) {
        // Actualizar datos básicos
        String nombreCompleto = user.getName();
        if (user.getApellido() != null && !user.getApellido().isEmpty()) {
            nombreCompleto += " " + user.getApellido();
        }
        binding.tvNombreUsuario.setText(nombreCompleto);
        binding.collapsingToolbar.setTitle(nombreCompleto);

        // Rol de usuario
        binding.tvRolUsuario.setText(formatearRol(user.getRol()));

        // Información personal
        binding.tvEmail.setText(user.getEmail());
        binding.tvTelefono.setText(user.getTelefono() != null ? user.getTelefono() : "No especificado");

        // Fechas
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        if (user.getFechaRegistro() != null) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.US);
                Date fecha = inputFormat.parse(user.getFechaRegistro());
                binding.tvFechaRegistro.setText(outputFormat.format(fecha));
            } catch (Exception e) {
                binding.tvFechaRegistro.setText(user.getFechaRegistro());
            }
        } else {
            binding.tvFechaRegistro.setText("No disponible");
        }

        if (user.getUltimaConexion() != null) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.US);
                Date fecha = inputFormat.parse(user.getUltimaConexion());
                binding.tvUltimaConexion.setText(outputFormat.format(fecha));
            } catch (Exception e) {
                binding.tvUltimaConexion.setText(user.getUltimaConexion());
            }
        } else {
            binding.tvUltimaConexion.setText("No disponible");
        }

        // Foto de perfil
        if (user.getFotoPerfil() != null && !user.getFotoPerfil().isEmpty()) {
            ImageUtils.loadUserPhoto(binding.ivProfilePic, user.getFotoPerfil());
        }

        // Mostrar/ocultar botón de solicitar ser repartidor si es cliente
        if (Constants.ROL_CLIENTE.equals(user.getRol())) {
            binding.btnSolicitarSerRepartidor.setVisibility(View.VISIBLE);
        } else {
            binding.btnSolicitarSerRepartidor.setVisibility(View.GONE);
        }
    }

    private String formatearRol(String rol) {
        switch (rol) {
            case Constants.ROL_CLIENTE:
                return "Cliente";
            case Constants.ROL_REPARTIDOR:
                return "Repartidor";
            case Constants.ROL_ADMINISTRADOR:
                return "Administrador";
            default:
                return rol;
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            uploadProfileImage(imageUri);
        }
    }

    private void uploadProfileImage(Uri imageUri) {
        binding.progressBar.setVisibility(View.VISIBLE);

        // Usar el método en el ViewModel
        viewModel.uploadProfileImage(imageUri).observe(getViewLifecycleOwner(), result -> {
            binding.progressBar.setVisibility(View.GONE);

            if (result.status == Resource.Status.SUCCESS && result.data != null) {
                Toast.makeText(requireContext(), "Imagen de perfil actualizada", Toast.LENGTH_SHORT).show();
                actualizarUIConDatosUsuario(result.data);
            } else if (result.status == Resource.Status.ERROR) {
                Toast.makeText(requireContext(), "Error: " + result.message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarDialogoEditarPerfil() {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        DialogEditarPerfilBinding dialogBinding = DialogEditarPerfilBinding.inflate(getLayoutInflater());
        dialog.setContentView(dialogBinding.getRoot());

        // Obtener datos actuales del usuario
        User user = UserManager.getUser(requireContext());
        if (user != null) {
            dialogBinding.etNombre.setText(user.getName());
            dialogBinding.etApellido.setText(user.getApellido());
            dialogBinding.etTelefono.setText(user.getTelefono());
        }

        // Configurar botones
        dialogBinding.btnCerrar.setOnClickListener(v -> dialog.dismiss());

        dialogBinding.btnGuardar.setOnClickListener(v -> {
            if (validarFormularioEdicion(dialogBinding)) {
                guardarCambiosPerfil(dialogBinding, dialog);
            }
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private boolean validarFormularioEdicion(DialogEditarPerfilBinding binding) {
        boolean valido = true;

        if (TextUtils.isEmpty(binding.etNombre.getText())) {
            binding.tilNombre.setError("El nombre es requerido");
            valido = false;
        } else {
            binding.tilNombre.setError(null);
        }

        // El teléfono es opcional, pero si se proporciona, validar formato
        if (!TextUtils.isEmpty(binding.etTelefono.getText())) {
            String telefono = binding.etTelefono.getText().toString().trim();
            if (!telefono.matches("\\+?[0-9 ()-]{8,}")) {
                binding.tilTelefono.setError("Formato de teléfono inválido");
                valido = false;
            } else {
                binding.tilTelefono.setError(null);
            }
        }

        return valido;
    }

    private void guardarCambiosPerfil(DialogEditarPerfilBinding dialogBinding, Dialog dialog) {
        dialogBinding.progressBar.setVisibility(View.VISIBLE);
        dialogBinding.btnGuardar.setEnabled(false);

        String nombre = dialogBinding.etNombre.getText().toString().trim();
        String apellido = dialogBinding.etApellido.getText().toString().trim();
        String telefono = dialogBinding.etTelefono.getText().toString().trim();

        viewModel.updateProfile(nombre, apellido, telefono).observe(getViewLifecycleOwner(), result -> {
            dialogBinding.progressBar.setVisibility(View.GONE);
            dialogBinding.btnGuardar.setEnabled(true);

            if (result.status == Resource.Status.SUCCESS && result.data != null) {
                Toast.makeText(requireContext(), "Perfil actualizado con éxito", Toast.LENGTH_SHORT).show();
                actualizarUIConDatosUsuario(result.data);
                dialog.dismiss();
            } else if (result.status == Resource.Status.ERROR) {
                Toast.makeText(requireContext(), "Error: " + result.message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarDialogoCambiarContrasena() {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        DialogCambiarContrasenaBinding dialogBinding = DialogCambiarContrasenaBinding.inflate(getLayoutInflater());
        dialog.setContentView(dialogBinding.getRoot());

        // Configurar botones
        dialogBinding.btnCerrar.setOnClickListener(v -> dialog.dismiss());

        dialogBinding.btnGuardar.setOnClickListener(v -> {
            if (validarFormularioContrasena(dialogBinding)) {
                cambiarContrasena(dialogBinding, dialog);
            }
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private boolean validarFormularioContrasena(DialogCambiarContrasenaBinding binding) {
        boolean valido = true;

        // Validar contraseña actual
        if (TextUtils.isEmpty(binding.etContrasenaActual.getText())) {
            binding.tilContrasenaActual.setError("La contraseña actual es requerida");
            valido = false;
        } else {
            binding.tilContrasenaActual.setError(null);
        }

        // Validar nueva contraseña
        if (TextUtils.isEmpty(binding.etNuevaContrasena.getText())) {
            binding.tilNuevaContrasena.setError("La nueva contraseña es requerida");
            valido = false;
        } else if (binding.etNuevaContrasena.getText().length() < 8) {
            binding.tilNuevaContrasena.setError("La contraseña debe tener al menos 8 caracteres");
            valido = false;
        } else {
            binding.tilNuevaContrasena.setError(null);
        }

        // Validar confirmación
        if (TextUtils.isEmpty(binding.etConfirmarContrasena.getText())) {
            binding.tilConfirmarContrasena.setError("Debes confirmar la contraseña");
            valido = false;
        } else if (!binding.etNuevaContrasena.getText().toString()
                .equals(binding.etConfirmarContrasena.getText().toString())) {
            binding.tilConfirmarContrasena.setError("Las contraseñas no coinciden");
            valido = false;
        } else {
            binding.tilConfirmarContrasena.setError(null);
        }

        return valido;
    }

    private void cambiarContrasena(DialogCambiarContrasenaBinding dialogBinding, Dialog dialog) {
        dialogBinding.progressBar.setVisibility(View.VISIBLE);
        dialogBinding.btnGuardar.setEnabled(false);

        String contrasenaActual = dialogBinding.etContrasenaActual.getText().toString();
        String nuevaContrasena = dialogBinding.etNuevaContrasena.getText().toString();

        viewModel.changePassword(contrasenaActual, nuevaContrasena).observe(getViewLifecycleOwner(), result -> {
            dialogBinding.progressBar.setVisibility(View.GONE);
            dialogBinding.btnGuardar.setEnabled(true);

            if (result.status == Resource.Status.SUCCESS && result.data != null) {
                Toast.makeText(requireContext(), "Contraseña actualizada con éxito", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } else if (result.status == Resource.Status.ERROR) {
                Toast.makeText(requireContext(), "Error: " + result.message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarDialogoSolicitarRepartidor() {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        DialogSolicitarRepartidorBinding dialogBinding = DialogSolicitarRepartidorBinding.inflate(getLayoutInflater());
        dialog.setContentView(dialogBinding.getRoot());

        // Configurar botones
        dialogBinding.btnCancelar.setOnClickListener(v -> dialog.dismiss());

        dialogBinding.btnEnviarSolicitud.setOnClickListener(v -> {
            enviarSolicitudRepartidor(dialogBinding, dialog);
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private void enviarSolicitudRepartidor(DialogSolicitarRepartidorBinding dialogBinding, Dialog dialog) {
        dialogBinding.progressBar.setVisibility(View.VISIBLE);
        dialogBinding.btnEnviarSolicitud.setEnabled(false);
        dialogBinding.btnCancelar.setEnabled(false);

        viewModel.solicitarSerRepartidor().observe(getViewLifecycleOwner(), result -> {
            dialogBinding.progressBar.setVisibility(View.GONE);

            if (result.status == Resource.Status.SUCCESS && result.data != null) {
                Toast.makeText(requireContext(), "Solicitud enviada exitosamente", Toast.LENGTH_LONG).show();
                dialog.dismiss();
            } else if (result.status == Resource.Status.ERROR) {
                dialogBinding.btnEnviarSolicitud.setEnabled(true);
                dialogBinding.btnCancelar.setEnabled(true);
                Toast.makeText(requireContext(), "Error: " + result.message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmarCerrarSesion() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Cerrar Sesión")
                .setMessage("¿Estás seguro que deseas cerrar sesión?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    cerrarSesion();
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void cerrarSesion() {
        binding.progressBar.setVisibility(View.VISIBLE);

        viewModel.logout().observe(getViewLifecycleOwner(), result -> {
            binding.progressBar.setVisibility(View.GONE);

            // Independientemente del resultado, dirigir al login
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}