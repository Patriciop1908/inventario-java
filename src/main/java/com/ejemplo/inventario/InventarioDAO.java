package com.ejemplo.inventario;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.sqlite.SQLiteErrorCode;
import org.sqlite.SQLiteException;

/**
 * Objeto de acceso a datos (DAO) que guarda los productos en una base de datos SQLite.
 * <p>
 * Cada operación abre su propia conexión y la cierra al terminar
 * (try-with-resources). Con SQLite esto es barato y evita dejar
 * conexiones abiertas por error.
 * <p>
 * Igual que {@link Inventario}, los nombres no distinguen mayúsculas:
 * se guarda una columna {@code clave} con el nombre normalizado, que es
 * la clave primaria, y otra {@code nombre} con el nombre tal como se escribió.
 */
public class InventarioDAO {

    /** Nombre del archivo de base de datos que usa la aplicación por defecto. */
    public static final String ARCHIVO_POR_DEFECTO = "inventario.db";

    // El precio se guarda como TEXT para conservar exactamente el valor del BigDecimal
    // (un REAL de SQLite es un double y podría introducir errores de redondeo)
    private static final String SQL_CREAR_TABLA = """
            CREATE TABLE IF NOT EXISTS productos (
                clave    TEXT    PRIMARY KEY,
                nombre   TEXT    NOT NULL,
                precio   TEXT    NOT NULL,
                cantidad INTEGER NOT NULL CHECK (cantidad >= 0)
            )""";
    private static final String SQL_INSERTAR =
            "INSERT INTO productos (clave, nombre, precio, cantidad) VALUES (?, ?, ?, ?)";
    private static final String SQL_ACTUALIZAR_STOCK =
            "UPDATE productos SET cantidad = ? WHERE clave = ?";
    private static final String SQL_CONSULTAR =
            "SELECT nombre, precio, cantidad FROM productos WHERE clave = ?";
    // rowid refleja el orden de inserción, igual que el listado de Inventario
    private static final String SQL_LISTAR =
            "SELECT nombre, precio, cantidad FROM productos ORDER BY rowid";

    private final String url;

    /**
     * Crea un DAO que trabaja sobre el archivo de base de datos indicado.
     * El archivo se crea automáticamente la primera vez que se usa.
     *
     * @param archivo ruta del archivo SQLite
     */
    public InventarioDAO(Path archivo) {
        Objects.requireNonNull(archivo, "La ruta de la base de datos no puede ser nula");
        this.url = "jdbc:sqlite:" + archivo.toAbsolutePath();
    }

    /**
     * Crea la tabla de productos si todavía no existe.
     */
    public void crearTablaSiNoExiste() {
        try (Connection conexion = conectar();
             Statement sentencia = conexion.createStatement()) {
            sentencia.execute(SQL_CREAR_TABLA);
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo crear la tabla de productos", e);
        }
    }

    /**
     * Guarda un producto nuevo.
     *
     * @param producto producto a guardar
     * @throws IllegalArgumentException si ya existe un producto con el mismo nombre
     */
    public void insertarProducto(Producto producto) {
        Objects.requireNonNull(producto, "El producto no puede ser nulo");
        try (Connection conexion = conectar();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_INSERTAR)) {
            sentencia.setString(1, Inventario.normalizar(producto.getNombre()));
            sentencia.setString(2, producto.getNombre());
            sentencia.setString(3, producto.getPrecio().toPlainString());
            sentencia.setInt(4, producto.getCantidad());
            sentencia.executeUpdate();
        } catch (SQLiteException e) {
            if (e.getResultCode() == SQLiteErrorCode.SQLITE_CONSTRAINT_PRIMARYKEY) {
                throw new IllegalArgumentException(
                        "Ya existe un producto con el nombre '" + producto.getNombre() + "'", e);
            }
            throw new PersistenciaException("No se pudo insertar el producto '" + producto.getNombre() + "'", e);
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo insertar el producto '" + producto.getNombre() + "'", e);
        }
    }

    /**
     * Cambia la cantidad en stock de un producto ya guardado.
     *
     * @param nombre        nombre del producto
     * @param nuevaCantidad nueva cantidad (no puede ser negativa)
     * @throws IllegalArgumentException      si la cantidad es negativa
     * @throws ProductoNoEncontradoException si el producto no existe
     */
    public void actualizarStock(String nombre, int nuevaCantidad) {
        if (nuevaCantidad < 0) {
            throw new IllegalArgumentException("La cantidad no puede ser negativa: " + nuevaCantidad);
        }
        try (Connection conexion = conectar();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_ACTUALIZAR_STOCK)) {
            sentencia.setInt(1, nuevaCantidad);
            sentencia.setString(2, Inventario.normalizar(nombre));
            // Si no se modificó ninguna fila, el producto no existe
            if (sentencia.executeUpdate() == 0) {
                throw new ProductoNoEncontradoException(nombre);
            }
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo actualizar el stock de '" + nombre + "'", e);
        }
    }

    /**
     * Busca un producto por nombre.
     *
     * @param nombre nombre del producto
     * @return el producto, o {@link Optional#empty()} si no existe
     */
    public Optional<Producto> consultarProducto(String nombre) {
        try (Connection conexion = conectar();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_CONSULTAR)) {
            sentencia.setString(1, Inventario.normalizar(nombre));
            try (ResultSet fila = sentencia.executeQuery()) {
                return fila.next() ? Optional.of(leerProducto(fila)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo consultar el producto '" + nombre + "'", e);
        }
    }

    /**
     * Devuelve todos los productos guardados, en el orden en que se insertaron.
     */
    public List<Producto> listarTodos() {
        try (Connection conexion = conectar();
             Statement sentencia = conexion.createStatement();
             ResultSet filas = sentencia.executeQuery(SQL_LISTAR)) {
            List<Producto> productos = new ArrayList<>();
            while (filas.next()) {
                productos.add(leerProducto(filas));
            }
            return productos;
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudieron listar los productos", e);
        }
    }

    private Connection conectar() throws SQLException {
        return DriverManager.getConnection(url);
    }

    /** Convierte la fila actual del ResultSet en un Producto. */
    private static Producto leerProducto(ResultSet fila) throws SQLException {
        return new Producto(
                fila.getString("nombre"),
                new BigDecimal(fila.getString("precio")),
                fila.getInt("cantidad"));
    }
}
