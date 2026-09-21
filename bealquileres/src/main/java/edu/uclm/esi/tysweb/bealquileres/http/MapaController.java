package edu.uclm.esi.tysweb.bealquileres.http;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.uclm.esi.tysweb.bealquileres.dto.VehiculoSimulado;
import edu.uclm.esi.tysweb.bealquileres.services.MapaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/mapa")
@CrossOrigin("*")
@Tag(
    name = "Mapa",
    description = "Operaciones para consultar el mapa de un municipio"
)
public class MapaController {

    @Autowired
    private MapaService service;

    @GetMapping("/getVehiculos/{city}")
    @Operation(summary = "Devuelve los vehículos que están circulando en la ciudad pasada como parámetro")
    public List<VehiculoSimulado> getVehiculos(@PathVariable String city) {
        return this.service.getVehiculos(city);
    }
}