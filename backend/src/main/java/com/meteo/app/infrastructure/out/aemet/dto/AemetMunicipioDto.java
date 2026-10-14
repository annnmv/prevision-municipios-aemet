package com.meteo.app.infrastructure.out.aemet.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Municipio del maestro de AEMET. El id viene como "id28079". */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AemetMunicipioDto(String id, String nombre) {
    public String codigo() {
        if (id == null) return "";
        return id.startsWith("id") ? id.substring(2) : id;
    }
}
