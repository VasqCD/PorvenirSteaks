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
import com.example.porvenirsteaks.data.model.responses.UserResponse;
import com.example.porvenirsteaks.data.preferences.TokenManager;
import com.example.porvenirsteaks.data.preferences.UserManager;
import com.example.porvenirsteaks.utils.ImageUtils;
import com.example.porvenirsteaks.utils.NetworkUtils;
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

        // Verificar conexión a internet
        if (!NetworkUtils.isNetworkAvailable(getApplication())) {
            // Cargar datos desde caché
            User cachedUser = UserManager.getUser(getApplication());
            if (cachedUser != null) {
                result.setValue(Resource.success(cachedUser));
            } else {
                result.setValue(Resource.error("No hay conexión a internet", null));
            }
            return result;
        }

        apiService.getUserProfile().enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getUser() != null) {
                    User user = response.body().getUser();

                    // Log para depuración
                    Log.d("PerfilViewModel", "Respuesta del servidor: ID=" + user.getId() +
                            ", Nombre=" + user.getName() +
                            ", Email=" + user.getEmail() +
                            ", Rol=" + user.getRol());

                    // IMPORTANTE: Verificar que el usuario tenga los datos correctos
                    if (user.getId() > 0) {
                        // Guardar en preferencias locales
                        UserManager.saveUser(getApplication(), user);
                        result.setValue(Resource.success(user));
                    } else {
                        Log.e("PerfilViewModel", "Usuario recibido del servidor con ID inválido: " + user.getId());

                        // Intentar recuperar de caché si hay problema con la respuesta
                        User cachedUser = UserManager.getUser(getApplication());
                        if (cachedUser != null && cachedUser.getId() > 0) {
                            result.setValue(Resource.success(cachedUser));
                        } else {
                            result.setValue(Resource.error("Datos de usuario incorrectos", null));
                        }
                    }
                } else {
                    try {
                        if (response.errorBody() != null) {
                            String errorMsg = response.errorBody().string();
                            result.setValue(Resource.error(errorMsg, null));
                        } else {
                            result.setValue(Resource.error("Error al obtener el perfil", null));
                        }
                    } catch (Exception e) {
                        result.setValue(Resource.error("Error al procesar la respuesta", null));
                    }
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Log.e("PerfilViewModel", "Error al obtener perfil", t);
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
            // Check if network is available
            if (!NetworkUtils.isNetworkAvailable(getApplication())) {
                result.setValue(Resource.error("No hay conexión a internet", null));
                return result;
            }

            // Comprimir la imagen
            File compressedFile = ImageUtils.compressImage(getApplication(), imageUri);
            if (compressedFile == null) {
                result.setValue(Resource.error("Error al procesar la imagen", null));
                return result;
            }

            // Crear RequestBody y MultipartBody.Part
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), compressedFile);
            MultipartBody.Part imagePart = MultipartBody.Part.createFormData("foto_perfil", compressedFile.getName(), requestFile);

            // Enviar la imagen al servidor
            apiService.uploadProfileImage(imagePart).enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        User user = response.body();
                        // Guardar en preferencias locales
                        UserManager.saveUser(getApplication(), user);
                        result.setValue(Resource.success(user));
                    } else {
                        try {
                            if (response.errorBody() != null) {
                                String errorMsg = response.errorBody().string();
                                result.setValue(Resource.error(errorMsg, null));
                            } else {
                                result.setValue(Resource.error("Error al subir la imagen", null));
                            }
                        } catch (Exception e) {
                            result.setValue(Resource.error("Error al procesar la respuesta", null));
                        }
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

        String[] filePathColumn = {android.provider.MediaStore.Images.Media.DATA};
        android.database.Cursor cursor = getApplication().getContentResolver().query(uri, filePathColumn, null, null, null);
        if (cursor == null)
            return uri.getPath();

        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        if (columnIndex < 0) {
            cursor.close();
            return uri.getPath();
        }
        String filePath = cursor.getString(columnIndex);
        cursor.close();

        return filePath;
    }
}