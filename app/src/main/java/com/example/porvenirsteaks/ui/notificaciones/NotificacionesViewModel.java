package com.example.porvenirsteaks.ui.notificaciones;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.porvenirsteaks.data.model.Notificacion;
import com.example.porvenirsteaks.data.repository.NotificacionRepository;
import com.example.porvenirsteaks.utils.Resource;

import java.util.List;

public class NotificacionesViewModel extends AndroidViewModel {
    private NotificacionRepository repository;

    public NotificacionesViewModel(@NonNull Application application) {
        super(application);
        repository = new NotificacionRepository(application);
    }

    public LiveData<Resource<List<Notificacion>>> getNotificaciones() {
        return repository.getNotificaciones();
    }

    public LiveData<Resource<Notificacion>> marcarComoLeida(int id) {
        return repository.marcarComoLeida(id);
    }

    public LiveData<Resource<Boolean>> marcarTodasComoLeidas() {
        return repository.marcarTodasComoLeidas();
    }
}