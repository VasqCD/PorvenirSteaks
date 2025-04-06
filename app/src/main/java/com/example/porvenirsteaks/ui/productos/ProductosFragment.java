package com.example.porvenirsteaks.ui.productos;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.porvenirsteaks.databinding.FragmentProductosBinding;
import com.example.porvenirsteaks.ui.productos.adapters.CategoriasAdapter;
import com.example.porvenirsteaks.ui.productos.adapters.ProductosAdapter;
import com.example.porvenirsteaks.utils.Resource;

public class ProductosFragment extends Fragment {
    private FragmentProductosBinding binding;
    private ProductViewModel viewModel;
    private ProductosAdapter productosAdapter;
    private CategoriasAdapter categoriasAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProductosBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ProductViewModel.class);

        setupRecyclerViews();
        setupSearchView();
        observeViewModel();

        // Cargar datos iniciales
        viewModel.getCategorias();
        viewModel.getProductos();
    }

    private void setupRecyclerViews() {
        // Configurar RecyclerView de categorías
        categoriasAdapter = new CategoriasAdapter(categoriaId -> {
            viewModel.setCategoriaSeleccionada(categoriaId);
        });
        binding.recyclerViewCategorias.setAdapter(categoriasAdapter);

        // Configurar RecyclerView de productos
        productosAdapter = new ProductosAdapter(producto -> {
            // Navegar a detalle de producto
            Bundle args = new Bundle();
            args.putInt("producto_id", producto.getId());

            // Navegar usando NavController
            // navController.navigate(R.id.action_productosFragment_to_detalleProductoFragment, args);
        });
        binding.recyclerViewProductos.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.recyclerViewProductos.setAdapter(productosAdapter);
    }

    private void setupSearchView() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                viewModel.setBusqueda(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    viewModel.setBusqueda("");
                }
                return true;
            }
        });
    }

    private void observeViewModel() {
        // Observar categorías
        viewModel.getCategorias().observe(getViewLifecycleOwner(), result -> {
            if (result.status == Resource.Status.SUCCESS && result.data != null) {
                categoriasAdapter.submitList(result.data);
            } else if (result.status == Resource.Status.ERROR) {
                Toast.makeText(requireContext(), "Error: " + result.message, Toast.LENGTH_SHORT).show();
            }
        });

        // Observar productos por categoría
        viewModel.getProductosByCategoria().observe(getViewLifecycleOwner(), result -> {
            binding.progressBar.setVisibility(
                    result.status == Resource.Status.LOADING ? View.VISIBLE : View.GONE);

            if (result.status == Resource.Status.SUCCESS && result.data != null) {
                productosAdapter.submitList(result.data);
                binding.tvEmptyState.setVisibility(result.data.isEmpty() ? View.VISIBLE : View.GONE);
            } else if (result.status == Resource.Status.ERROR) {
                Toast.makeText(requireContext(), "Error: " + result.message, Toast.LENGTH_SHORT).show();
                binding.tvEmptyState.setVisibility(View.VISIBLE);
            }
        });

        // Observar búsqueda de productos
        viewModel.searchProductos().observe(getViewLifecycleOwner(), result -> {
            binding.progressBar.setVisibility(
                    result.status == Resource.Status.LOADING ? View.VISIBLE : View.GONE);

            if (result.status == Resource.Status.SUCCESS && result.data != null) {
                productosAdapter.submitList(result.data);
                binding.tvEmptyState.setVisibility(result.data.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    @Override
    public void onResume() {
        super.onResume();
        // Actualizar la lista de productos al volver a la vista
        viewModel.getProductos();
    }
    @Override
    public void onPause() {
        super.onPause();
        // Limpiar la búsqueda al salir de la vista
        binding.searchView.setQuery("", false);
        viewModel.setBusqueda("");
    }
}