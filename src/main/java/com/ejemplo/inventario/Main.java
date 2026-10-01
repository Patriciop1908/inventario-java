package com.ejemplo.inventario;

import java.math.BigDecimal;

/**
 * Programa de demostración del uso de {@link Inventario} y {@link Producto}.
 */
public class Main {

    public static void main(String[] args) {
        Inventario inventario = new Inventario();

        // 1. Agregar productos
        inventario.agregarProducto(new Producto("Teclado", new BigDecimal("25.50"), 10));
        inventario.agregarProducto(new Producto("Ratón", new BigDecimal("12.99"), 25));
        inventario.agregarProducto(new Producto("Monitor", new BigDecimal("189.00"), 4));

        System.out.println("=== Inventario inicial ===");
        imprimirInventario(inventario);

        // 2. Quitar stock (una venta)
        System.out.println("\nVendiendo 3 teclados...");
        inventario.quitarStock("teclado", 3); // el nombre no distingue mayúsculas
        System.out.println("Stock de Teclado: " + inventario.consultarStock("Teclado"));

        // 3. Casos de error controlados
        System.out.println("\n=== Casos de error ===");
        try {
            inventario.quitarStock("Monitor", 10);
        } catch (StockInsuficienteException e) {
            System.out.println("Error: " + e.getMessage());
        }

        try {
            inventario.consultarStock("Impresora");
        } catch (ProductoNoEncontradoException e) {
            System.out.println("Error: " + e.getMessage());
        }

        try {
            inventario.agregarProducto(new Producto("RATÓN", new BigDecimal("9.99"), 5));
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }

        // 4. Listado final
        System.out.println("\n=== Inventario final ===");
        imprimirInventario(inventario);
    }

    /** Muestra todos los productos y el valor total del inventario. */
    private static void imprimirInventario(Inventario inventario) {
        inventario.listarProductos().forEach(System.out::println);
        System.out.println("Valor total: " + inventario.valorTotal() + " €");
    }
}
