package com.ejemplo.inventario;

/**
 * Se lanza cuando falla una operación contra la base de datos.
 * <p>
 * Envuelve la {@link java.sql.SQLException} original (accesible con
 * {@link #getCause()}) para que el resto del código no tenga que
 * depender de los detalles de JDBC.
 */
public class PersistenciaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public PersistenciaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
