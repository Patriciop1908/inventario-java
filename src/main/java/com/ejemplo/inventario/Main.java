package com.ejemplo.inventario;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;

/**
 * Programa de demostración del uso de {@link Inventario} con persistencia en SQLite.
 * <p>
 * Los productos se guardan en el archivo {@value InventarioDAO#ARCHIVO_POR_DEFECTO}
 * del directorio de trabajo, así que los cambios de stock se conservan entre
 * ejecuciones: cada vez que se ejecuta, se venden 3 teclados más.
 */
public class Main {

    public static void main(String[] args) {
        InventarioDAO dao = new InventarioDAO(Path.of(InventarioDAO.ARCHIVO_POR_DEFECTO));
        dao.crearTablaSiNoExiste();

        // 1. Cargar los productos guardados (o crear los de ejemplo la primera vez)
        Inventario inventario = cargarInventario(dao);

        System.out.println("=== Inventario inicial ===");
        imprimirInventario(inventario);

        // 2. Quitar stock (una venta) y guardar el cambio en la base de datos
        System.out.println("\nVendiendo 3 teclados...");
        try {
            vender(inventario, dao, "teclado", 3); // el nombre no distingue mayúsculas
            System.out.println("Stock de Teclado: " + inventario.consultarStock("Teclado"));
        } catch (StockInsuficienteException e) {
            System.out.println("Error: " + e.getMessage());
        }

        // 3. Producto con menor stock (útil para saber qué reponer primero)
        Producto menorStock = inventario.obtenerProductoConMenorStock();
        System.out.println("\nProducto a reponer primero: " + menorStock.getNombre()
                + " (" + menorStock.getCantidad() + " unidades)");

        // 4. Casos de error controlados
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
            dao.insertarProducto(new Producto("RATÓN", new BigDecimal("9.99"), 5));
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }

        try {
            new Inventario().obtenerProductoConMenorStock();
        } catch (InventarioVacioException e) {
            System.out.println("Error: " + e.getMessage());
        }

        // 5. Listado final (leído de nuevo de la base de datos para comprobar que se guardó)
        System.out.println("\n=== Productos guardados en " + InventarioDAO.ARCHIVO_POR_DEFECTO + " ===");
        dao.listarTodos().forEach(System.out::println);
    }

    /**
     * Crea un inventario en memoria con los productos de la base de datos.
     * Si la base de datos está vacía (primera ejecución), guarda unos productos de ejemplo.
     */
    private static Inventario cargarInventario(InventarioDAO dao) {
        List<Producto> guardados = dao.listarTodos();
        if (guardados.isEmpty()) {
            System.out.println("Base de datos vacía: se crean los productos de ejemplo.\n");
            dao.insertarProducto(new Producto("Teclado", new BigDecimal("25.50"), 10));
            dao.insertarProducto(new Producto("Ratón", new BigDecimal("12.99"), 25));
            dao.insertarProducto(new Producto("Monitor", new BigDecimal("189.00"), 4));
            guardados = dao.listarTodos();
        } else {
            System.out.println("Cargados " + guardados.size() + " productos de la base de datos.\n");
        }

        Inventario inventario = new Inventario();
        guardados.forEach(inventario::agregarProducto);
        return inventario;
    }

    /**
     * Retira stock del inventario y guarda la nueva cantidad en la base de datos.
     * Primero se modifica el inventario porque es quien valida que haya stock suficiente;
     * si la validación falla, la base de datos no se toca.
     */
    private static void vender(Inventario inventario, InventarioDAO dao, String nombre, int unidades) {
        inventario.quitarStock(nombre, unidades);
        dao.actualizarStock(nombre, inventario.consultarStock(nombre));
    }

    /** Muestra todos los productos y el valor total del inventario. */
    private static void imprimirInventario(Inventario inventario) {
        inventario.listarProductos().forEach(System.out::println);
        System.out.println("Valor total: " + inventario.valorTotal() + " €");
    }
}
