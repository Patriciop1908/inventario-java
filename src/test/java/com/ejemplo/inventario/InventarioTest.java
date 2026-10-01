package com.ejemplo.inventario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Pruebas unitarias de la clase {@link Inventario}.
 */
class InventarioTest {

    private Inventario inventario;

    @BeforeEach
    void preparar() {
        inventario = new Inventario();
        inventario.agregarProducto(new Producto("Teclado", new BigDecimal("25.50"), 10));
    }

    @Test
    void consultarStockDevuelveLaCantidadActual() {
        assertEquals(10, inventario.consultarStock("Teclado"));
    }

    @Test
    void quitarStockReduceLaCantidad() {
        inventario.quitarStock("Teclado", 4);
        assertEquals(6, inventario.consultarStock("Teclado"));
    }

    @Test
    void elNombreNoDistingueMayusculas() {
        inventario.quitarStock("  TECLADO ", 1);
        assertEquals(9, inventario.consultarStock("teclado"));
    }

    @Test
    void quitarMasStockDelDisponibleLanzaExcepcion() {
        assertThrows(StockInsuficienteException.class, () -> inventario.quitarStock("Teclado", 11));
        assertEquals(10, inventario.consultarStock("Teclado")); // el stock no cambia
    }

    @Test
    void quitarUnidadesNoPositivasLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> inventario.quitarStock("Teclado", 0));
    }

    @Test
    void productoInexistenteLanzaExcepcion() {
        assertThrows(ProductoNoEncontradoException.class, () -> inventario.consultarStock("Impresora"));
    }

    @Test
    void agregarProductoDuplicadoLanzaExcepcion() {
        Producto duplicado = new Producto("teclado", BigDecimal.ONE, 1);
        assertThrows(IllegalArgumentException.class, () -> inventario.agregarProducto(duplicado));
    }

    @Test
    void listarProductosDevuelveListaInmutableEnOrden() {
        inventario.agregarProducto(new Producto("Ratón", new BigDecimal("12.99"), 5));
        List<Producto> lista = inventario.listarProductos();

        assertEquals(List.of("Teclado", "Ratón"), lista.stream().map(Producto::getNombre).toList());
        assertThrows(UnsupportedOperationException.class, () -> lista.clear());
    }

    @Test
    void valorTotalSumaPrecioPorCantidad() {
        inventario.agregarProducto(new Producto("Ratón", new BigDecimal("12.99"), 2));
        // 25.50 * 10 + 12.99 * 2 = 280.98
        assertEquals(new BigDecimal("280.98"), inventario.valorTotal());
    }

    @Test
    void productoConDatosInvalidosLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> new Producto(" ", BigDecimal.ONE, 1));
        assertThrows(IllegalArgumentException.class, () -> new Producto("X", new BigDecimal("-1"), 1));
        assertThrows(IllegalArgumentException.class, () -> new Producto("X", BigDecimal.ONE, -1));
    }
}
