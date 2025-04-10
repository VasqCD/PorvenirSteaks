package com.example.porvenirsteaks.ui.productos;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.porvenirsteaks.R;
import com.example.porvenirsteaks.data.model.Producto;
import com.example.porvenirsteaks.databinding.FragmentDetalleProductoBinding;
import com.example.porvenirsteaks.ui.carrito.CarritoViewModel;
import com.example.porvenirsteaks.utils.ImageUtils;
import com.example.porvenirsteaks.utils.Resource;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.text.NumberFormat;
import java.util.Locale;

public class DetalleProductoFragment extends Fragment {
    private FragmentDetalleProductoBinding binding;
    private ProductViewModel viewModel;
    private CarritoViewModel carritoViewModel;
    private int productoId;
    private int cantidad = 1;
    private Producto currentProducto;
    private NumberFormat currencyFormatter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDetalleProductoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ProductViewModel.class);
        carritoViewModel = new ViewModelProvider(this).get(CarritoViewModel.class);
        currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("es", "HN"));

        // Obtener productoId de los argumentos
        if (getArguments() != null) {
            productoId = getArguments().getInt("producto_id", 0);
            if (productoId == 0) {
                Toast.makeText(requireContext(), "Error: ID de producto no válido", Toast.LENGTH_SHORT).show();
                navigateUp();
                return;
            }
        }

        setupUI();
        cargarProducto();
    }

    private void setupUI() {
        // Configurar la barra de herramientas
        binding.toolbar.setNavigationOnClickListener(v -> navigateUp());

        // Configurar botones para la cantidad
        binding.btnDecrement.setOnClickListener(v -> {
            if (cantidad > 1) {
                cantidad--;
                updateCantidad();
            }
        });

        binding.btnIncrement.setOnClickListener(v -> {
            cantidad++;
            updateCantidad();
        });

        // Inicializar la cantidad
        binding.tvCantidad.setText(String.valueOf(cantidad));

        // Configurar botón para agregar al carrito
        binding.btnAgregarAlCarrito.setOnClickListener(v -> agregarAlCarrito());
    }

    private void cargarProducto() {
        binding.progressBar.setVisibility(View.VISIBLE);

        viewModel.getProductoById(productoId).observe(getViewLifecycleOwner(), result -> {
            binding.progressBar.setVisibility(View.GONE);

            if (result.status == Resource.Status.SUCCESS && result.data != null) {
                currentProducto = result.data;
                actualizarUI(result.data);
            } else if (result.status == Resource.Status.ERROR) {
                Toast.makeText(requireContext(), "Error: " + result.message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void actualizarUI(Producto producto) {
        // Actualizar título en la toolbar
        CollapsingToolbarLayout collapsingToolbar = binding.collapsingToolbar;
        collapsingToolbar.setTitle(producto.getNombre());

        // Cargar imagen
        ImageUtils.loadImage(binding.ivProductoImagen, producto.getImagen());

        // Información básica
        binding.tvNombreProducto.setText(producto.getNombre());
        binding.tvPrecio.setText(currencyFormatter.format(producto.getPrecio()));

        // Descripción
        if (producto.getDescripcion() != null && !producto.getDescripcion().isEmpty()) {
            binding.tvDescripcion.setText(producto.getDescripcion());
        } else {
            binding.tvDescripcion.setText("Sin descripción disponible");
        }

        // Categoría
        if (producto.getCategoria() != null) {
            binding.tvCategoria.setText(producto.getCategoria().getNombre());
            binding.tvCategoria.setVisibility(View.VISIBLE);
        } else {
            binding.tvCategoria.setVisibility(View.GONE);
        }

        // Verificar disponibilidad
        if (!producto.isDisponible()) {
            binding.btnAgregarAlCarrito.setEnabled(false);
            binding.btnAgregarAlCarrito.setText("No disponible");
            binding.btnIncrement.setEnabled(false);
            binding.btnDecrement.setEnabled(false);
            Toast.makeText(requireContext(), "Este producto no está disponible actualmente", Toast.LENGTH_SHORT).show();
        } else {
            binding.btnAgregarAlCarrito.setEnabled(true);
            binding.btnAgregarAlCarrito.setText("Agregar al carrito");
            binding.btnIncrement.setEnabled(true);
            binding.btnDecrement.setEnabled(true);
        }
    }

    private void updateCantidad() {
        binding.tvCantidad.setText(String.valueOf(cantidad));
    }

    private void agregarAlCarrito() {
        if (currentProducto == null) {
            Toast.makeText(requireContext(), "Error al cargar el producto", Toast.LENGTH_SHORT).show();
            return;
        }

        // Usar una instancia de CarritoViewModel para agregar el producto al carrito
        carritoViewModel.addToCart(currentProducto.getId(), cantidad);

        // Mostrar mensaje de éxito
        Toast.makeText(requireContext(), "Producto agregado al carrito", Toast.LENGTH_SHORT).show();

        // Opcional: navegar al carrito o regresar
        navigateUp();
    }

    private void navigateUp() {
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
        navController.navigateUp();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}