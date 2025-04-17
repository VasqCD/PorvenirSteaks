package com.example.porvenirsteaks.ui.productos;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.porvenirsteaks.data.model.Categoria;
import com.example.porvenirsteaks.data.model.Producto;
import com.example.porvenirsteaks.data.repository.CategoriaRepository;
import com.example.porvenirsteaks.data.repository.ProductoRepository;
import com.example.porvenirsteaks.utils.Resource;

import java.util.List;

public class ProductViewModel extends AndroidViewModel {
    private ProductoRepository productoRepository;
    private CategoriaRepository categoriaRepository;

    private MutableLiveData<Integer> categoriaSeleccionadaId = new MutableLiveData<>();
    private MutableLiveData<String> busqueda = new MutableLiveData<>();

    private MutableLiveData<Boolean> refreshTrigger = new MutableLiveData<>();

    public void forceRefresh() {
        // Esto forzará una nueva carga de datos
        refreshTrigger.setValue(true);
    }



    public void refreshProductos() {
        // Reiniciar la carga de productos
        productoRepository.getProductos();
    }

    public ProductViewModel(@NonNull Application application) {
        super(application);
        productoRepository = new ProductoRepository(application);
        categoriaRepository = new CategoriaRepository(application);
    }

    public LiveData<Resource<List<Producto>>> getProductos() {
        // Si ya tenemos la solicitud en curso, forzamos una nueva
        forceRefresh();
        return productoRepository.getProductos();
    }

    public LiveData<Resource<List<Categoria>>> getCategorias() {
        return categoriaRepository.getCategorias();
    }

    public LiveData<Resource<Producto>> getProductoById(int id) {
        return productoRepository.getProductoById(id);
    }

    public LiveData<Resource<List<Producto>>> getProductosByCategoria() {
        return Transformations.switchMap(categoriaSeleccionadaId, categoriaId -> {
            if (categoriaId == null || categoriaId == 0) {
                return productoRepository.getProductos();
            } else {
                return productoRepository.getProductosByCategoria(categoriaId);
            }
        });
    }

    public LiveData<Resource<List<Producto>>> searchProductos() {
        return Transformations.switchMap(busqueda, query -> {
            if (query == null || query.isEmpty()) {
                return productoRepository.getProductos();
            } else {
                return productoRepository.searchProductos(query);
            }
        });
    }

    public void setCategoriaSeleccionada(Integer categoriaId) {
        categoriaSeleccionadaId.setValue(categoriaId);
    }

    public void setBusqueda(String query) {
        busqueda.setValue(query);
    }
}