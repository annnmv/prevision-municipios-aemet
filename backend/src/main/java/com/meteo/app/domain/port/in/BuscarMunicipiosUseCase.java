package com.meteo.app.domain.port.in;

import com.meteo.app.domain.model.Municipio;

import java.util.List;

/** Buscar municipios por nombre. */
public interface BuscarMunicipiosUseCase {
    List<Municipio> buscar(String nombre);
}
