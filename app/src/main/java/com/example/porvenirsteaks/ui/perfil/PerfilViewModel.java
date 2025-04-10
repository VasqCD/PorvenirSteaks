package com.example.porvenirsteaks.ui.perfil;

import android.app.Application;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.porvenirsteaks.api.ApiService;
import com.example.porvenirsteaks.api.RetrofitClient;
import com.example.porvenirsteaks.data.model.User;
import com.example.porvenirsteaks.data.preferences.TokenManager;
import com.example.porvenirsteaks.data.preferences.UserManager;
import com.example.porvenirsteaks.utils.Resource;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PerfilViewModel extends AndroidViewModel {
    private static final String TAG = "PerfilViewModel";
    private ApiService apiService;

    public PerfilViewModel(@NonNull Application application) {
        super(application);
        apiService = RetrofitClient.getClient(TokenManager.getToken(application))
                .create(ApiService.class);
    }

    /**
     * Obtiene el perfil del usuario desde el servidor
     */
    public LiveData<Resource<User>> getUserProfile() {
        MutableLiveData<Resource<User>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        apiService.getUserProfile().enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    // Guardar en preferencias locales
                    UserManager.saveUser(getApplication(), user);
                    result.setValue(Resource.success(user));
                } else {
                    result.setValue(Resource.error("Error al obtener el perfil", null));
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e(TAG, "Error al obtener perfil", t);
                result.setValue(Resource.error("Error de conexión: " + t.getMessage(), null));
            }
        });

        return result;
    }

    /**
     * Actualiza el perfil del usuario con la información proporcionada
     */
    public LiveData<Resource<User>> updateProfile(String nombre, String apellido, String telefono) {
        MutableLiveData<Resource<User>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        Map<String, Object> request = new HashMap<>();
        request.put("name", nombre);
        request.put("apellido", apellido);
        request.put("telefono", telefono);

        apiService.updateProfile(request).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    // Guardar en preferencias locales
                    UserManager.saveUser(getApplication(), user);
                    result.setValue(Resource.success(user));
                } else {
                    result.setValue(Resource.error("Error al actualizar el perfil", null));
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e(TAG, "Error al actualizar perfil", t);
                result.setValue(Resource.error("Error de conexión: " + t.getMessage(), null));
            }
        });

        return result;
    }

    /**
     * Cambia la contraseña del usuario
     */
    public LiveData<Resource<Boolean>> changePassword(String currentPassword, String newPassword) {
        MutableLiveData<Resource<Boolean>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        Map<String, String> request = new HashMap<>();
        request.put("current_password", currentPassword);
        request.put("new_password", newPassword);

        // Suponiendo que tienes este endpoint en ApiService
        apiService.changePassword(request).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    result.setValue(Resource.success(true));
                } else {
                    String errorMessage = "Error al cambiar la contraseña";
                    try {
                        if (response.errorBody() != null) {
                            errorMessage = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error al leer errorBody", e);
                    }
                    result.setValue(Resource.error(errorMessage, false));
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Log.e(TAG, "Error al cambiar contraseña", t);
                result.setValue(Resource.error("Error de conexión: " + t.getMessage(), false));
            }
        });

        return result;
    }

    /**
     * Sube una imagen de perfil
     */
    public LiveData<Resource<User>> uploadProfileImage(Uri imageUri) {
        MutableLiveData<Resource<User>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        try {
            // Convertir Uri a File
            File file = new File(getRealPathFromURI(imageUri));

            // Crear RequestBody y MultipartBody.Part
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
            MultipartBody.Part imagePart = MultipartBody.Part.createFormData("foto_perfil", file.getName(), requestFile);

            // Suponiendo que tienes este endpoint en ApiService
            apiService.uploadProfileImage(imagePart).enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        User user = response.body();
                        // Guardar en preferencias locales
                        UserManager.saveUser(getApplication(), user);
                        result.setValue(Resource.success(user));
                    } else {
                        result.setValue(Resource.error("Error al subir la imagen", null));
                    }
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    Log.e(TAG, "Error al subir imagen", t);
                    result.setValue(Resource.error("Error de conexión: " + t.getMessage(), null));
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error al procesar imagen", e);
            result.setValue(Resource.error("Error al procesar la imagen: " + e.getMessage(), null));
        }

        return result;
    }

    /**
     * Envía solicitud para ser repartidor
     */
    public LiveData<Resource<Map<String, Object>>> solicitarSerRepartidor() {
        MutableLiveData<Resource<Map<String, Object>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        // Suponiendo que tienes este endpoint en ApiService
        apiService.solicitarSerRepartidor().enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(Resource.success(response.body()));
                } else {
                    result.setValue(Resource.error("Error al enviar solicitud", null));
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Log.e(TAG, "Error al solicitar ser repartidor", t);
                result.setValue(Resource.error("Error de conexión: " + t.getMessage(), null));
            }
        });

        return result;
    }

    /**
     * Cierra la sesión del usuario
     */
    public LiveData<Resource<Boolean>> logout() {
        MutableLiveData<Resource<Boolean>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        apiService.logout().enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                // Independientemente del resultado, limpiamos la sesión localmente
                TokenManager.clearToken(getApplication());
                UserManager.clearUser(getApplication());

                if (response.isSuccessful()) {
                    result.setValue(Resource.success(true));
                } else {
                    // Aún consideramos éxito ya que limpiamos localmente
                    result.setValue(Resource.success(true));
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                // Aún en caso de fallo, limpiamos localmente
                TokenManager.clearToken(getApplication());
                UserManager.clearUser(getApplication());
                result.setValue(Resource.success(true));
            }
        });

        return result;
    }

    /**
     * Método auxiliar para obtener la ruta real de un Uri
     */
    private String getRealPathFromURI(Uri uri) {
        // Aquí deberías implementar la lógica para convertir un Uri a una ruta real
        // Esta implementación podría variar según la versión de Android y el tipo de Uri

        // Implementación simplificada para este ejemplo:
        String[] filePathColumn = {android.provider.MediaStore.Images.Media.DATA};
        android.database.Cursor cursor = getApplication().getContentResolver().query(uri, filePathColumn, null, null, null);
        if (cursor == null)
            return uri.getPath();

        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String filePath = cursor.getString(columnIndex);
        cursor.close();

        return filePath;
    }
}