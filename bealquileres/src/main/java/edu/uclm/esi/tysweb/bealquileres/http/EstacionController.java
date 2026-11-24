package edu.uclm.esi.tysweb.bealquileres.http;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.uclm.esi.tysweb.bealquileres.dto.EstacionDto;
import edu.uclm.esi.tysweb.bealquileres.dto.EstacionRequestDto;
import edu.uclm.esi.tysweb.bealquileres.dto.SesionDto;
import edu.uclm.esi.tysweb.bealquileres.services.EstacionService;

@RestController
@RequestMapping("/api/estaciones")
@CrossOrigin("*")
public class EstacionController {

    private final EstacionService service;

    public EstacionController(EstacionService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<EstacionDto> ubicar(@RequestBody EstacionRequestDto body,
            @RequestAttribute("sesion") SesionDto sesion) {
        EstacionDto dto = this.service.ubicar(sesion.municipio(), body.featureId(), body.capacidad());
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }
}