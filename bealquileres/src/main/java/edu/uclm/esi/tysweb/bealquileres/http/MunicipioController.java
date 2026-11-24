package edu.uclm.esi.tysweb.bealquileres.http;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import edu.uclm.esi.tysweb.bealquileres.dto.MunicipioRequest;
import edu.uclm.esi.tysweb.bealquileres.dto.SesionDto;
import edu.uclm.esi.tysweb.bealquileres.services.MunicipioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/municipios")
@CrossOrigin("*")
@Tag(
    name = "Municipios",
    description = "Operaciones relacionadas con la gestión de municipios por parte de los ayuntamientos"
)
public class MunicipioController {

    private final MunicipioService municipioService;

    public MunicipioController(MunicipioService municipioService) {
        this.municipioService = municipioService;
    }

    @GetMapping("/getNumeroDeDirecciones") @Operation(summary = "Devuelve el número de direcciones del municipio pasado como parámetro")
    public Integer getNumeroDeCalles(@RequestParam @Parameter(description = "Nombre de la ciudad") String municipio) {
        return this.municipioService.getNumeroDeDirecciones(municipio);
    }

    @PostMapping("/load")
    @Operation(summary = "Carga en la base de datos los puntos de interés del municipio del empleado (el municipio lo certifica beusu)")
    public void load(@RequestParam(required = false) @Parameter(description = "Índice de la dirección inicial (por defecto, 0)") Integer offset,
        @RequestParam(required = false) @Parameter(description = "Índice de la dirección final (por defecto, todas)") Integer limit,
        @RequestParam(required = false) @Parameter(description = "Mostrar o no progreso") Boolean progress,
        @RequestBody(required = false) MunicipioRequest request,
        @RequestAttribute("sesion") SesionDto sesion) {

        // Requisito 9.1: el municipio sale de beusu, nunca del cliente
        String municipio = sesion.municipio();

        if (request != null && request.municipio() != null && !request.municipio().equalsIgnoreCase(municipio))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo puedes cargar tu propio municipio");

        int desde = (offset == null) ? 0 : offset;
        int hasta = (limit == null) ? this.municipioService.getNumeroDeDirecciones(municipio) : limit;

        this.municipioService.load(municipio, desde, hasta, progress);
    }
}