package com.meteo.app.application.service;

import com.meteo.app.domain.model.Municipio;
import com.meteo.app.domain.port.in.BuscarMunicipiosUseCase;
import com.meteo.app.domain.port.out.MunicipiosPort;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Buscador de municipios. El maestro de AEMET pesa ~3MB y cambia poco,
 * así que se descarga una sola vez y se filtra en memoria.
 */
@Service
public class MunicipioService implements BuscarMunicipiosUseCase {

    private static final int MAX_RESULTADOS = 20;

    private final MunicipiosPort municipiosPort;
    private final AtomicReference<List<Municipio>> cache = new AtomicReference<>();
    private final Object loadLock = new Object();

    public MunicipioService(MunicipiosPort municipiosPort) {
        this.municipiosPort = municipiosPort;
    }

    @Override
    public List<Municipio> buscar(String nombre) {
        if (nombre == null || nombre.trim().length() < 2) return List.of();
        String q = normalizar(nombre);
        return todos().stream()
                .filter(m -> normalizar(m.nombre()).contains(q))
                .sorted(Comparator.comparingInt((Municipio m) -> posicion(normalizar(m.nombre()), q))
                        .thenComparing(Municipio::nombre))
                .limit(MAX_RESULTADOS)
                .toList();
    }

    private List<Municipio> todos() {
        List<Municipio> v = cache.get();
        if (v != null) return v;
        synchronized (loadLock) {
            v = cache.get();
            if (v != null) return v;
            v = List.copyOf(municipiosPort.cargarTodos());
            cache.set(v);
            return v;
        }
    }

    private static int posicion(String nombre, String q) {
        int i = nombre.indexOf(q);
        return i < 0 ? Integer.MAX_VALUE : i;
    }

    static String normalizar(String s) {
        if (s == null) return "";
        String n = Normalizer.normalize(s.toLowerCase(Locale.ROOT), Normalizer.Form.NFD);
        return n.replaceAll("\\p{M}", "");
    }
}
