package com.meteo.app.infrastructure.out.aemet.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Primera respuesta de AEMET: trae la URL donde están los datos. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AemetInitResponse(int estado, String descripcion, String datos, String metadatos) {}
