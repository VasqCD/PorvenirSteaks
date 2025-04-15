package com.example.porvenirsteaks.ui.perfil;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.porvenirsteaks.R;
import com.example.porvenirsteaks.api.ApiService;
import com.example.porvenirsteaks.api.RetrofitClient;
import com.example.porvenirsteaks.data.model.User;
import com.example.porvenirsteaks.data.preferences.TokenManager;
import com.example.porvenirsteaks.data.preferences.UserManager;
import com.example.porvenirsteaks.databinding.DialogCambiarContrasenaBinding;
import com.example.porvenirsteaks.databinding.DialogEditarPerfilBinding;
import com.example.porvenirsteaks.databinding.DialogSolicitarRepartidorBinding;
import com.example.porvenirsteaks.databinding.FragmentPerfilBinding;
import com.example.porvenirsteaks.ui.auth.LoginActivity;
import com.example.porvenirsteaks.utils.Constants;
import com.example.porvenirsteaks.utils.DateUtils;
import com.example.porvenirsteaks.utils.ImageUtils;
import com.example.porvenirsteaks.utils.NetworkUtils;
import com.example.porvenirsteaks.utils.PermissionUtils;
import com.example.porvenirsteaks.utils.Resource;
import com.example.porvenirsteaks.utils.ToastUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.Manifest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PerfilFragment extends Fragment {
    private FragmentPerfilBinding binding;
    private PerfilViewModel viewModel;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;
    private static final int STORAGE_PERMISSION_REQUEST = 1001;
    private static final int REQUEST_IMAGE_PERMISSION = 101;
    private static final String[] IMAGE_PERMISSIONS = new String[] {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_MEDIA_IMAGES
    };

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
            checkAndRequestImagePermissions();
            Log.d("PerfilFragment", "Botón de editar foto presionado");
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

        // botón de prueba de notificaciones
        binding.btnTestNotification.setOnClickListener(v -> {
            testNotification();
        });
    }

    private void testNotification() {
        if (TokenManager.hasToken(requireContext())) {
            RetrofitClient.getClient(TokenManager.getToken(requireContext()))
                    .create(ApiService.class)
                    .testNotification()
                    .enqueue(new Callback<Map<String, Object>>() {
                        @Override
                        public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(requireContext(), "Notificación de prueba enviada", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(requireContext(), "Error al enviar notificación de prueba", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                            Toast.makeText(requireContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void checkAndRequestImagePermissions() {
        // Determinar qué permisos solicitar según la versión de Android
        List<String> permissionsNeeded = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ requiere READ_MEDIA_IMAGES
            permissionsNeeded.add(Manifest.permission.READ_MEDIA_IMAGES);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11-12 puede usar READ_EXTERNAL_STORAGE
            permissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        } else {
            // Android 10 y anteriores
            permissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            permissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        String[] permissions = permissionsNeeded.toArray(new String[0]);

        // Verificar si ya tenemos los permisos
        boolean allPermissionsGranted = true;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(requireContext(), permission)
                    != PackageManager.PERMISSION_GRANTED) {
                allPermissionsGranted = false;
                break;
            }
        }

        if (allPermissionsGranted) {
            // Ya tenemos todos los permisos
            openImagePicker();
        } else {
            // Solicitar permisos
            requestPermissions(permissions, REQUEST_IMAGE_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_IMAGE_PERMISSION) {
            // Verifica si TODOS los permisos fueron concedidos
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                // Todos los permisos fueron otorgados
                openImagePicker();
            } else {
                // Verifica qué permisos fueron denegados permanentemente
                boolean somePermissionPermanentlyDenied = false;
                for (String permission : permissions) {
                    if (!shouldShowRequestPermissionRationale(permission)) {
                        somePermissionPermanentlyDenied = true;
                        break;
                    }
                }

                if (somePermissionPermanentlyDenied) {
                    // Usuario eligió "No volver a preguntar" para al menos un permiso
                    PermissionUtils.showPermissionDeniedDialog(
                            requireContext(),
                            "Para cambiar tu foto de perfil, necesitas conceder acceso a tus imágenes. Puedes habilitarlo en la configuración de la aplicación."
                    );
                } else {
                    Toast.makeText(requireContext(), "Necesitamos estos permisos para cambiar tu foto de perfil.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void cargarDatosPerfil() {
        binding.progressBar.setVisibility(View.VISIBLE);

        // Primero intenta cargar desde caché local
        User cachedUser = UserManager.getUser(requireContext());
        if (cachedUser != null) {
            Log.d("PerfilFragment", "Cargando datos desde caché");
            actualizarUIConDatosUsuario(cachedUser);
        }

        // Luego intenta actualizar desde el servidor solo si hay conexión
        if (NetworkUtils.isNetworkAvailable(requireContext())) {
            viewModel.getUserProfile().observe(getViewLifecycleOwner(), result -> {
                binding.progressBar.setVisibility(View.GONE);

                if (result.status == Resource.Status.SUCCESS && result.data != null) {
                    Log.d("PerfilFragment", "Datos actualizados desde el servidor");
                    actualizarUIConDatosUsuario(result.data);
                } else if (result.status == Resource.Status.ERROR) {
                    Log.e("PerfilFragment", "Error cargando datos: " + result.message);

                    if (cachedUser == null) {
                        // Solo mostrar error si no teníamos datos en caché
                        Toast.makeText(requireContext(), "Error al cargar datos de perfil: " + result.message, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            binding.progressBar.setVisibility(View.GONE);
            if (cachedUser == null) {
                // No hay caché y tampoco hay red
                Toast.makeText(requireContext(), "No hay conexión a internet ni datos guardados", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void actualizarUIConDatosUsuario(User user) {
        try {
            if (user == null) {
                Log.e("PerfilFragment", "Usuario nulo en actualizarUIConDatosUsuario");
                // Intentar recuperar usuario desde preferences si es nulo
                user = UserManager.getUser(requireContext());
                if (user == null) {
                    return; // No podemos hacer nada sin usuario
                }
            }

            Log.d("PerfilFragment", "Actualizando UI con datos: Usuario=" + user +
                    ", Nombre=" + (user.getName() != null ? user.getName() : "null") +
                    ", Email=" + (user.getEmail() != null ? user.getEmail() : "null") +
                    ", Rol=" + (user.getRol() != null ? user.getRol() : "null"));

            // Verificar datos importantes y usar valores predeterminados si son nulos
            String nombre = user.getName();
            String email = user.getEmail();
            String rol = user.getRol();
            String apellido = user.getApellido();
            String telefono = user.getTelefono();
            String fechaRegistro = user.getFechaRegistro();
            String ultimaConexion = user.getUltimaConexion();
            String fotoPerfil = user.getFotoPerfil();

            // Intentar recuperar valores faltantes del usuario guardado
            if (nombre == null || email == null || rol == null) {
                User storedUser = UserManager.getUser(requireContext());
                if (storedUser != null) {
                    if (nombre == null) nombre = storedUser.getName();
                    if (apellido == null) apellido = storedUser.getApellido();
                    if (email == null) email = storedUser.getEmail();
                    if (rol == null) rol = storedUser.getRol();
                    if (telefono == null) telefono = storedUser.getTelefono();
                    if (fechaRegistro == null) fechaRegistro = storedUser.getFechaRegistro();
                    if (ultimaConexion == null) ultimaConexion = storedUser.getUltimaConexion();
                    if (fotoPerfil == null) fotoPerfil = storedUser.getFotoPerfil();
                }
            }

            // Actualizar datos básicos
            String nombreCompleto = (nombre != null) ? nombre : "";
            if (apellido != null && !apellido.isEmpty()) {
                nombreCompleto += " " + apellido;
            }
            binding.tvNombreUsuario.setText(nombreCompleto);
            binding.collapsingToolbar.setTitle(nombreCompleto);

            // Rol de usuario
            binding.tvRolUsuario.setText(formatearRol(rol));

            // Información personal
            binding.tvEmail.setText(email != null ? email : "No especificado");
            binding.tvTelefono.setText(telefono != null ? telefono : "No especificado");

            // Fechas usando nuestra nueva utilidad
            binding.tvFechaRegistro.setText(
                    fechaRegistro != null ?
                            DateUtils.formatDateString(fechaRegistro, "dd/MM/yyyy") :
                            "No disponible"
            );

            binding.tvUltimaConexion.setText(
                    ultimaConexion != null ?
                            DateUtils.formatDateString(ultimaConexion, "dd/MM/yyyy") :
                            "No disponible"
            );

            // Foto de perfil
            if (fotoPerfil != null && !fotoPerfil.isEmpty()) {
                Log.d("PerfilFragment", "Cargando foto de perfil: " + fotoPerfil);
                ImageUtils.loadUserPhoto(binding.ivProfilePic, fotoPerfil);
            }

            // Mostrar/ocultar botón de solicitar ser repartidor si es cliente
            if (Constants.ROL_CLIENTE.equals(rol)) {
                binding.btnSolicitarSerRepartidor.setVisibility(View.VISIBLE);
            } else {
                binding.btnSolicitarSerRepartidor.setVisibility(View.GONE);
            }

            Log.d("PerfilFragment", "UI actualizada correctamente");
        } catch (Exception e) {
            Log.e("PerfilFragment", "Error actualizando UI del perfil: " + e.getMessage(), e);
        }
    }

    private String formatearRol(String rol) {
        // Verificar si el rol es nulo
        if (rol == null) {
            Log.w("PerfilFragment", "Rol es nulo en formatearRol");
            return "Cliente"; // Valor predeterminado
        }

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
        boolean permissionGranted;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // En Android 13 y superiores
            permissionGranted = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES)
                    == PackageManager.PERMISSION_GRANTED;
        } else {
            // En versiones anteriores a Android 13
            permissionGranted = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        }

        if (!permissionGranted) {
            // Vuelve a solicitar permisos si faltan (o simplemente llama a checkAndRequestImagePermissions() si preferís)
            checkAndRequestImagePermissions();
            return;
        }

        // Abrir el selector de imágenes
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
                // Mostrar mensaje de éxito
                Toast.makeText(requireContext(), "Imagen de perfil actualizada", Toast.LENGTH_SHORT).show();

                // Obtener los datos más recientes
                User updatedUser = result.data;

                if (updatedUser.getFotoPerfil() != null && !updatedUser.getFotoPerfil().isEmpty()) {
                    String photoUrl = updatedUser.getFotoPerfil();
                    String fullUrl;
                    if (!photoUrl.startsWith("http")) {
                        fullUrl = Constants.BASE_IMAGE_URL + photoUrl;
                    } else {
                        fullUrl = photoUrl;
                    }

                    Log.d("PerfilFragment", "Nueva foto de perfil URL: " + fullUrl);

                    // Limpiar la caché específicamente para esta imagen
                    try {
                        // Limpiar las cachés de Glide
                        Glide.get(requireContext()).clearMemory();
                        new Thread(() -> Glide.get(requireContext()).clearDiskCache()).start();

                        // Método alternativo: recargar completamente la foto
                        binding.ivProfilePic.post(() -> {
                            // Reiniciar la imagen primero con el placeholder
                            binding.ivProfilePic.setImageResource(R.drawable.user_placeholder);

                            // Esperar un breve momento y luego cargar la nueva imagen
                            new Handler().postDelayed(() -> {
                                // Cargar la imagen directamente sin pasar por nuestro método loadUserPhoto
                                Glide.with(requireContext())
                                        .load(fullUrl + "?t=" + System.currentTimeMillis()) // Agregar query param para forzar recarga
                                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                                        .skipMemoryCache(true)
                                        .placeholder(R.drawable.user_placeholder)
                                        .error(R.drawable.user_placeholder)
                                        .circleCrop()
                                        .into(binding.ivProfilePic);
                            }, 300); // Un poco más de tiempo de retraso
                        });
                    } catch (Exception e) {
                        Log.e("PerfilFragment", "Error al cargar nueva imagen: " + e.getMessage(), e);
                    }
                }

                // Actualizar el resto de la UI
                actualizarUIConDatosUsuario(updatedUser);
            } else if (result.status == Resource.Status.ERROR) {
                Toast.makeText(requireContext(), "Error: " + result.message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserImage(String photoUrl) {
        try {
            if (photoUrl != null && !photoUrl.isEmpty()) {
                ImageUtils.loadUserPhoto(binding.ivProfilePic, photoUrl);
            }
        } catch (Exception e) {
            Log.e("PerfilFragment", "Error al cargar imagen", e);
            // No hacer nada más, continuar con la UI
        }
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
                // Primero cerrar el diálogo
                dialog.dismiss();

                // Luego actualizar la UI con los datos actualizados
                Toast.makeText(requireContext(), "Perfil actualizado con éxito", Toast.LENGTH_SHORT).show();

                // Obtener el usuario más reciente de preferences (por si acaso)
                User updatedUser = result.data;

                // Actualizar UI con datos actualizados
                Log.d("PerfilFragment", "Actualizando UI después de guardar cambios: " +
                        "ID=" + updatedUser.getId() +
                        ", Nombre=" + updatedUser.getName() +
                        ", Email=" + updatedUser.getEmail() +
                        ", Rol=" + updatedUser.getRol());

                actualizarUIConDatosUsuario(updatedUser);
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

            if (result.status == Resource.Status.SUCCESS) {
                ToastUtils.showSuccessToast(requireContext(), "Solicitud enviada exitosamente");
                dialog.dismiss();

                // Mostrar diálogo informativo
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Solicitud Enviada")
                        .setMessage("Tu solicitud para ser repartidor ha sido enviada con éxito. Nos pondremos en contacto contigo pronto para los siguientes pasos.")
                        .setPositiveButton("Entendido", null)
                        .show();

            } else if (result.status == Resource.Status.ERROR) {
                dialogBinding.btnEnviarSolicitud.setEnabled(true);
                dialogBinding.btnCancelar.setEnabled(true);
                ToastUtils.showErrorToast(requireContext(), "Error: " + result.message);
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