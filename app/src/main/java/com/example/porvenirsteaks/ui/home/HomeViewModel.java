package com.example.porvenirsteaks.ui.home;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.porvenirsteaks.data.model.Categoria;
import com.example.porvenirsteaks.data.model.Pedido;
import com.example.porvenirsteaks.data.model.Producto;
import com.example.porvenirsteaks.data.model.Repartidor;
import com.example.porvenirsteaks.data.model.Ubicacion;
import com.example.porvenirsteaks.data.preferences.UserManager;
import com.example.porvenirsteaks.data.repository.CategoriaRepository;
import com.example.porvenirsteaks.data.repository.PedidoRepository;
import com.example.porvenirsteaks.data.repository.ProductoRepository;
import com.example.porvenirsteaks.data.repository.RepartidorRepository;
import com.example.porvenirsteaks.data.repository.UbicacionRepository;
import com.example.porvenirsteaks.utils.Constants;
import com.example.porvenirsteaks.utils.Resource;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeViewModel extends AndroidViewModel {

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;
    private final PedidoRepository pedidoRepository;
    private final UbicacionRepository ubicacionRepository;
    private final RepartidorRepository repartidorRepository;
    private final UserManager userManager;

    // LiveData compartidos
    private final MutableLiveData<String> greeting = new MutableLiveData<>();
    private final MutableLiveData<String> currentDate = new MutableLiveData<>();
    private final MutableLiveData<String> userRole = new MutableLiveData<>();

    // LiveData para cliente
    private final MutableLiveData<List<BannerItem>> bannerItems = new MutableLiveData<>();
    private final MediatorLiveData<Resource<List<Categoria>>> categorias = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<List<Producto>>> productosRecomendados = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<List<Pedido>>> ultimosPedidos = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<Pedido>> pedidoActivo = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<Ubicacion>> ubicacionActual = new MediatorLiveData<>();

    // LiveData para repartidor
    private final MutableLiveData<Boolean> disponibilidad = new MutableLiveData<>();
    private final MutableLiveData<EntregasStats> estadisticasEntregas = new MutableLiveData<>();
    private final MediatorLiveData<Resource<List<Pedido>>> entregasPendientes = new MediatorLiveData<>();

    // LiveData para administrador
    private final MutableLiveData<DashboardStats> estadisticasNegocio = new MutableLiveData<>();
    private final MediatorLiveData<Resource<List<Pedido>>> pedidosRecientes = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<List<Repartidor>>> repartidoresActivos = new MediatorLiveData<>();

    public HomeViewModel(@NonNull Application application) {
        super(application);

        // Inicializar repositorios
        categoriaRepository = new CategoriaRepository(application);
        productoRepository = new ProductoRepository(application);
        pedidoRepository = new PedidoRepository(application);
        ubicacionRepository = new UbicacionRepository(application);
        repartidorRepository = new RepartidorRepository(application);
        userManager = new UserManager(application);

        // Configurar datos iniciales
        initializeCommonData();

        // Determinar rol y cargar datos específicos
        userRole.setValue(userManager.getUserRole(application));
        switch (userRole.getValue()) {
            case Constants.ROL_CLIENTE:
                initializeClienteData();
                break;
            case Constants.ROL_REPARTIDOR:
                initializeRepartidorData();
                break;
            case Constants.ROL_ADMINISTRADOR:
                initializeAdminData();
                break;
        }
    }

    /**
     * Inicializa datos comunes para todos los roles
     */
    private void initializeCommonData() {
        // Obtener nombre del usuario y generar saludo
        String userName = userManager.getUserName(getApplication());
        greeting.setValue("¡Hola, " + userName + "!");

        // Obtener fecha actual
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, d 'de' MMMM, yyyy", new Locale("es", "ES"));
        currentDate.setValue(dateFormat.format(new Date()));
    }

    /**
     * Inicializa datos específicos para el rol Cliente
     */
    private void initializeClienteData() {
        // Configurar banners promocionales
        setupBanners();

        // Cargar categorías populares
        loadCategorias();

        // Cargar productos recomendados
        loadProductosRecomendados();

        // Cargar últimos pedidos
        loadUltimosPedidos();

        // Cargar pedido activo (si existe)
        loadPedidoActivo();

        // Cargar ubicación actual
        loadUbicacionActual();
    }

    /**
     * Inicializa datos específicos para el rol Repartidor
     */
    private void initializeRepartidorData() {
        // Establecer disponibilidad inicial
        disponibilidad.setValue(true);

        // Cargar estadísticas de entregas
        loadEstadisticasEntregas();

        // Cargar entregas pendientes
        loadEntregasPendientes();
    }

    /**
     * Inicializa datos específicos para el rol Administrador
     */
    private void initializeAdminData() {
        // Cargar estadísticas del negocio
        loadEstadisticasNegocio();

        // Cargar pedidos recientes
        loadPedidosRecientes();

        // Cargar repartidores activos
        loadRepartidoresActivos();
    }

    // --- Métodos de carga de datos ---

    private void setupBanners() {
        List<BannerItem> banners = new ArrayList<>();

        // Estos datos deberían venir de la API, pero para el ejemplo los hardcodeamos
        banners.add(new BannerItem(
                "Promoción del día",
                "Disfruta de nuestros deliciosos cortes con 20% de descuento",
                "https://example.com/banner1.jpg",
                "PROMO20"));

        banners.add(new BannerItem(
                "Nuevos cortes premium",
                "Hemos añadido nuevos cortes importados a nuestro menú",
                "https://example.com/banner2.jpg",
                "NUEVOS"));

        banners.add(new BannerItem(
                "Envío gratis",
                "En todos los pedidos mayores a L. 500",
                "https://example.com/banner3.jpg",
                "ENVIO0"));

        bannerItems.setValue(banners);
    }

    private void loadCategorias() {
        categorias.addSource(categoriaRepository.getCategorias(), resource -> {
            categorias.setValue(resource);
        });
    }

    private void loadProductosRecomendados() {
        // En un caso real, esto debería ser una llamada específica a la API para recomendaciones
        // Por ahora, simplemente cargamos todos los productos
        productosRecomendados.addSource(productoRepository.getProductos(), resource -> {
            productosRecomendados.setValue(resource);
        });
    }

    private void loadUltimosPedidos() {
        ultimosPedidos.addSource(pedidoRepository.getPedidos(), resource -> {
            ultimosPedidos.setValue(resource);
        });
    }

    private void loadPedidoActivo() {
        // Esto debería ser una llamada específica a la API
        // Por simplicidad, asumimos que no hay pedido activo
        pedidoActivo.setValue(Resource.success(null));
    }

    private void loadUbicacionActual() {
        // Cargar ubicaciones y tomar la principal
        ubicacionRepository.getUbicaciones().observeForever(resource -> {
            if (resource.status == Resource.Status.SUCCESS && resource.data != null && !resource.data.isEmpty()) {
                // Buscar la ubicación principal
                for (Ubicacion ubicacion : resource.data) {
                    if (ubicacion.isEsPrincipal()) {
                        ubicacionActual.setValue(Resource.success(ubicacion));
                        return;
                    }
                }
                // Si no hay principal, tomar la primera
                if (!resource.data.isEmpty()) {
                    ubicacionActual.setValue(Resource.success(resource.data.get(0)));
                } else {
                    ubicacionActual.setValue(Resource.success(null));
                }
            } else {
                ubicacionActual.setValue(Resource.error("No se pudo cargar la ubicación", null));
            }
        });
    }

    private void loadEstadisticasEntregas() {
        // Datos de ejemplo (en un caso real vendrían de la API)
        EntregasStats stats = new EntregasStats(5, 4.5, 250);
        estadisticasEntregas.setValue(stats);
    }

    private void loadEntregasPendientes() {
        entregasPendientes.addSource(pedidoRepository.getPedidosPendientes(), resource -> {
            entregasPendientes.setValue(resource);
        });
    }

    private void loadEstadisticasNegocio() {
        // Datos de ejemplo (en un caso real vendrían de la API)
        DashboardStats stats = new DashboardStats(5250, 12, 3, 5);
        estadisticasNegocio.setValue(stats);
    }

    private void loadPedidosRecientes() {
        pedidosRecientes.addSource(pedidoRepository.getPedidos(), resource -> {
            pedidosRecientes.setValue(resource);
        });
    }

    private void loadRepartidoresActivos() {
        repartidoresActivos.addSource(repartidorRepository.getRepartidoresDisponibles(), resource -> {
            repartidoresActivos.setValue(resource);
        });
    }

    // --- Métodos para cambiar estados ---

    public void setDisponibilidad(boolean disponible) {
        disponibilidad.setValue(disponible);

        // En un caso real, esto llamaría al repositorio para actualizar en el servidor
        repartidorRepository.cambiarDisponibilidad(disponible).observeForever(resource -> {
            // Podríamos manejar errores aquí
        });
    }

    // --- Getters para todos los LiveData ---

    public LiveData<String> getGreeting() {
        return greeting;
    }

    public LiveData<String> getCurrentDate() {
        return currentDate;
    }

    public LiveData<String> getUserRole() {
        return userRole;
    }

    // Cliente
    public LiveData<List<BannerItem>> getBannerItems() {
        return bannerItems;
    }

    public LiveData<Resource<List<Categoria>>> getCategorias() {
        return categorias;
    }

    public LiveData<Resource<List<Producto>>> getProductosRecomendados() {
        return productosRecomendados;
    }

    public LiveData<Resource<List<Pedido>>> getUltimosPedidos() {
        return ultimosPedidos;
    }

    public LiveData<Resource<Pedido>> getPedidoActivo() {
        return pedidoActivo;
    }

    public LiveData<Resource<Ubicacion>> getUbicacionActual() {
        return ubicacionActual;
    }

    // Repartidor
    public LiveData<Boolean> getDisponibilidad() {
        return disponibilidad;
    }

    public LiveData<EntregasStats> getEstadisticasEntregas() {
        return estadisticasEntregas;
    }

    public LiveData<Resource<List<Pedido>>> getEntregasPendientes() {
        return entregasPendientes;
    }

    // Administrador
    public LiveData<DashboardStats> getEstadisticasNegocio() {
        return estadisticasNegocio;
    }

    public LiveData<Resource<List<Pedido>>> getPedidosRecientes() {
        return pedidosRecientes;
    }

    public LiveData<Resource<List<Repartidor>>> getRepartidoresActivos() {
        return repartidoresActivos;
    }

    // Clases internas para los datos

    /**
     * Clase para los elementos del banner
     */
    public static class BannerItem {
        private String title;
        private String description;
        private String imageUrl;
        private String actionCode;

        public BannerItem(String title, String description, String imageUrl, String actionCode) {
            this.title = title;
            this.description = description;
            this.imageUrl = imageUrl;
            this.actionCode = actionCode;
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public String getActionCode() {
            return actionCode;
        }
    }

    /**
     * Clase para las estadísticas de entregas de repartidores
     */
    public static class EntregasStats {
        private int entregasCount;
        private double distanciaRecorrida;
        private double gananciasHoy;

        public EntregasStats(int entregasCount, double distanciaRecorrida, double gananciasHoy) {
            this.entregasCount = entregasCount;
            this.distanciaRecorrida = distanciaRecorrida;
            this.gananciasHoy = gananciasHoy;
        }

        public int getEntregasCount() {
            return entregasCount;
        }

        public double getDistanciaRecorrida() {
            return distanciaRecorrida;
        }

        public double getGananciasHoy() {
            return gananciasHoy;
        }
    }

    /**
     * Clase para las estadísticas del dashboard de administrador
     */
    public static class DashboardStats {
        private double ventasHoy;
        private int pedidosHoy;
        private int pedidosPendientes;
        private int repartidoresActivos;

        public DashboardStats(double ventasHoy, int pedidosHoy, int pedidosPendientes, int repartidoresActivos) {
            this.ventasHoy = ventasHoy;
            this.pedidosHoy = pedidosHoy;
            this.pedidosPendientes = pedidosPendientes;
            this.repartidoresActivos = repartidoresActivos;
        }

        public double getVentasHoy() {
            return ventasHoy;
        }

        public int getPedidosHoy() {
            return pedidosHoy;
        }

        public int getPedidosPendientes() {
            return pedidosPendientes;
        }

        public int getRepartidoresActivos() {
            return repartidoresActivos;
        }
    }
}