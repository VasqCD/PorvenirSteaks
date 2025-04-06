package com.example.porvenirsteaks.data.repository;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.porvenirsteaks.data.model.CartItem;
import com.example.porvenirsteaks.data.model.Producto;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class CarritoRepository {
    private static final String PREF_NAME = "CarritoPreferences";
    private static final String KEY_CARRITO = "carrito_items";

    private Context context;
    private SharedPreferences preferences;
    private List<CartItem> carritoItems;
    private MutableLiveData<List<CartItem>> carritoItemsLiveData = new MutableLiveData<>();

    public CarritoRepository(Context context) {
        this.context = context;
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        carritoItems = getCarritoFromPrefs();
        carritoItemsLiveData.setValue(carritoItems);
    }

    public LiveData<List<CartItem>> getCarrito() {
        return carritoItemsLiveData;
    }

    public void addProducto(Producto producto, int cantidad) {
        // Verificar si el producto ya está en el carrito
        boolean found = false;
        for (int i = 0; i < carritoItems.size(); i++) {
            CartItem item = carritoItems.get(i);
            if (item.getProducto().getId() == producto.getId()) {
                // Actualizar cantidad
                int newCantidad = item.getCantidad() + cantidad;
                item.setCantidad(newCantidad);
                found = true;
                break;
            }
        }

        // Si no se encontró, agregar nuevo item
        if (!found) {
            carritoItems.add(new CartItem(producto, cantidad));
        }

        // Guardar cambios
        saveCarrito();
    }

    public void updateCantidad(int productoId, int cantidad) {
        for (int i = 0; i < carritoItems.size(); i++) {
            CartItem item = carritoItems.get(i);
            if (item.getProducto().getId() == productoId) {
                if (cantidad <= 0) {
                    // Eliminar item si la cantidad es 0 o negativa
                    carritoItems.remove(i);
                } else {
                    // Actualizar cantidad
                    item.setCantidad(cantidad);
                }
                break;
            }
        }

        // Guardar cambios
        saveCarrito();
    }

    public void removeProducto(int productoId) {
        for (int i = 0; i < carritoItems.size(); i++) {
            if (carritoItems.get(i).getProducto().getId() == productoId) {
                carritoItems.remove(i);
                break;
            }
        }

        // Guardar cambios
        saveCarrito();
    }

    public void clearCarrito() {
        carritoItems.clear();
        saveCarrito();
    }

    public double getTotal() {
        double total = 0;
        for (CartItem item : carritoItems) {
            total += item.getSubtotal();
        }
        return total;
    }

    private void saveCarrito() {
        SharedPreferences.Editor editor = preferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(carritoItems);
        editor.putString(KEY_CARRITO, json);
        editor.apply();

        // Actualizar LiveData
        carritoItemsLiveData.setValue(carritoItems);
    }

    private List<CartItem> getCarritoFromPrefs() {
        Gson gson = new Gson();
        String json = preferences.getString(KEY_CARRITO, "");

        if (json.isEmpty()) {
            return new ArrayList<>();
        } else {
            Type type = new TypeToken<List<CartItem>>() {}.getType();
            return gson.fromJson(json, type);
        }
    }
}