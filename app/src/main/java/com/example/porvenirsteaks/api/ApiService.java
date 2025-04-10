package com.example.porvenirsteaks.api;

import com.example.porvenirsteaks.data.model.Categoria;
import com.example.porvenirsteaks.data.model.DetallePedido;
import com.example.porvenirsteaks.data.model.Notificacion;
import com.example.porvenirsteaks.data.model.Pedido;
import com.example.porvenirsteaks.data.model.Producto;
import com.example.porvenirsteaks.data.model.Ubicacion;
import com.example.porvenirsteaks.data.model.User;
import com.example.porvenirsteaks.data.model.requests.LoginRequest;
import com.example.porvenirsteaks.data.model.requests.PedidoRequest;
import com.example.porvenirsteaks.data.model.requests.RegisterRequest;
import com.example.porvenirsteaks.data.model.requests.UbicacionRequest;
import com.example.porvenirsteaks.data.model.responses.LoginResponse;
import com.example.porvenirsteaks.data.model.responses.RegisterResponse;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    // Auth Endpoints
    @POST("login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("register")
    Call<RegisterResponse> register(@Body RegisterRequest request);

    @POST("verificar-codigo")
    Call<Map<String, Object>> verificarCodigo(@Body Map<String, String> request);

    @POST("reenviar-codigo")
    Call<Map<String, Object>> reenviarCodigo(@Body Map<String, String> request);

    @POST("recuperar-password")
    Call<Map<String, Object>> recuperarPassword(@Body Map<String, String> request);

    @POST("cambiar-password")
    Call<Map<String, Object>> cambiarPassword(@Body Map<String, Object> request);

    @GET("user")
    Call<User> getUserProfile();

    @POST("user/update")
    Call<User> updateProfile(@Body Map<String, Object> request);

    @POST("logout")
    Call<Map<String, Object>> logout();

    // Productos Endpoints
    @GET("productos")
    Call<List<Producto>> getProductos();

    @GET("productos")
    Call<List<Producto>> getProductosByCategoria(@Query("categoria_id") int categoriaId);

    @GET("productos")
    Call<List<Producto>> searchProductos(@Query("nombre") String nombre);

    @GET("productos/{id}")
    Call<Producto> getProductoById(@Path("id") int id);

    // Categorías Endpoints
    @GET("categorias")
    Call<List<Categoria>> getCategorias();

    @GET("categorias/{id}")
    Call<Categoria> getCategoriaById(@Path("id") int id);

    // Pedidos Endpoints
    @GET("pedidos")
    Call<List<Pedido>> getPedidos();

    @GET("pedidos")
    Call<List<Pedido>> getPedidosByEstado(@Query("estado") String estado);

    @POST("pedidos")
    Call<Pedido> createPedido(@Body PedidoRequest request);

    @GET("pedidos/{id}")
    Call<Pedido> getPedidoById(@Path("id") int id);

    @POST("pedidos/{id}/estado")
    Call<Pedido> actualizarEstadoPedido(@Path("id") int id, @Body Map<String, String> request);

    @POST("pedidos/{id}/calificar")
    Call<Pedido> calificarPedido(@Path("id") int id, @Body Map<String, Object> request);

    // Detalles de Pedido Endpoints
    @GET("detalles-pedido")
    Call<List<DetallePedido>> getDetallesPedido(@Query("pedido_id") int pedidoId);

    @GET("detalles-pedido/{id}")
    Call<DetallePedido> getDetallePedidoById(@Path("id") int id);

    // Ubicaciones Endpoints
    @GET("ubicaciones")
    Call<List<Ubicacion>> getUbicaciones();

    @POST("ubicaciones")
    Call<Ubicacion> createUbicacion(@Body UbicacionRequest request);

    @GET("ubicaciones/{id}")
    Call<Ubicacion> getUbicacionById(@Path("id") int id);

    @PUT("ubicaciones/{id}")
    Call<Ubicacion> updateUbicacion(@Path("id") int id, @Body UbicacionRequest request);

    @DELETE("ubicaciones/{id}")
    Call<Map<String, Object>> deleteUbicacion(@Path("id") int id);

    // Notificaciones Endpoints
    @GET("notificaciones")
    Call<List<Notificacion>> getNotificaciones();

    @POST("notificaciones/{id}/marcar-leida")
    Call<Notificacion> marcarNotificacionLeida(@Path("id") int id);

    @POST("notificaciones/marcar-todas-leidas")
    Call<Map<String, Object>> marcarTodasNotificacionesLeidas();

    // FCM Endpoints
    @POST("fcm/register")
    Call<Map<String, Object>> registerFcmToken(@Body Map<String, String> request);

    // Repartidor Endpoints
    @GET("pedidos/pendientes")
    Call<List<Pedido>> getPedidosPendientes();

    @POST("repartidor/ubicacion")
    Call<Map<String, Object>> actualizarUbicacionRepartidor(@Body Map<String, Double> request);

    @POST("repartidor/disponibilidad")
    Call<Map<String, Object>> cambiarDisponibilidadRepartidor(@Body Map<String, Boolean> request);
}