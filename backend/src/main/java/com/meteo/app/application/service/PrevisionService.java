package com.meteo.app.application.service;

import com.meteo.app.domain.model.PrevisionDia;
import com.meteo.app.domain.model.UnidadTemperatura;
import com.meteo.app.domain.port.in.ObtenerPrevisionUseCase;
import com.meteo.app.domain.port.out.PrevisionAemetPort;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Previsión del día siguiente. Cada municipio se guarda con caducidad
 * para no llamar a AEMET dos veces por lo mismo.
 */
@Service
public class PrevisionService implements ObtenerPrevisionUseCase {

    private static final List<String> TRAMOS = List.of("00-06", "06-12", "12-18", "18-24");
    private static final ZoneId ZONA = ZoneId.of("Europe/Madrid");

    private final PrevisionAemetPort previsionPort;
    private final Duration ttl;
    private final Map<String, Entrada> cache = new ConcurrentHashMap<>();
    private final Map<String, Object> locks = new ConcurrentHashMap<>();

    public PrevisionService(PrevisionAemetPort previsionPort,
                            @Value("${aemet.prevision-ttl:PT3H}") Duration ttl) {
        this.previsionPort = previsionPort;
        this.ttl = ttl;
    }

    @Override
    public PrevisionDia previsionDiaSiguiente(String codigoMunicipio, UnidadTemperatura unidad) {
        if (codigoMunicipio == null || codigoMunicipio.isBlank()) {
            throw new IllegalArgumentException("Código de municipio obligatorio");
        }
        if (unidad == null) unidad = UnidadTemperatura.G_CEL;
        String codigo = codigoMunicipio.trim();

        Entrada e = cache.get(codigo);
        if (e != null && !e.caducada()) return convertir(e.dia(), unidad);

        synchronized (locks.computeIfAbsent(codigo, k -> new Object())) {
            e = cache.get(codigo);
            if (e != null && !e.caducada()) return convertir(e.dia(), unidad);
            DiaAemet dia = descargarDiaSiguiente(codigo);
            cache.put(codigo, new Entrada(dia, Instant.now().plus(ttl)));
            return convertir(dia, unidad);
        }
    }

    private DiaAemet descargarDiaSiguiente(String codigo) {
        JsonNode root = previsionPort.prediccionDiaria(codigo);
        JsonNode dias = root.path(0).path("prediccion").path("dia");
        if (!dias.isArray() || dias.isEmpty()) {
            throw new IllegalStateException("AEMET no devolvió predicción para " + codigo);
        }
        String manana = LocalDate.now(ZONA).plusDays(1).toString();
        JsonNode elegido = null;
        for (JsonNode d : dias) {
            if (d.path("fecha").asText("").startsWith(manana)) {
                elegido = d;
                break;
            }
        }
        if (elegido == null) elegido = dias.size() > 1 ? dias.get(1) : dias.get(0);

        Map<String, Integer> probs = new java.util.HashMap<>();
        for (JsonNode p : elegido.path("probPrecipitacion")) {
            probs.put(p.path("periodo").asText(""), entero(p.path("value")));
        }
        List<PrevisionDia.TramoPrecipitacion> tramos = new ArrayList<>();
        for (String t : TRAMOS) {
            tramos.add(new PrevisionDia.TramoPrecipitacion(probs.getOrDefault(t, 0), t));
        }

        List<Double> temps = new ArrayList<>();
        for (JsonNode t : elegido.path("temperatura").path("dato")) {
            Double v = decimal(t.path("value"));
            if (v != null) temps.add(v);
        }
        double mediaC;
        if (!temps.isEmpty()) {
            mediaC = temps.stream().mapToDouble(Double::doubleValue).average().orElseThrow();
        } else {
            Double max = decimal(elegido.path("temperatura").path("maxima"));
            Double min = decimal(elegido.path("temperatura").path("minima"));
            if (max == null || min == null) {
                throw new IllegalStateException("AEMET no devolvió temperaturas para " + codigo);
            }
            mediaC = (max + min) / 2;
        }
        return new DiaAemet(redondeo1(mediaC), tramos);
    }

    private PrevisionDia convertir(DiaAemet dia, UnidadTemperatura unidad) {
        return new PrevisionDia(redondeo1(unidad.convertirDesdeCelsius(dia.mediaC)), unidad, dia.tramos());
    }

    private static int entero(JsonNode n) {
        if (n.isNumber()) return n.asInt();
        try {
            return Integer.parseInt(n.asText("0").trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static Double decimal(JsonNode n) {
        if (n.isMissingNode() || n.isNull()) return null;
        if (n.isNumber()) return n.asDouble();
        try {
            return Double.parseDouble(n.asText("").replace(',', '.').trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static double redondeo1(double v) {
        return Math.round(v * 10) / 10.0;
    }

    private record DiaAemet(double mediaC, List<PrevisionDia.TramoPrecipitacion> tramos) {}

    private record Entrada(DiaAemet dia, Instant expira) {
        boolean caducada() {
            return Instant.now().isAfter(expira);
        }
    }
}
