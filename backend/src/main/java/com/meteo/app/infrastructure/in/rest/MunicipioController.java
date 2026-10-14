package com.meteo.app.infrastructure.in.rest;

import com.meteo.app.domain.model.Municipio;
import com.meteo.app.domain.port.in.BuscarMunicipiosUseCase;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Buscador de municipios por nombre. */
@RestController
@RequestMapping("/api/municipios")
@CrossOrigin(origins = "http://localhost:4200")
public class MunicipioController {

    private final BuscarMunicipiosUseCase useCase;

    public MunicipioController(BuscarMunicipiosUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping
    public List<Municipio> buscar(@RequestParam(name = "nombre", required = false) String nombre) {
        return useCase.buscar(nombre);
    }
}
