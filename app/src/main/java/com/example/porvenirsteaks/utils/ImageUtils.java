package com.example.porvenirsteaks.utils;

import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.example.porvenirsteaks.R;

public class ImageUtils {
    public static void loadImage(ImageView imageView, String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            // Construir URL completa si es necesario
            String fullUrl = imageUrl;
            if (!imageUrl.startsWith("http")) {
                fullUrl = Constants.BASE_URL + imageUrl;
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
                fullUrl = Constants.BASE_URL + photoUrl;
            }

            Glide.with(imageView.getContext())
                    .load(fullUrl)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.user_placeholder)
                            .error(R.drawable.user_placeholder)
                            .circleCrop())
                    .into(imageView);
        } else {
            imageView.setImageResource(R.drawable.user_placeholder);
        }
    }
}