package com.meteo.app.application;

import com.meteo.app.application.service.PrevisionService;
import com.meteo.app.domain.model.PrevisionDia;
import com.meteo.app.domain.model.UnidadTemperatura;
import com.meteo.app.domain.port.out.PrevisionAemetPort;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PrevisionServiceTest {

    @Mock PrevisionAemetPort port;

    private static final ZoneId ZONA = ZoneId.of("Europe/Madrid");

    private JsonNode muestra(double t6, double t12, int p00, Integer p06) throws Exception {
        String manana = LocalDate.now(ZONA).plusDays(1).toString();
        StringBuilder probs = new StringBuilder("[{\"value\":" + p00 + ",\"periodo\":\"00-06\"},");
        if (p06 != null) probs.append("{\"value\":").append(p06).append(",\"periodo\":\"06-12\"},");
        probs.append("{\"value\":10,\"periodo\":\"12-18\"},{\"value\":5,\"periodo\":\"18-24\"}]");
        String json = "[{\"nombre\":\"Madrid\",\"prediccion\":{\"dia\":["
                + "{\"fecha\":\"" + LocalDate.now(ZONA) + "T00:00:00\","
                + "\"probPrecipitacion\":[],\"temperatura\":{\"dato\":[]}},"
                + "{\"fecha\":\"" + manana + "T00:00:00\","
                + "\"probPrecipitacion\":" + probs + ","
                + "\"temperatura\":{\"dato\":[{\"value\":" + t6 + ",\"hora\":6},"
                + "{\"value\":" + t12 + ",\"hora\":12}]}}]}}]";
        return new ObjectMapper().readTree(json);
    }

    @Test
    void mediaEnCelsiusYCuatroTramos() throws Exception {
        when(port.prediccionDiaria("28079")).thenReturn(muestra(16, 24, 5, 10));

        PrevisionDia r = new PrevisionService(port, Duration.ofHours(3))
                .previsionDiaSiguiente("28079", UnidadTemperatura.G_CEL);

        assertThat(r.mediaTemperatura()).isCloseTo(20.0, within(0.01));
        assertThat(r.unidadTemperatura()).isEqualTo(UnidadTemperatura.G_CEL);
        assertThat(r.probPrecipitacion()).extracting(PrevisionDia.TramoPrecipitacion::periodo)
                .containsExactly("00-06", "06-12", "12-18", "18-24");
        assertThat(r.probPrecipitacion()).extracting(PrevisionDia.TramoPrecipitacion::probabilidad)
                .containsExactly(5, 10, 10, 5);
    }

    @Test
    void convierteAFahrenheit() throws Exception {
        when(port.prediccionDiaria("28079")).thenReturn(muestra(20, 20, 0, 0));

        PrevisionDia r = new PrevisionService(port, Duration.ofHours(3))
                .previsionDiaSiguiente("28079", UnidadTemperatura.G_FAH);

        assertThat(r.mediaTemperatura()).isCloseTo(68.0, within(0.01));
        assertThat(r.unidadTemperatura()).isEqualTo(UnidadTemperatura.G_FAH);
    }

    @Test
    void tramoAusenteValeCero() throws Exception {
        when(port.prediccionDiaria("28079")).thenReturn(muestra(20, 20, 5, null));

        PrevisionDia r = new PrevisionService(port, Duration.ofHours(3))
                .previsionDiaSiguiente("28079", null);

        assertThat(r.probPrecipitacion().get(1).probabilidad()).isZero();
        assertThat(r.unidadTemperatura()).isEqualTo(UnidadTemperatura.G_CEL);
    }

    @Test
    void sinDesgloseHorario_mediaDeMaximaYMinima() throws Exception {
        String manana = LocalDate.now(ZONA).plusDays(1).toString();
        String json = "[{\"nombre\":\"Madrid\",\"prediccion\":{\"dia\":["
                + "{\"fecha\":\"" + manana + "T00:00:00\","
                + "\"probPrecipitacion\":[],"
                + "\"temperatura\":{\"dato\":[],\"maxima\":30,\"minima\":20}}]}}]";
        when(port.prediccionDiaria("28079")).thenReturn(new ObjectMapper().readTree(json));

        PrevisionDia r = new PrevisionService(port, Duration.ofHours(3))
                .previsionDiaSiguiente("28079", UnidadTemperatura.G_CEL);

        assertThat(r.mediaTemperatura()).isCloseTo(25.0, within(0.01));
    }

    @Test
    void segundaPeticion_reutilizaCacheSinLlamar() throws Exception {
        when(port.prediccionDiaria("28079")).thenReturn(muestra(20, 20, 0, 0));
        var service = new PrevisionService(port, Duration.ofHours(3));

        service.previsionDiaSiguiente("28079", UnidadTemperatura.G_CEL);
        service.previsionDiaSiguiente("28079", UnidadTemperatura.G_FAH);

        org.mockito.Mockito.verify(port, org.mockito.Mockito.times(1)).prediccionDiaria("28079");
    }
}
