package com.meteo.app.domain.port.in;

import com.meteo.app.domain.model.PrevisionDia;
import com.meteo.app.domain.model.UnidadTemperatura;

/** Previsión del día siguiente para un municipio. */
public interface ObtenerPrevisionUseCase {
    PrevisionDia previsionDiaSiguiente(String codigoMunicipio, UnidadTemperatura unidad);
}
