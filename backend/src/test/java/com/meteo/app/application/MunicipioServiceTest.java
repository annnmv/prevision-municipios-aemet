package com.meteo.app.application;

import com.meteo.app.application.service.MunicipioService;
import com.meteo.app.domain.model.Municipio;
import com.meteo.app.domain.port.out.MunicipiosPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MunicipioServiceTest {

    @Mock MunicipiosPort port;

    private MunicipioService service() {
        return new MunicipioService(port);
    }

    @Test
    void conMenosDeDosLetrasDevuelveVacio() {
        assertThat(service().buscar("a")).isEmpty();
        assertThat(service().buscar("")).isEmpty();
        assertThat(service().buscar(null)).isEmpty();
    }

    @Test
    void ignoraTildesYMayusculas() {
        when(port.cargarTodos()).thenReturn(List.of(
                new Municipio("28079", "Madrid"),
                new Municipio("46250", "Valencia"),
                new Municipio("29067", "Málaga")));

        assertThat(service().buscar("malaga")).extracting(Municipio::codigo).containsExactly("29067");
        assertThat(service().buscar("MADR")).extracting(Municipio::codigo).containsExactly("28079");
    }

    @Test
    void limitaYDescargaElMaestroUnaSolaVez() {
        var todos = new java.util.ArrayList<Municipio>();
        for (int i = 0; i < 50; i++) todos.add(new Municipio("28" + i, "Madrid " + i));
        when(port.cargarTodos()).thenReturn(todos);

        var s = service();
        assertThat(s.buscar("madrid")).hasSize(20);
        s.buscar("madrid");
        org.mockito.Mockito.verify(port, org.mockito.Mockito.times(1)).cargarTodos();
    }
}
