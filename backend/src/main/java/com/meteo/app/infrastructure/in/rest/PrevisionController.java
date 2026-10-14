package com.meteo.app.infrastructure.in.rest;

import com.meteo.app.domain.model.PrevisionDia;
import com.meteo.app.domain.model.UnidadTemperatura;
import com.meteo.app.domain.port.in.ObtenerPrevisionUseCase;
import org.springframework.web.bind.annotation.*;

/** Previsión del día siguiente por municipio. */
@RestController
@RequestMapping("/api/prevision")
@CrossOrigin(origins = "http://localhost:4200")
public class PrevisionController {

    private final ObtenerPrevisionUseCase useCase;

    public PrevisionController(ObtenerPrevisionUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping("/{codigo}")
    public PrevisionDia prevision(@PathVariable String codigo,
                                  @RequestParam(name = "unidad", required = false) String unidad) {
        return useCase.previsionDiaSiguiente(codigo, UnidadTemperatura.desde(unidad));
    }
}
