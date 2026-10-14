package com.meteo.app.domain.model;

import java.util.List;

/** Previsión del día siguiente de un municipio. */
public record PrevisionDia(
        double mediaTemperatura,
        UnidadTemperatura unidadTemperatura,
        List<TramoPrecipitacion> probPrecipitacion
) {
    public record TramoPrecipitacion(int probabilidad, String periodo) {}
}
