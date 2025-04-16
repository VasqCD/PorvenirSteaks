package com.example.porvenirsteaks.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.porvenirsteaks.R;
import com.example.porvenirsteaks.utils.ImageUtils;

import java.util.ArrayList;
import java.util.List;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerViewHolder> {

    private List<HomeViewModel.BannerItem> bannerItems = new ArrayList<>();
    private OnBannerClickListener listener;

    public interface OnBannerClickListener {
        void onBannerClick(HomeViewModel.BannerItem bannerItem);
    }

    public BannerAdapter(OnBannerClickListener listener) {
        this.listener = listener;
    }

    public void setBannerItems(List<HomeViewModel.BannerItem> bannerItems) {
        this.bannerItems = bannerItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_banner_promocion, parent, false);
        return new BannerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
        holder.bind(bannerItems.get(position));
    }

    @Override
    public int getItemCount() {
        return bannerItems.size();
    }

    class BannerViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivBannerBackground;
        private TextView tvBannerTitle;
        private TextView tvBannerDescription;
        private Button btnBannerAction;

        public BannerViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBannerBackground = itemView.findViewById(R.id.ivBannerBackground);
            tvBannerTitle = itemView.findViewById(R.id.tvBannerTitle);
            tvBannerDescription = itemView.findViewById(R.id.tvBannerDescription);
            btnBannerAction = itemView.findViewById(R.id.btnBannerAction);
        }

        public void bind(HomeViewModel.BannerItem bannerItem) {
            tvBannerTitle.setText(bannerItem.getTitle());
            tvBannerDescription.setText(bannerItem.getDescription());

            // Cargar imagen usando Glide (a través de nuestra utilidad)
            ImageUtils.loadImage(ivBannerBackground, bannerItem.getImageUrl());

            // Configurar acción del botón
            btnBannerAction.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onBannerClick(bannerItem);
                }
            });

            // Hacer clic en toda la vista también
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onBannerClick(bannerItem);
                }
            });
        }
    }
}