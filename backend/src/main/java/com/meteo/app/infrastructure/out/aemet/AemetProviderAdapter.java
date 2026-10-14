package com.meteo.app.infrastructure.out.aemet;

import com.meteo.app.domain.model.Municipio;
import com.meteo.app.domain.port.out.MunicipiosPort;
import com.meteo.app.domain.port.out.PrevisionAemetPort;
import com.meteo.app.infrastructure.out.aemet.dto.AemetInitResponse;
import com.meteo.app.infrastructure.out.aemet.dto.AemetMunicipioDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * Llama a AEMET OpenData. Primero se pide el endpoint y AEMET devuelve
 * la URL con los datos; después se descarga esa URL y se parsea el JSON.
 */
@Component
public class AemetProviderAdapter implements MunicipiosPort, PrevisionAemetPort {

    private final RestClient restClient;
    private final String apiKey;
    private final ObjectMapper objectMapper;

    public AemetProviderAdapter(RestClient.Builder builder,
                                @Value("${aemet.base-url}") String baseUrl,
                                @Value("${aemet.api-key}") String apiKey,
                                ObjectMapper objectMapper) {
        this.restClient = builder.baseUrl(baseUrl).build();
        this.apiKey = apiKey;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<Municipio> cargarTodos() {
        List<AemetMunicipioDto> raw = getDatos("/api/maestro/municipios",
                new TypeReference<>() {});
        return raw.stream()
                .filter(m -> m.codigo() != null && !m.codigo().isBlank()
                        && m.nombre() != null && !m.nombre().isBlank())
                .map(m -> new Municipio(m.codigo(), m.nombre()))
                .toList();
    }

    @Override
    public JsonNode prediccionDiaria(String codigoMunicipio) {
        return getDatosRaw("/api/prediccion/especifica/municipio/diaria/" + codigoMunicipio);
    }

    private <T> T getDatos(String apiPath, TypeReference<T> type) {
        String json = downloadDatosJson(apiPath);
        try {
            return objectMapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("No se pudo parsear la respuesta de AEMET para " + apiPath, e);
        }
    }

    private JsonNode getDatosRaw(String apiPath) {
        String json = downloadDatosJson(apiPath);
        try {
            return objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("No se pudo parsear la respuesta de AEMET para " + apiPath, e);
        }
    }

    private String downloadDatosJson(String apiPath) {
        AemetInitResponse init = restClient.get()
                .uri(apiPath + "?api_key={key}", apiKey)
                .retrieve()
                .body(AemetInitResponse.class);
        if (init == null || init.datos() == null) {
            throw new IllegalStateException("AEMET no devolvió URL de datos para " + apiPath);
        }
        // "datos" ya es la URL final. Se lee como texto porque AEMET no responde application/json.
        String json = RestClient.create().get().uri(init.datos()).retrieve().body(String.class);
        if (json == null || json.isBlank()) {
            throw new IllegalStateException("AEMET devolvió contenido vacío para " + apiPath);
        }
        return json;
    }
}
