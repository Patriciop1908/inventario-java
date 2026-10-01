package com.ejemplo.inventario;

/**
 * Se lanza cuando se intenta retirar más unidades de las disponibles en stock.
 */
public class StockInsuficienteException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public StockInsuficienteException(String nombreProducto, int solicitadas, int disponibles) {
        super(String.format("Stock insuficiente de '%s': se solicitaron %d unidades y solo hay %d",
                nombreProducto, solicitadas, disponibles));
    }
}
