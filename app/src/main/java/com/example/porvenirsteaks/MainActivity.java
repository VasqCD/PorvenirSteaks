package com.example.porvenirsteaks;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.porvenirsteaks.api.ApiService;
import com.example.porvenirsteaks.api.RetrofitClient;
import com.example.porvenirsteaks.data.model.Ubicacion;
import com.example.porvenirsteaks.data.preferences.TokenManager;
import com.example.porvenirsteaks.data.preferences.UserManager;
import com.example.porvenirsteaks.databinding.ActivityMainBinding;
import com.example.porvenirsteaks.ui.auth.AuthViewModel;
import com.example.porvenirsteaks.ui.auth.LoginActivity;
import com.example.porvenirsteaks.ui.ubicaciones.DireccionConfirmationActivity;
import com.example.porvenirsteaks.utils.ImageUtils;
import com.example.porvenirsteaks.utils.LocationPermissionHandler;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_PERMISSIONS = 1000;
    private static final String TAG = "MainActivity";
    private static final String PREF_NAME = "AppPreferences";
    private static final String KEY_FIRST_LOGIN = "first_login";
    private static final String KEY_PERMISOS_SOLICITADOS = "permisos_solicitados";

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private AuthViewModel authViewModel;
    private LocationPermissionHandler permissionHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        permissionHandler = new LocationPermissionHandler(this);

        setSupportActionBar(binding.appBarMain.toolbar);

        // Ocultar el título de la app en la barra superior
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        // Configurar el menú de navegación lateral según el rol del usuario
        setupNavigationMenu(navigationView);

        // Configurar la navegación
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_productos, R.id.nav_pedidos,
                R.id.nav_ubicaciones, R.id.nav_notificaciones, R.id.nav_perfil)
                .setOpenableLayout(drawer)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // Configurar el header del navigation drawer
        setupNavHeader(navigationView);

        // Verificar si hay un token FCM pendiente por enviar
        enviarTokenFcmPendiente();

        // Verificar y solicitar los permisos necesarios
        verificarPermisos();

        // Verificar si es el primer inicio de sesión y la existencia de direcciones
        verificarPrimerInicio();
    }

    private void setupNavigationMenu(NavigationView navigationView) {
        Menu menu = navigationView.getMenu();
        String userRole = UserManager.getUserRole(this);

        if (userRole != null) {
            if (menu.findItem(R.id.nav_repartidor_entregas) != null) {
                menu.findItem(R.id.nav_repartidor_entregas).setVisible(userRole.equals("repartidor"));
            }
            if (menu.findItem(R.id.nav_admin_dashboard) != null) {
                menu.findItem(R.id.nav_admin_dashboard).setVisible(userRole.equals("administrador"));
            }
        } else {
            if (menu.findItem(R.id.nav_repartidor_entregas) != null) {
                menu.findItem(R.id.nav_repartidor_entregas).setVisible(false);
            }
            if (menu.findItem(R.id.nav_admin_dashboard) != null) {
                menu.findItem(R.id.nav_admin_dashboard).setVisible(false);
            }
        }
    }

    private void setupNavHeader(NavigationView navigationView) {
        View headerView = navigationView.getHeaderView(0);
        TextView tvName = headerView.findViewById(R.id.tv_user_name);
        TextView tvEmail = headerView.findViewById(R.id.tv_user_email);
        ImageView ivProfile = headerView.findViewById(R.id.iv_user_photo);

        tvName.setText(UserManager.getUserName(this));
        tvEmail.setText(UserManager.getUserEmail(this));

        String photoUrl = (UserManager.getUser(this) != null ? UserManager.getUser(this).getFotoPerfil() : null);
        if (photoUrl != null && !photoUrl.isEmpty()) {
            ImageUtils.loadUserPhoto(ivProfile, photoUrl);
        }
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
                        prefs.edit().remove("pending_token").apply();
                    }
                }

                @Override
                public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                    // Mantener el token para intentar nuevamente más tarde
                }
            });
        }
    }

    /**
     * Verifica si es el primer inicio de sesión del usuario y posteriormente revisa si tiene direcciones registradas.
     */
    private void verificarPrimerInicio() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        boolean isPrimerInicio = prefs.getBoolean(KEY_FIRST_LOGIN, true);

        if (isPrimerInicio) {
            // Marcar que ya no es primer inicio
            prefs.edit().putBoolean(KEY_FIRST_LOGIN, false).apply();
            // Verificar direcciones en primer inicio
            verificarDireccionesUsuario(true);
        } else {
            // Verificar direcciones aunque no sea el primer inicio
            verificarDireccionesUsuario(false);
        }
    }

    /**
     * Consulta las direcciones registradas para el usuario mediante la API.
     * Si no hay direcciones, redirige a la pantalla de confirmación de dirección.
     *
     * @param isPrimerInicio indica si es el primer inicio de sesión
     */
    private void verificarDireccionesUsuario(boolean isPrimerInicio) {
        binding.appBarMain.contentMain.progressBarMain.setVisibility(View.VISIBLE);

        String token = TokenManager.getToken(this);
        if (token == null) {
            irALogin();
            return;
        }

        ApiService apiService = RetrofitClient.getClient(token).create(ApiService.class);
        apiService.getUbicaciones().enqueue(new Callback<List<Ubicacion>>() {
            @Override
            public void onResponse(Call<List<Ubicacion>> call, Response<List<Ubicacion>> response) {
                binding.appBarMain.contentMain.progressBarMain.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isEmpty()) {
                        Log.d(TAG, "El usuario no tiene direcciones registradas");
                        Intent intent = new Intent(MainActivity.this, DireccionConfirmationActivity.class);
                        intent.putExtra("isFirstLogin", isPrimerInicio);
                        startActivity(intent);
                    } else {
                        Log.d(TAG, "El usuario tiene " + response.body().size() + " direcciones registradas");
                    }
                } else {
                    Log.e(TAG, "Error al obtener ubicaciones: " + response.message());
                    if (response.code() == 401) {
                        TokenManager.clearToken(MainActivity.this);
                        irALogin();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Ubicacion>> call, Throwable t) {
                binding.appBarMain.contentMain.progressBarMain.setVisibility(View.GONE);
                Log.e(TAG, "Error al verificar ubicaciones", t);
                Toast.makeText(MainActivity.this, "Error al verificar direcciones: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Verifica y solicita los permisos necesarios para la aplicación.
     */
    private void verificarPermisos() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        boolean permisosYaSolicitados = prefs.getBoolean(KEY_PERMISOS_SOLICITADOS, false);

        if (!permisosYaSolicitados) {
            permissionHandler.requestAllPermissions(allGranted -> {
                prefs.edit().putBoolean(KEY_PERMISOS_SOLICITADOS, true).apply();
                if (allGranted) {
                    Log.d(TAG, "Todos los permisos concedidos");
                } else {
                    Log.d(TAG, "No se concedieron todos los permisos");
                    Snackbar.make(
                            binding.getRoot(),
                            "Algunas funciones pueden no estar disponibles sin los permisos necesarios",
                            Snackbar.LENGTH_LONG
                    ).setAction("Configuración", v -> {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.setData(Uri.parse("package:" + getPackageName()));
                        startActivity(intent);
                    }).show();
                }
            });
        }
    }

    /**
     * Redirige al usuario a la pantalla de login.
     */
    private void irALogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        permissionHandler.handleActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionHandler.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            logout();
            return true;
        } else if (id == R.id.action_cart) {
            // Navegar al fragmento del carrito
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
            navController.navigate(R.id.carritoFragment);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private void logout() {
        authViewModel.logout().observe(this, result -> {
            TokenManager.clearToken(this);
            UserManager.clearUser(this);
            getSharedPreferences(PREF_NAME, MODE_PRIVATE)
                    .edit()
                    .putBoolean(KEY_FIRST_LOGIN, true)
                    .apply();
            irALogin();
        });
    }
}
