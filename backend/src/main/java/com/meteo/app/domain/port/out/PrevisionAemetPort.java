package com.meteo.app.domain.port.out;

import com.fasterxml.jackson.databind.JsonNode;

/** Predicción diaria de AEMET en crudo. La parsea el caso de uso. */
public interface PrevisionAemetPort {
    JsonNode prediccionDiaria(String codigoMunicipio);
}
