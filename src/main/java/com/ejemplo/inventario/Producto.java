package com.ejemplo.inventario;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Representa un producto del inventario.
 * <p>
 * El nombre y el precio son inmutables; la cantidad en stock solo puede
 * modificarse desde el propio paquete (a través de {@link Inventario}),
 * de modo que el código externo no pueda alterar el stock sin pasar por
 * las validaciones del inventario.
 */
public class Producto {

    private final String nombre;
    // Se usa BigDecimal para el precio: evita los errores de redondeo de double con dinero
    private final BigDecimal precio;
    private int cantidad;

    /**
     * Crea un nuevo producto.
     *
     * @param nombre   nombre del producto (no puede estar vacío)
     * @param precio   precio unitario (no puede ser negativo)
     * @param cantidad cantidad inicial en stock (no puede ser negativa)
     * @throws IllegalArgumentException si algún dato no es válido
     */
    public Producto(String nombre, BigDecimal precio, int cantidad) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del producto no puede estar vacío");
        }
        Objects.requireNonNull(precio, "El precio no puede ser nulo");
        if (precio.signum() < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo: " + precio);
        }
        if (cantidad < 0) {
            throw new IllegalArgumentException("La cantidad inicial no puede ser negativa: " + cantidad);
        }
        this.nombre = nombre.trim();
        this.precio = precio;
        this.cantidad = cantidad;
    }

    public String getNombre() {
        return nombre;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public int getCantidad() {
        return cantidad;
    }

    /**
     * Calcula el valor total del stock de este producto (precio × cantidad).
     */
    public BigDecimal getValorTotal() {
        return precio.multiply(BigDecimal.valueOf(cantidad));
    }

    /**
     * Resta unidades del stock. Visibilidad de paquete: solo el inventario lo usa.
     *
     * @param unidades número de unidades a retirar (debe ser positivo)
     * @throws IllegalArgumentException   si las unidades no son positivas
     * @throws StockInsuficienteException si no hay stock suficiente
     */
    void retirarStock(int unidades) {
        if (unidades <= 0) {
            throw new IllegalArgumentException("Las unidades a retirar deben ser positivas: " + unidades);
        }
        if (unidades > cantidad) {
            throw new StockInsuficienteException(nombre, unidades, cantidad);
        }
        cantidad -= unidades;
    }

    /**
     * Dos productos se consideran iguales si tienen el mismo nombre
     * (sin distinguir mayúsculas/minúsculas), igual que en el inventario.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Producto otro)) {
            return false;
        }
        return nombre.equalsIgnoreCase(otro.nombre);
    }

    @Override
    public int hashCode() {
        return nombre.toLowerCase().hashCode();
    }

    @Override
    public String toString() {
        return String.format("%-12s | precio: %8s € | stock: %4d", nombre, precio, cantidad);
    }
}
