package com.meteo.app.infrastructure;

import com.meteo.app.domain.model.PrevisionDia;
import com.meteo.app.domain.model.UnidadTemperatura;
import com.meteo.app.domain.port.in.ObtenerPrevisionUseCase;
import com.meteo.app.infrastructure.in.rest.PrevisionController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** La respuesta trae media, unidad y los 4 tramos en orden. */
@WebMvcTest(PrevisionController.class)
class PrevisionControllerTest {

    @Autowired MockMvc mvc;
    @MockBean ObtenerPrevisionUseCase useCase;

    private PrevisionDia muestra() {
        return new PrevisionDia(66.7, UnidadTemperatura.G_FAH, List.of(
                new PrevisionDia.TramoPrecipitacion(5, "00-06"),
                new PrevisionDia.TramoPrecipitacion(10, "06-12"),
                new PrevisionDia.TramoPrecipitacion(10, "12-18"),
                new PrevisionDia.TramoPrecipitacion(5, "18-24")));
    }

    @Test
    void formatoIgualAlDelEjemplo() throws Exception {
        when(useCase.previsionDiaSiguiente("28079", UnidadTemperatura.G_FAH)).thenReturn(muestra());

        mvc.perform(get("/api/prevision/28079").param("unidad", "G_FAH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mediaTemperatura").value(66.7))
                .andExpect(jsonPath("$.unidadTemperatura").value("G_FAH"))
                .andExpect(jsonPath("$.probPrecipitacion.length()").value(4))
                .andExpect(jsonPath("$.probPrecipitacion[0].periodo").value("00-06"))
                .andExpect(jsonPath("$.probPrecipitacion[0].probabilidad").value(5));
    }

    @Test
    void sinUnidadPorDefectoCelsius() throws Exception {
        when(useCase.previsionDiaSiguiente("28079", UnidadTemperatura.G_CEL))
                .thenReturn(new PrevisionDia(20.0, UnidadTemperatura.G_CEL, List.of()));

        mvc.perform(get("/api/prevision/28079"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unidadTemperatura").value("G_CEL"));
    }

    @Test
    void unidadRaraDevuelve400() throws Exception {
        mvc.perform(get("/api/prevision/28079").param("unidad", "KELVIN"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void siAemetFalla_devuelve502ConMensaje() throws Exception {
        when(useCase.previsionDiaSiguiente("28079", UnidadTemperatura.G_CEL))
                .thenThrow(new RuntimeException("AEMET caído"));

        mvc.perform(get("/api/prevision/28079"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.error").value("No se pudo obtener el dato de AEMET"));
    }
}
