package com.meteo.app.infrastructure;

import com.meteo.app.domain.model.Municipio;
import com.meteo.app.domain.port.in.BuscarMunicipiosUseCase;
import com.meteo.app.infrastructure.in.rest.MunicipioController;
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

/** El buscador devuelve [{codigo, nombre}] y sin nombre no rompe. */
@WebMvcTest(MunicipioController.class)
class MunicipioControllerTest {

    @Autowired MockMvc mvc;
    @MockBean BuscarMunicipiosUseCase useCase;

    @Test
    void buscaYDevuelveCodigoYNombre() throws Exception {
        when(useCase.buscar("mad")).thenReturn(List.of(
                new Municipio("28079", "Madrid"),
                new Municipio("49103", "Madridanos")));

        mvc.perform(get("/api/municipios").param("nombre", "mad"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].codigo").value("28079"))
                .andExpect(jsonPath("$[0].nombre").value("Madrid"));
    }

    @Test
    void sinParametro_devuelveVacio() throws Exception {
        when(useCase.buscar(null)).thenReturn(List.of());

        mvc.perform(get("/api/municipios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
