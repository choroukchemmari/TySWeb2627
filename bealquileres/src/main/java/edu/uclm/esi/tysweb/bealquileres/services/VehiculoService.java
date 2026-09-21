package edu.uclm.esi.tysweb.bealquileres.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import edu.uclm.esi.tysweb.bealquileres.dao.MunicipioDao;
import edu.uclm.esi.tysweb.bealquileres.dao.VehiculoDao;
import edu.uclm.esi.tysweb.bealquileres.dto.VehiculoDto;
import edu.uclm.esi.tysweb.bealquileres.model.Municipio;
import edu.uclm.esi.tysweb.bealquileres.model.Vehiculo;

@Service
public class VehiculoService {

    @Autowired
    private VehiculoDao dao;

    @Autowired
    private MunicipioDao municipioDao;

    public void createVehicles(Integer numberOfVehicles) {
        List<Vehiculo> vehiculos = new ArrayList<>();
        for (int i = 0; i < numberOfVehicles; i++)
            vehiculos.add(new Vehiculo());
        this.dao.saveAll(vehiculos);
    }

    public void asignarVehiculos(String nombreMunicipio, Integer cantidad) {
        if (cantidad == null || cantidad <= 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La cantidad debe ser mayor que cero");

        Municipio municipio = this.municipioDao.findByName(nombreMunicipio);
        if (municipio == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe el municipio " + nombreMunicipio);

        List<Vehiculo> libres = this.dao.findByMunicipioIsNull();
        if (cantidad > libres.size())
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Solo hay " + libres.size() + " vehículos sin asignar");

        List<Vehiculo> aAsignar = libres.subList(0, cantidad);
        for (Vehiculo vehiculo : aAsignar)
            vehiculo.setMunicipio(municipio);
        this.dao.saveAll(aAsignar);
    }

    public List<VehiculoDto> getVehiculos() {
        return this.dao.findAll().stream()
                .map(v -> new VehiculoDto(v.getId(), v.getMatricula(),
                        v.getLatitud(), v.getLongitud(), v.getDistanciaTotal(), v.getBateria(),
                        v.getActivo(), v.getMunicipio() != null ? v.getMunicipio().getName() : null))
                .toList();
    }
}