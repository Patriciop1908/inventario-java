package com.ejemplo.inventario;

/**
 * Se lanza cuando se intenta realizar una operación que necesita al menos
 * un producto sobre un inventario vacío.
 * <p>
 * Hereda de {@link IllegalStateException} porque el error no está en los
 * argumentos recibidos, sino en el estado actual del inventario.
 */
public class InventarioVacioException extends IllegalStateException {

    private static final long serialVersionUID = 1L;

    public InventarioVacioException() {
        super("El inventario está vacío: no hay productos que consultar");
    }
}
