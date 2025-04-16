package com.example.porvenirsteaks.ui.home;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager2.widget.ViewPager2;

import com.example.porvenirsteaks.R;
import com.example.porvenirsteaks.data.model.Categoria;
import com.example.porvenirsteaks.data.model.Pedido;
import com.example.porvenirsteaks.data.model.Producto;
import com.example.porvenirsteaks.data.model.Repartidor;
import com.example.porvenirsteaks.data.model.Ubicacion;
import com.example.porvenirsteaks.databinding.FragmentHomeBinding;
import com.example.porvenirsteaks.ui.home.adapters.CategoriasAdapter;
import com.example.porvenirsteaks.ui.home.adapters.EntregaAdapter;
import com.example.porvenirsteaks.ui.home.adapters.PedidoAdapter;
import com.example.porvenirsteaks.ui.home.adapters.ProductoAdapter;
import com.example.porvenirsteaks.ui.home.adapters.RepartidorAdapter;
import com.example.porvenirsteaks.utils.Constants;
import com.example.porvenirsteaks.utils.LocationPermissionHandler;
import com.example.porvenirsteaks.utils.MapaManager;
import com.example.porvenirsteaks.utils.Resource;
import com.google.android.material.tabs.TabLayoutMediator;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment implements BannerAdapter.OnBannerClickListener {

    private FragmentHomeBinding binding;
    private HomeViewModel homeViewModel;

    // Adaptadores
    private BannerAdapter bannerAdapter;
    private CategoriasAdapter categoriasAdapter;
    private ProductoAdapter productosRecomendadosAdapter;
    private PedidoAdapter ultimosPedidosAdapter;
    private EntregaAdapter entregasPendientesAdapter;
    private PedidoAdapter pedidosRecientesAdapter;
    private RepartidorAdapter repartidoresAdapter;

    // Para gestionar mapa y ubicación
    private MapaManager mapaManager;
    private LocationPermissionHandler locationPermissionHandler;
    private static final int LOCATION_UPDATE_INTERVAL = 60000; // 1 minuto
    private Handler locationUpdateHandler = new Handler(Looper.getMainLooper());
    private Runnable locationUpdateRunnable;

    // Formatter para números
    private NumberFormat currencyFormatter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("es", "HN"));

        // Inicializar el controlador de permisos
        locationPermissionHandler = new LocationPermissionHandler(requireActivity());

        // Inicializar adaptadores
        setupAdapters();

        // Configurar interfaz según el rol
        homeViewModel.getUserRole().observe(getViewLifecycleOwner(), userRole -> {
            setupUIForRole(userRole);

            // Si es repartidor, iniciar actualización de ubicación
            if (Constants.ROL_REPARTIDOR.equals(userRole)) {
                checkLocationPermissionAndStartUpdates();
            }
        });

        // Cargar elementos comunes
        loadCommonElements();
    }

    private void setupAdapters() {
        // Banner
        bannerAdapter = new BannerAdapter(this);

        // Categorías
        categoriasAdapter = new CategoriasAdapter(categoria -> {
            // Navegar a productos filtrados por categoría
            Bundle args = new Bundle();
            args.putInt("categoria_id", categoria.getId());
            Navigation.findNavController(requireView()).navigate(R.id.nav_productos, args);
        });

        // Productos recomendados
        productosRecomendadosAdapter = new ProductoAdapter(producto -> {
            // Navegar al detalle del producto
            Bundle args = new Bundle();
            args.putInt("producto_id", producto.getId());
            Navigation.findNavController(requireView()).navigate(R.id.detalleProductoFragment, args);
        });

        // Últimos pedidos
        ultimosPedidosAdapter = new PedidoAdapter(pedido -> {
            // Navegar al detalle del pedido
            Bundle args = new Bundle();
            args.putInt("pedido_id", pedido.getId());
            Navigation.findNavController(requireView()).navigate(R.id.detallePedidoFragment, args);
        });

        // Entregas pendientes
        entregasPendientesAdapter = new EntregaAdapter(pedido -> {
            // Navegar al detalle de la entrega
            Bundle args = new Bundle();
            args.putInt("pedido_id", pedido.getId());
            Navigation.findNavController(requireView()).navigate(R.id.detalleEntregaFragment, args);
        });

        // Pedidos recientes (admin)
        pedidosRecientesAdapter = new PedidoAdapter(pedido -> {
            // Navegar al detalle del pedido
            Bundle args = new Bundle();
            args.putInt("pedido_id", pedido.getId());
            Navigation.findNavController(requireView()).navigate(R.id.detallePedidoFragment, args);
        });

        // Repartidores
        repartidoresAdapter = new RepartidorAdapter(repartidor -> {
            // Acción al seleccionar un repartidor
            Toast.makeText(requireContext(), "Seleccionado: " + repartidor.getUsuario().getName(), Toast.LENGTH_SHORT).show();
        });
    }

    private void loadCommonElements() {
        // Cargar saludo
        homeViewModel.getGreeting().observe(getViewLifecycleOwner(), greeting -> {
            binding.tvSaludo.setText(greeting);
        });

        // Cargar fecha
        homeViewModel.getCurrentDate().observe(getViewLifecycleOwner(), date -> {
            binding.tvFecha.setText(date);
        });
    }

    private void setupUIForRole(String userRole) {
        // Ocultar todas las vistas específicas de rol
        binding.layoutCliente.getRoot().setVisibility(View.GONE);
        binding.layoutRepartidor.getRoot().setVisibility(View.GONE);
        binding.layoutAdmin.getRoot().setVisibility(View.GONE);

        // Mostrar la vista según el rol
        switch (userRole) {
            case Constants.ROL_CLIENTE:
                setupClienteUI();
                binding.layoutCliente.getRoot().setVisibility(View.VISIBLE);
                break;
            case Constants.ROL_REPARTIDOR:
                setupRepartidorUI();
                binding.layoutRepartidor.getRoot().setVisibility(View.VISIBLE);
                break;
            case Constants.ROL_ADMINISTRADOR:
                setupAdminUI();
                binding.layoutAdmin.getRoot().setVisibility(View.VISIBLE);
                break;
        }
    }

    // CONFIGURACIÓN DE UI PARA ROL CLIENTE

    private void setupClienteUI() {
        // Configurar banner
        setupBanner();

        // Configurar categorías
        setupCategorias();

        // Configurar productos recomendados
        setupProductosRecomendados();

        // Configurar ubicación actual
        setupUbicacionActual();

        // Configurar pedido activo
        setupPedidoActivo();

        // Configurar últimos pedidos
        setupUltimosPedidos();

        // Configurar botones de acción
        setupClienteActions();
    }

    private void setupBanner() {
        binding.layoutCliente.bannerViewPager.setAdapter(bannerAdapter);

        // Establecer indicador de página
        new TabLayoutMediator(binding.layoutCliente.bannerIndicator, binding.layoutCliente.bannerViewPager,
                (tab, position) -> {
                    // No necesitamos configurar nada aquí, solo conectar el indicador
                }).attach();

        // Cargar banners
        homeViewModel.getBannerItems().observe(getViewLifecycleOwner(), bannerItems -> {
            bannerAdapter.setBannerItems(bannerItems);
        });
    }

    private void setupCategorias() {
        binding.layoutCliente.recyclerViewCategorias.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.layoutCliente.recyclerViewCategorias.setAdapter(categoriasAdapter);

        homeViewModel.getCategorias().observe(getViewLifecycleOwner(), resource -> {
            if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                categoriasAdapter.setCategorias(resource.data);
            } else if (resource.status == Resource.Status.ERROR) {
                Toast.makeText(requireContext(), "Error al cargar categorías: " + resource.message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupProductosRecomendados() {
        binding.layoutCliente.recyclerViewRecomendados.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.layoutCliente.recyclerViewRecomendados.setAdapter(productosRecomendadosAdapter);

        homeViewModel.getProductosRecomendados().observe(getViewLifecycleOwner(), resource -> {
            if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                List<Producto> productos = resource.data;
                // Limitar a los primeros 5 productos
                if (productos.size() > 5) {
                    productos = productos.subList(0, 5);
                }
                productosRecomendadosAdapter.setProductos(productos);
            } else if (resource.status == Resource.Status.ERROR) {
                Toast.makeText(requireContext(), "Error al cargar productos: " + resource.message, Toast.LENGTH_SHORT).show();
            }
        });

        // Configurar acción "Ver todos"
        binding.layoutCliente.tvVerTodosRecomendados.setOnClickListener(v -> {
            Navigation.findNavController(requireView()).navigate(R.id.nav_productos);
        });
    }

    private void setupUbicacionActual() {
        homeViewModel.getUbicacionActual().observe(getViewLifecycleOwner(), resource -> {
            if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                Ubicacion ubicacion = resource.data;
                binding.layoutCliente.tvDireccionActual.setText(ubicacion.getDireccionCompleta());
            } else {
                binding.layoutCliente.tvDireccionActual.setText("No hay dirección configurada");
            }
        });

        // Configurar botón para cambiar dirección
        binding.layoutCliente.btnCambiarDireccion.setOnClickListener(v -> {
            Navigation.findNavController(requireView()).navigate(R.id.nav_ubicaciones);
        });
    }

    private void setupPedidoActivo() {
        homeViewModel.getPedidoActivo().observe(getViewLifecycleOwner(), resource -> {
            if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                Pedido pedido = resource.data;
                binding.layoutCliente.cardPedidoActual.setVisibility(View.VISIBLE);

                // Configurar detalles del pedido activo
                binding.layoutCliente.tvPedidoId.setText("Pedido #" + pedido.getId());
                binding.layoutCliente.tvEstadoPedido.setText(formatEstadoPedido(pedido.getEstado()));

                // Configurar barra de progreso según el estado
                int progress = getProgressByEstado(pedido.getEstado());
                binding.layoutCliente.progressBarPedido.setProgress(progress);

                // Configurar acción del botón
                binding.layoutCliente.btnVerDetallePedido.setOnClickListener(v -> {
                    Bundle args = new Bundle();
                    args.putInt("pedido_id", pedido.getId());
                    Navigation.findNavController(requireView()).navigate(R.id.detallePedidoFragment, args);
                });
            } else {
                binding.layoutCliente.cardPedidoActual.setVisibility(View.GONE);
            }
        });
    }

    private void setupUltimosPedidos() {
        binding.layoutCliente.recyclerViewUltimosPedidos.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.layoutCliente.recyclerViewUltimosPedidos.setAdapter(ultimosPedidosAdapter);

        homeViewModel.getUltimosPedidos().observe(getViewLifecycleOwner(), resource -> {
            if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                List<Pedido> pedidos = resource.data;
                // Limitar a los últimos 3 pedidos
                if (pedidos.size() > 3) {
                    pedidos = pedidos.subList(0, 3);
                }
                ultimosPedidosAdapter.setPedidos(pedidos);
            } else if (resource.status == Resource.Status.ERROR) {
                Toast.makeText(requireContext(), "Error al cargar pedidos: " + resource.message, Toast.LENGTH_SHORT).show();
            }
        });

        // Configurar acción "Ver todos"
        binding.layoutCliente.tvVerTodosPedidos.setOnClickListener(v -> {
            Navigation.findNavController(requireView()).navigate(R.id.nav_pedidos);
        });
    }

    private void setupClienteActions() {
        // Aquí podrían configurarse acciones adicionales específicas para el cliente
    }

    // CONFIGURACIÓN DE UI PARA ROL REPARTIDOR

    private void setupRepartidorUI() {
        // Configurar switch de disponibilidad
        setupDisponibilidadSwitch();

        // Configurar entregas pendientes
        setupEntregasPendientes();

        // Configurar vista previa del mapa
        setupMapaPreview();
    }

    private void setupDisponibilidadSwitch() {
        // Iniciar con el estado de carga
        binding.layoutRepartidor.switchDisponibilidad.setEnabled(false);

        // Observar cambios en la disponibilidad
        homeViewModel.getDisponibilidad().observe(getViewLifecycleOwner(), disponible -> {
            binding.layoutRepartidor.switchDisponibilidad.setChecked(disponible);
            binding.layoutRepartidor.switchDisponibilidad.setEnabled(true);

            // Actualizar texto según disponibilidad
            if (disponible) {
                binding.layoutRepartidor.tvEstadoDisponibilidad.setText("Estás disponible para recibir entregas");
                binding.layoutRepartidor.tvEstadoDisponibilidad.setTextColor(requireContext().getColor(R.color.estado_en_camino));
            } else {
                binding.layoutRepartidor.tvEstadoDisponibilidad.setText("No estás disponible para recibir entregas");
                binding.layoutRepartidor.tvEstadoDisponibilidad.setTextColor(requireContext().getColor(R.color.estado_cancelado));
            }
        });

        // Configurar cambio de estado con mejor manejo de errores
        binding.layoutRepartidor.switchDisponibilidad.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Evitar ciclos infinitos por la actualización desde LiveData
            buttonView.setOnCheckedChangeListener(null);

            // Deshabilitar temporalmente
            buttonView.setEnabled(false);

            // Mostrar progreso
            Toast.makeText(requireContext(), "Actualizando disponibilidad...", Toast.LENGTH_SHORT).show();

            // Llamar al ViewModel para actualizar
            homeViewModel.setDisponibilidad(isChecked);

            // Restaurar el listener después de un breve retraso
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                buttonView.setEnabled(true);
                buttonView.setOnCheckedChangeListener((b, checked) ->
                        homeViewModel.setDisponibilidad(checked));
            }, 1000);
        });
    }

    private void setupEntregasPendientes() {
        binding.layoutRepartidor.recyclerViewEntregasPendientes.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.layoutRepartidor.recyclerViewEntregasPendientes.setAdapter(entregasPendientesAdapter);

        homeViewModel.getEntregasPendientes().observe(getViewLifecycleOwner(), resource -> {
            if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                List<Pedido> pedidos = resource.data;
                if (pedidos.isEmpty()) {
                    binding.layoutRepartidor.tvEmptyStateRepartidor.setVisibility(View.VISIBLE);
                } else {
                    binding.layoutRepartidor.tvEmptyStateRepartidor.setVisibility(View.GONE);
                    entregasPendientesAdapter.setPedidos(pedidos);
                }
            } else if (resource.status == Resource.Status.ERROR) {
                Toast.makeText(requireContext(), "Error al cargar entregas: " + resource.message, Toast.LENGTH_SHORT).show();
            }
        });

        // Configurar acción "Ver todas"
        binding.layoutRepartidor.tvVerTodasEntregas.setOnClickListener(v -> {
            Navigation.findNavController(requireView()).navigate(R.id.nav_repartidor_entregas);
        });
    }

    private void setupMapaPreview() {
        // Inicializar el gestor de mapa
        mapaManager = new MapaManager(requireContext(), binding.layoutRepartidor.frameMapPreview);

        // Observar las entregas pendientes para mostrarlas en el mapa
        homeViewModel.getEntregasPendientes().observe(getViewLifecycleOwner(), resource -> {
            if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                mapaManager.setPedidos(resource.data);
            }
        });

        // Configurar acción "Ver mapa completo"
        binding.layoutRepartidor.tvVerMapaCompleto.setOnClickListener(v -> {
            // Aquí podrías navegar a un fragmento con un mapa de pantalla completa
            Toast.makeText(requireContext(), "Próximamente: Mapa completo", Toast.LENGTH_SHORT).show();
        });
    }

    // CONFIGURACIÓN DE UI PARA ROL ADMINISTRADOR

    private void setupAdminUI() {
        // Configurar estadísticas del negocio
        setupEstadisticasNegocio();

        // Configurar pedidos recientes
        setupPedidosRecientes();

        // Configurar repartidores activos
        setupRepartidoresActivos();

        // Configurar botones de acción rápida
        setupAdminActions();
    }

    private void setupEstadisticasNegocio() {
        homeViewModel.getEstadisticasNegocio().observe(getViewLifecycleOwner(), stats -> {
            binding.layoutAdmin.tvVentasHoy.setText(currencyFormatter.format(stats.getVentasHoy()));
            binding.layoutAdmin.tvPedidosHoy.setText(String.valueOf(stats.getPedidosHoy()));
            binding.layoutAdmin.tvPedidosPendientes.setText(String.valueOf(stats.getPedidosPendientes()));
            binding.layoutAdmin.tvRepartidoresActivos.setText(String.valueOf(stats.getRepartidoresActivos()));
        });
    }

    private void setupPedidosRecientes() {
        binding.layoutAdmin.recyclerViewPedidosRecientes.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.layoutAdmin.recyclerViewPedidosRecientes.setAdapter(pedidosRecientesAdapter);

        homeViewModel.getPedidosRecientes().observe(getViewLifecycleOwner(), resource -> {
            if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                List<Pedido> pedidos = resource.data;
                // Limitar a los primeros 5 pedidos
                if (pedidos.size() > 5) {
                    pedidos = pedidos.subList(0, 5);
                }
                pedidosRecientesAdapter.setPedidos(pedidos);
            } else if (resource.status == Resource.Status.ERROR) {
                Toast.makeText(requireContext(), "Error al cargar pedidos: " + resource.message, Toast.LENGTH_SHORT).show();
            }
        });

        // Configurar acción "Ver todos"
        binding.layoutAdmin.tvVerTodosPedidosAdmin.setOnClickListener(v -> {
            Navigation.findNavController(requireView()).navigate(R.id.nav_pedidos);
        });
    }

    private void setupRepartidoresActivos() {
        binding.layoutAdmin.recyclerViewRepartidores.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.layoutAdmin.recyclerViewRepartidores.setAdapter(repartidoresAdapter);

        homeViewModel.getRepartidoresActivos().observe(getViewLifecycleOwner(), resource -> {
            if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                repartidoresAdapter.setRepartidores(resource.data);
            } else if (resource.status == Resource.Status.ERROR) {
                Toast.makeText(requireContext(), "Error al cargar repartidores: " + resource.message, Toast.LENGTH_SHORT).show();
            }
        });

        // Configurar acción "Ver todos"
        binding.layoutAdmin.tvVerTodosRepartidores.setOnClickListener(v -> {
            // Navegar a una vista de todos los repartidores (no implementada aún)
            Toast.makeText(requireContext(), "Funcionalidad no implementada", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupAdminActions() {
        // Configurar botones de acción rápida
        binding.layoutAdmin.btnAgregarProducto.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Agregar producto (no implementado)", Toast.LENGTH_SHORT).show();
            // En una implementación real, navegar a pantalla de agregar producto
        });

        binding.layoutAdmin.btnVerReportes.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Ver reportes (no implementado)", Toast.LENGTH_SHORT).show();
            // En una implementación real, navegar a pantalla de reportes
        });

        binding.layoutAdmin.btnAsignarPedidos.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Asignar pedidos (no implementado)", Toast.LENGTH_SHORT).show();
            // En una implementación real, navegar a pantalla de asignación de pedidos
        });
    }

    // MÉTODOS PARA MANEJO DE UBICACIÓN

    private void checkLocationPermissionAndStartUpdates() {
        locationPermissionHandler.checkLocationPermission(hasPermission -> {
            if (hasPermission) {
                startLocationUpdates();
            } else {
                Toast.makeText(requireContext(),
                        "Se requiere permiso de ubicación para mostrar el mapa",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void startLocationUpdates() {
        // Detener cualquier actualización previa
        stopLocationUpdates();

        // Crear un nuevo runnable para actualizaciones periódicas
        locationUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                // Obtener la ubicación actual
                getCurrentLocation();

                // Programar la próxima actualización
                locationUpdateHandler.postDelayed(this, LOCATION_UPDATE_INTERVAL);
            }
        };

        // Iniciar actualizaciones
        locationUpdateHandler.post(locationUpdateRunnable);
    }

    private void stopLocationUpdates() {
        if (locationUpdateRunnable != null) {
            locationUpdateHandler.removeCallbacks(locationUpdateRunnable);
        }
    }

    private void getCurrentLocation() {
        try {
            if (ActivityCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            LocationManager locationManager =
                    (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);

            // Intentar obtener la ubicación más reciente
            Location location = null;
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }

            if (location == null && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }

            if (location != null) {
                // Actualizar el mapa con la ubicación obtenida
                if (mapaManager != null) {
                    mapaManager.setCurrentLocation(location.getLatitude(), location.getLongitude());
                }
            }
        } catch (Exception e) {
            Log.e("HomeFragment", "Error al obtener ubicación: " + e.getMessage());
        }
    }

    // MÉTODOS AUXILIARES

    @Override
    public void onBannerClick(HomeViewModel.BannerItem bannerItem) {
        // Manejar clic en banner
        Toast.makeText(requireContext(), "Banner: " + bannerItem.getTitle(), Toast.LENGTH_SHORT).show();

        // En una implementación real, podríamos navegar a una oferta o producto específico
        // según el código de acción del banner
    }

    private String formatEstadoPedido(String estado) {
        switch (estado) {
            case Constants.ESTADO_PENDIENTE:
                return "En cocina";
            case Constants.ESTADO_EN_CAMINO:
                return "En camino";
            case Constants.ESTADO_ENTREGADO:
                return "Entregado";
            case Constants.ESTADO_CANCELADO:
                return "Cancelado";
            default:
                return estado;
        }
    }

    private int getProgressByEstado(String estado) {
        switch (estado) {
            case Constants.ESTADO_PENDIENTE:
                return 20;
            case Constants.ESTADO_EN_COCINA:
                return 40;
            case Constants.ESTADO_EN_CAMINO:
                return 80;
            case Constants.ESTADO_ENTREGADO:
                return 100;
            case Constants.ESTADO_CANCELADO:
                return 0;
            default:
                return 0;
        }
    }

    // MÉTODOS DEL CICLO DE VIDA

    @Override
    public void onResume() {
        super.onResume();
        if (mapaManager != null) {
            mapaManager.onResume();
        }

        // Reiniciar actualizaciones de ubicación si es repartidor
        if (homeViewModel.getUserRole().getValue() != null &&
                Constants.ROL_REPARTIDOR.equals(homeViewModel.getUserRole().getValue())) {
            startLocationUpdates();
        }
    }

    @Override
    public void onPause() {
        if (mapaManager != null) {
            mapaManager.onPause();
        }
        stopLocationUpdates();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (mapaManager != null) {
            mapaManager.onDestroy();
        }
        stopLocationUpdates();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        if (mapaManager != null) {
            mapaManager.onLowMemory();
        }
        super.onLowMemory();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        locationPermissionHandler.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}