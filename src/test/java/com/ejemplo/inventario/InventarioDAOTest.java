package com.ejemplo.inventario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Pruebas de {@link InventarioDAO}.
 * <p>
 * Cada prueba usa un archivo SQLite nuevo dentro de una carpeta temporal
 * que JUnit borra al terminar, así las pruebas no se afectan entre sí
 * ni tocan el {@code inventario.db} real.
 */
class InventarioDAOTest {

    @TempDir
    Path carpetaTemporal;

    private Path archivo;
    private InventarioDAO dao;

    @BeforeEach
    void preparar() {
        archivo = carpetaTemporal.resolve("prueba.db");
        dao = new InventarioDAO(archivo);
        dao.crearTablaSiNoExiste();
    }

    @Test
    void crearTablaDosVecesNoFalla() {
        dao.crearTablaSiNoExiste();
        assertTrue(dao.listarTodos().isEmpty());
    }

    @Test
    void insertarYConsultarProductoConservaTodosLosDatos() {
        dao.insertarProducto(new Producto("Teclado", new BigDecimal("25.50"), 10));

        Producto leido = dao.consultarProducto("Teclado").orElseThrow();
        assertEquals("Teclado", leido.getNombre());
        assertEquals(new BigDecimal("25.50"), leido.getPrecio()); // se conserva la escala exacta
        assertEquals(10, leido.getCantidad());
    }

    @Test
    void consultarNoDistingueMayusculas() {
        dao.insertarProducto(new Producto("Ratón", new BigDecimal("12.99"), 5));

        assertEquals("Ratón", dao.consultarProducto("  RATÓN ").orElseThrow().getNombre());
    }

    @Test
    void consultarProductoInexistenteDevuelveVacio() {
        assertEquals(Optional.empty(), dao.consultarProducto("Impresora"));
    }

    @Test
    void insertarProductoDuplicadoLanzaExcepcion() {
        dao.insertarProducto(new Producto("Ratón", new BigDecimal("12.99"), 5));
        Producto duplicado = new Producto("RATÓN", BigDecimal.ONE, 1);

        assertThrows(IllegalArgumentException.class, () -> dao.insertarProducto(duplicado));
    }

    @Test
    void actualizarStockGuardaLaNuevaCantidad() {
        dao.insertarProducto(new Producto("Monitor", new BigDecimal("189.00"), 4));

        dao.actualizarStock("monitor", 1);

        assertEquals(1, dao.consultarProducto("Monitor").orElseThrow().getCantidad());
    }

    @Test
    void actualizarStockDeProductoInexistenteLanzaExcepcion() {
        assertThrows(ProductoNoEncontradoException.class, () -> dao.actualizarStock("Impresora", 3));
    }

    @Test
    void actualizarStockNegativoLanzaExcepcion() {
        dao.insertarProducto(new Producto("Monitor", new BigDecimal("189.00"), 4));

        assertThrows(IllegalArgumentException.class, () -> dao.actualizarStock("Monitor", -1));
        assertEquals(4, dao.consultarProducto("Monitor").orElseThrow().getCantidad());
    }

    @Test
    void listarTodosDevuelveLosProductosEnOrdenDeInsercion() {
        dao.insertarProducto(new Producto("Teclado", new BigDecimal("25.50"), 10));
        dao.insertarProducto(new Producto("Ratón", new BigDecimal("12.99"), 25));
        dao.insertarProducto(new Producto("Monitor", new BigDecimal("189.00"), 4));

        List<String> nombres = dao.listarTodos().stream().map(Producto::getNombre).toList();
        assertEquals(List.of("Teclado", "Ratón", "Monitor"), nombres);
    }

    @Test
    void losDatosPersistenEntreInstanciasDelDao() {
        dao.insertarProducto(new Producto("Teclado", new BigDecimal("25.50"), 10));
        dao.actualizarStock("Teclado", 7);

        // Un DAO nuevo sobre el mismo archivo simula reiniciar la aplicación
        InventarioDAO otroDao = new InventarioDAO(archivo);
        assertEquals(7, otroDao.consultarProducto("Teclado").orElseThrow().getCantidad());
    }
}
