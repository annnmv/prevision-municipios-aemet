package com.meteo.app.domain.model;

/** Unidades aceptadas: G_CEL (defecto) y G_FAH. */
public enum UnidadTemperatura {
    G_CEL,
    G_FAH;

    public static UnidadTemperatura desde(String valor) {
        if (valor == null || valor.isBlank()) return G_CEL;
        try {
            return UnidadTemperatura.valueOf(valor.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unidad no válida. Valores: G_CEL / G_FAH");
        }
    }

    public double convertirDesdeCelsius(double celsius) {
        return this == G_FAH ? celsius * 9 / 5 + 32 : celsius;
    }
}
