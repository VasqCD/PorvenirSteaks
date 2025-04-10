package com.example.porvenirsteaks;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.porvenirsteaks.api.ApiService;
import com.example.porvenirsteaks.api.RetrofitClient;
import com.example.porvenirsteaks.data.preferences.TokenManager;
import com.example.porvenirsteaks.data.preferences.UserManager;
import com.example.porvenirsteaks.databinding.ActivityMainBinding;
import com.example.porvenirsteaks.ui.auth.AuthViewModel;
import com.example.porvenirsteaks.ui.auth.LoginActivity;
import com.example.porvenirsteaks.utils.ImageUtils;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        setSupportActionBar(binding.appBarMain.toolbar);

        // Configurar el FAB como botón de carrito
        binding.appBarMain.fab.setImageResource(R.drawable.ic_shopping_cart);
        binding.appBarMain.fab.setOnClickListener(view -> {
            // Navegar al fragmento del carrito
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
            navController.navigate(R.id.carritoFragment);
        });

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
    }

    private void setupNavigationMenu(NavigationView navigationView) {
        Menu menu = navigationView.getMenu();

        // Mostrar/ocultar opciones según el rol del usuario
        String userRole = UserManager.getUserRole(this);

        if (menu.findItem(R.id.nav_repartidor_entregas) != null) {
            menu.findItem(R.id.nav_repartidor_entregas).setVisible(
                    userRole.equals("repartidor"));
        }

        // Si hay elementos específicos para administradores
        if (menu.findItem(R.id.nav_admin_dashboard) != null) {
            menu.findItem(R.id.nav_admin_dashboard).setVisible(
                    userRole.equals("administrador"));
        }
    }

    private void setupNavHeader(NavigationView navigationView) {
        View headerView = navigationView.getHeaderView(0);
        TextView tvName = headerView.findViewById(R.id.tv_user_name);
        TextView tvEmail = headerView.findViewById(R.id.tv_user_email);
        ImageView ivProfile = headerView.findViewById(R.id.iv_user_photo);

        // Obtener datos del usuario
        tvName.setText(UserManager.getUserName(this));
        tvEmail.setText(UserManager.getUserEmail(this));

        // Cargar foto de perfil si existe
        String photoUrl = UserManager.getUser(this) != null ?
                UserManager.getUser(this).getFotoPerfil() : null;

        if (photoUrl != null && !photoUrl.isEmpty()) {
            ImageUtils.loadUserPhoto(ivProfile, photoUrl);
        }
    }

    private void enviarTokenFcmPendiente() {
        SharedPreferences prefs = getSharedPreferences("FCM", MODE_PRIVATE);
        String pendingToken = prefs.getString("pending_token", null);

        if (pendingToken != null && TokenManager.hasToken(this)) {
            // Hay un token FCM pendiente y el usuario está autenticado, enviarlo al servidor
            ApiService apiService = RetrofitClient.getClient(TokenManager.getToken(this))
                    .create(ApiService.class);

            Map<String, String> request = new HashMap<>();
            request.put("token", pendingToken);
            request.put("device_type", "android");

            apiService.registerFcmToken(request).enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                    if (response.isSuccessful()) {
                        // Eliminar el token pendiente
                        prefs.edit().remove("pending_token").apply();
                    }
                }

                @Override
                public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                    // Mantener el token para intentar nuevamente después
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            logout();
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
            // Independientemente del resultado, cerrar la sesión localmente
            TokenManager.clearToken(this);
            UserManager.clearUser(this);

            // Ir a la pantalla de login
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}