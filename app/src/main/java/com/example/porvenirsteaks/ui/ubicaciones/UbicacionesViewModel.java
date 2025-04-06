package com.example.porvenirsteaks.ui.ubicaciones;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.porvenirsteaks.data.model.Ubicacion;
import com.example.porvenirsteaks.data.model.requests.UbicacionRequest;
import com.example.porvenirsteaks.data.repository.UbicacionRepository;
import com.example.porvenirsteaks.utils.Resource;

import java.util.List;

public class UbicacionesViewModel extends AndroidViewModel {
    private UbicacionRepository repository;

    public UbicacionesViewModel(@NonNull Application application) {
        super(application);
        repository = new UbicacionRepository(application);
    }

    public LiveData<Resource<List<Ubicacion>>> getUbicaciones() {
        return repository.getUbicaciones();
    }

    public LiveData<Resource<Ubicacion>> createUbicacion(UbicacionRequest request) {
        return repository.createUbicacion(request);
    }

    public LiveData<Resource<Boolean>> deleteUbicacion(int id) {
        return repository.deleteUbicacion(id);
    }
}