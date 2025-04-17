package com.example.porvenirsteaks.ui.carrito;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.example.porvenirsteaks.data.model.CartItem;
import com.example.porvenirsteaks.data.model.Pedido;
import com.example.porvenirsteaks.data.model.Producto;
import com.example.porvenirsteaks.data.model.Ubicacion;
import com.example.porvenirsteaks.data.model.requests.PedidoRequest;
import com.example.porvenirsteaks.data.repository.CarritoRepository;
import com.example.porvenirsteaks.data.repository.PedidoRepository;
import com.example.porvenirsteaks.data.repository.ProductoRepository;
import com.example.porvenirsteaks.data.repository.UbicacionRepository;
import com.example.porvenirsteaks.utils.Resource;

import java.util.ArrayList;
import java.util.List;

public class CarritoViewModel extends AndroidViewModel {
    private CarritoRepository carritoRepository;
    private UbicacionRepository ubicacionRepository;
    private PedidoRepository pedidoRepository;

    private ProductoRepository productoRepository;

    private LiveData<Resource<Producto>> currentProductRequest;





    public CarritoViewModel(@NonNull Application application) {
        super(application);
        carritoRepository = new CarritoRepository(application);
        ubicacionRepository = new UbicacionRepository(application);
        pedidoRepository = new PedidoRepository(application);
        productoRepository = new ProductoRepository(application);
    }

    public LiveData<List<CartItem>> getCarritoItems() {
        return carritoRepository.getCarrito();
    }

    public LiveData<Resource<List<Ubicacion>>> getUbicaciones() {
        return ubicacionRepository.getUbicaciones();
    }

    public double getTotal() {
        return carritoRepository.getTotal();
    }

    public void addToCart(Producto producto, int cantidad) {
        carritoRepository.addProducto(producto, cantidad);
    }
    public LiveData<Boolean> addToCart(int productoId, int cantidad) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();

        productoRepository.getProductoById(productoId).observeForever(new Observer<Resource<Producto>>() {
            @Override
            public void onChanged(Resource<Producto> resource) {
                // Remove observer to prevent memory leaks
                productoRepository.getProductoById(productoId).removeObserver(this);

                if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                    // Add product to cart
                    carritoRepository.addProducto(resource.data, cantidad);
                    result.setValue(true);
                } else {
                    result.setValue(false);
                }
            }
        });

        return result;
    }

    public void updateCantidad(int productoId, int cantidad) {
        carritoRepository.updateCantidad(productoId, cantidad);
    }

    public void removeFromCart(int productoId) {
        carritoRepository.removeProducto(productoId);
    }

    public void clearCart() {
        carritoRepository.clearCarrito();
    }

//    public LiveData<Resource<List<Ubicacion>>> getUbicaciones() {
//        return ubicacionRepository.getUbicaciones();
//    }

    public LiveData<Resource<Pedido>> realizarPedido(int ubicacionId) {
        List<CartItem> items = carritoRepository.getCarrito().getValue();
        if (items == null || items.isEmpty()) {
            MutableLiveData<Resource<Pedido>> result = new MutableLiveData<>();
            result.setValue(Resource.error("El carrito está vacío", null));
            return result;
        }

        // Convertir CartItems a formato para la API
        List<PedidoRequest.ProductoPedido> productos = new ArrayList<>();
        for (CartItem item : items) {
            productos.add(new PedidoRequest.ProductoPedido(
                    item.getProducto().getId(),
                    item.getCantidad()
            ));
        }

        // Crear objeto de pedido
        PedidoRequest pedidoRequest = new PedidoRequest();
        pedidoRequest.setUbicacion_id(ubicacionId);
        pedidoRequest.setProductos(productos);
        pedidoRequest.setEstadoAnterior("creado");


        // Enviar pedido a la API
        LiveData<Resource<Pedido>> resultado = pedidoRepository.createPedido(pedidoRequest);

        // Si el pedido es exitoso, limpiar el carrito
        resultado.observeForever(pedidoResource -> {
            if (pedidoResource.status == Resource.Status.SUCCESS && pedidoResource.data != null) {
                carritoRepository.clearCarrito();
            }
        });

        return resultado;
    }
}