package com.meteo.app.domain.port.out;

import com.meteo.app.domain.model.Municipio;

import java.util.List;

/** Maestro de municipios (AEMET). Cambia poco: se guarda en memoria. */
public interface MunicipiosPort {
    List<Municipio> cargarTodos();
}
