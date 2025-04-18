package com.example.porvenirsteaks.utils;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.example.porvenirsteaks.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class ImageUtils {
    public static void loadImage(ImageView imageView, String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            // Construir URL completa si es necesario
            String fullUrl = imageUrl;
            if (!imageUrl.startsWith("http")) {
                fullUrl = Constants.BASE_IMAGE_URL + imageUrl;
            }

            Glide.with(imageView.getContext())
                    .load(fullUrl)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.placeholder_image)
                            .error(R.drawable.error_image))
                    .into(imageView);
        } else {
            imageView.setImageResource(R.drawable.placeholder_image);
        }
    }

    public static void loadUserPhoto(ImageView imageView, String photoUrl) {
        if (photoUrl != null && !photoUrl.isEmpty()) {
            // Construir URL completa si es necesario
            String fullUrl = photoUrl;
            if (!photoUrl.startsWith("http")) {
                fullUrl = Constants.BASE_IMAGE_URL + photoUrl;
            }

            Log.d("ImageUtils", "Cargando foto de perfil: " + fullUrl);

            // Usar un enfoque más simple y directo con Glide
            Glide.with(imageView.getContext().getApplicationContext()) // Usar ApplicationContext para evitar memory leaks
                    .load(fullUrl)
                    .placeholder(R.drawable.user_placeholder)
                    .error(R.drawable.user_placeholder)
                    .circleCrop()
                    .into(imageView);
        } else {
            imageView.setImageResource(R.drawable.user_placeholder);
        }
    }

    // Métodos adicionales para tu clase ImageUtils.java

    /**
     * Comprime una imagen para reducir su tamaño antes de subirla
     */
    public static File compressImage(Context context, Uri imageUri) {
        try {
            // Obtener bitmap de la Uri
            Bitmap originalBitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), imageUri);

            // Obtener las dimensiones originales
            int originalWidth = originalBitmap.getWidth();
            int originalHeight = originalBitmap.getHeight();

            // Calcular nuevas dimensiones (limitar a máximo 1000px en cualquier dimensión)
            float maxDimension = 1000.0f;
            float scale = 1.0f;

            if (originalWidth > maxDimension || originalHeight > maxDimension) {
                if (originalWidth > originalHeight) {
                    scale = maxDimension / originalWidth;
                } else {
                    scale = maxDimension / originalHeight;
                }
            }

            // Redimensionar bitmap si es necesario
            Bitmap resizedBitmap;
            if (scale < 1.0f) {
                int newWidth = Math.round(originalWidth * scale);
                int newHeight = Math.round(originalHeight * scale);
                resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true);
            } else {
                resizedBitmap = originalBitmap;
            }

            // Comprimir a una calidad menor
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);

            // Verificar si el tamaño sigue siendo muy grande
            byte[] imageBytes = baos.toByteArray();
            int imageSize = imageBytes.length;

            // Si aún es más de 1MB, reducir más la calidad
            if (imageSize > 1024 * 1024) {
                baos.reset();
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 30, baos);
                imageBytes = baos.toByteArray();
            }

            // Crear archivo temporal para guardar la imagen comprimida
            File outputDir = context.getCacheDir();
            File outputFile = File.createTempFile("compressed_", ".jpg", outputDir);

            // Escribir bytes comprimidos al archivo
            FileOutputStream fos = new FileOutputStream(outputFile);
            fos.write(baos.toByteArray());
            fos.close();

            // Log del tamaño final para verificación
            Log.d("ImageUtils", "Tamaño original: " + originalBitmap.getByteCount() + " bytes");
            Log.d("ImageUtils", "Tamaño después de comprimir: " + baos.size() + " bytes");

            return outputFile;
        } catch (Exception e) {
            Log.e("ImageUtils", "Error al comprimir imagen", e);
            return null;
        }
    }

    /**
     * Obtiene el path real de un Uri
     */
    public static String getRealPathFromURI(Context context, Uri uri) {
        String filePath = "";
        try {
            // Check for newer Android versions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Use ContentResolver for Android 10 and above
                InputStream inputStream = context.getContentResolver().openInputStream(uri);
                if (inputStream != null) {
                    File tempFile = File.createTempFile("image", ".jpg", context.getCacheDir());
                    FileOutputStream outputStream = new FileOutputStream(tempFile);

                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }

                    inputStream.close();
                    outputStream.close();

                    filePath = tempFile.getAbsolutePath();
                }
            } else {
                // For older Android versions
                String[] projection = {MediaStore.Images.Media.DATA};
                Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
                if (cursor != null) {
                    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    cursor.moveToFirst();
                    filePath = cursor.getString(column_index);
                    cursor.close();
                }
            }
        } catch (Exception e) {
            Log.e("ImageUtils", "Error getting real path from URI: " + e.getMessage());
        }

        return filePath;
    }

    public static Uri getImageUri(Context context, Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Title", null);
        return Uri.parse(path);
    }

    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
}