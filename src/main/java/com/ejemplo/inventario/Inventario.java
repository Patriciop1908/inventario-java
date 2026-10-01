package com.ejemplo.inventario;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Gestiona un conjunto de productos identificados por su nombre.
 * <p>
 * Los nombres no distinguen mayúsculas/minúsculas: "Teclado" y "teclado"
 * se refieren al mismo producto.
 */
public class Inventario {

    // LinkedHashMap conserva el orden en que se agregaron los productos al listarlos
    private final Map<String, Producto> productos = new LinkedHashMap<>();

    /**
     * Agrega un producto nuevo al inventario.
     *
     * @param producto producto a agregar
     * @throws IllegalArgumentException si ya existe un producto con el mismo nombre
     */
    public void agregarProducto(Producto producto) {
        Objects.requireNonNull(producto, "El producto no puede ser nulo");
        String clave = normalizar(producto.getNombre());
        if (productos.containsKey(clave)) {
            throw new IllegalArgumentException(
                    "Ya existe un producto con el nombre '" + producto.getNombre() + "'");
        }
        productos.put(clave, producto);
    }

    /**
     * Retira unidades del stock de un producto.
     *
     * @param nombre   nombre del producto
     * @param unidades unidades a retirar (debe ser positivo)
     * @throws ProductoNoEncontradoException si el producto no existe
     * @throws StockInsuficienteException    si no hay unidades suficientes
     */
    public void quitarStock(String nombre, int unidades) {
        buscar(nombre).retirarStock(unidades);
    }

    /**
     * Devuelve la cantidad disponible de un producto.
     *
     * @param nombre nombre del producto
     * @return unidades en stock
     * @throws ProductoNoEncontradoException si el producto no existe
     */
    public int consultarStock(String nombre) {
        return buscar(nombre).getCantidad();
    }

    /**
     * Devuelve todos los productos en el orden en que se agregaron.
     * La lista es inmutable para que no se pueda modificar el inventario desde fuera.
     */
    public List<Producto> listarProductos() {
        return List.copyOf(productos.values());
    }

    /**
     * Devuelve el producto con menos unidades disponibles.
     * Si varios productos empatan, se devuelve el que se agregó primero.
     *
     * @return el producto con menor stock
     * @throws InventarioVacioException si el inventario no tiene productos
     */
    public Producto obtenerProductoConMenorStock() {
        // min() conserva el primer elemento en caso de empate, y LinkedHashMap mantiene el orden de inserción
        return productos.values().stream()
                .min(Comparator.comparingInt(Producto::getCantidad))
                .orElseThrow(InventarioVacioException::new);
    }

    /**
     * Calcula el valor económico total del inventario.
     */
    public BigDecimal valorTotal() {
        return productos.values().stream()
                .map(Producto::getValorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Busca un producto por nombre o lanza una excepción si no existe. */
    private Producto buscar(String nombre) {
        Producto producto = productos.get(normalizar(nombre));
        if (producto == null) {
            throw new ProductoNoEncontradoException(nombre);
        }
        return producto;
    }

    /** Convierte el nombre a una clave uniforme (sin espacios extremos y en minúsculas). */
    private static String normalizar(String nombre) {
        Objects.requireNonNull(nombre, "El nombre no puede ser nulo");
        return nombre.trim().toLowerCase(Locale.ROOT);
    }
}
