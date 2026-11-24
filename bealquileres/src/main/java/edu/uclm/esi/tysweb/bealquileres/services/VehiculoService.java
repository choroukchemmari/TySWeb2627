package edu.uclm.esi.tysweb.bealquileres.services;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import edu.uclm.esi.tysweb.bealquileres.dao.EstacionDao;
import edu.uclm.esi.tysweb.bealquileres.dao.MunicipioDao;
import edu.uclm.esi.tysweb.bealquileres.dao.VehiculoDao;
import edu.uclm.esi.tysweb.bealquileres.dto.VehiculoDto;
import edu.uclm.esi.tysweb.bealquileres.model.Estacion;
import edu.uclm.esi.tysweb.bealquileres.model.Feature;
import edu.uclm.esi.tysweb.bealquileres.model.Municipio;
import edu.uclm.esi.tysweb.bealquileres.model.Vehiculo;

@Service
public class VehiculoService {

    @Autowired
    private VehiculoDao dao;

    @Autowired
    private MunicipioDao municipioDao;

    @Autowired
    private EstacionDao estacionDao;

    public void createVehicles(Integer numberOfVehicles) {
        List<Vehiculo> vehiculos = new ArrayList<>();
        for (int i = 0; i < numberOfVehicles; i++)
            vehiculos.add(new Vehiculo());
        this.dao.saveAll(vehiculos);
    }

    public void asignarVehiculos(String nombreMunicipio, Integer cantidad) {
        Municipio municipio = this.municipioDao.findByName(nombreMunicipio);
        if (municipio == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe el municipio " + nombreMunicipio);

        Integer libres = this.dao.countByMunicipioIdIsNull();
        if (cantidad > libres)
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Solo hay " + libres + " vehículos sin asignar");

        List<Vehiculo> aAsignar = this.dao.getBicisLibres(municipio, cantidad);
        for (Vehiculo vehiculo : aAsignar)
            vehiculo.setMunicipio(municipio);
        this.dao.saveAll(aAsignar);
    }

    /**
     * Ubicar bicicletas: coloca bicicletas del municipio del funcionario en una
     * estación de recarga de su municipio. El municipio viene de la sesión
     * certificada por beusu, nunca del cliente (requisito 9.1).
     */
    @Transactional
    public void ubicar(String nombreMunicipio, String estacionId, List<Long> vehiculoIds) {
        // 400: faltan datos
        if (estacionId == null || estacionId.isBlank() || vehiculoIds == null || vehiculoIds.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Hay que indicar la estación y al menos una bicicleta");

        // 409: precondición: el municipio debe estar cargado
        Municipio municipio = this.municipioDao.findByName(nombreMunicipio);
        if (municipio == null)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El municipio no está cargado");

        // 404: la estación no existe (se bloquea su fila hasta el final de la transacción)
        Estacion estacion = this.estacionDao.findByIdForUpdate(estacionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "La estación no existe"));

        // 403: la estación es de otro municipio
        if (!estacion.getMunicipio().getId().equals(municipio.getId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "La estación no pertenece a su municipio");

        // 404: alguna bicicleta no existe
        Set<Long> ids = new LinkedHashSet<>(vehiculoIds);
        List<Vehiculo> vehiculos = this.dao.findAllById(ids);
        if (vehiculos.size() != ids.size())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Alguna de las bicicletas no existe");

        int nuevas = 0;
        for (Vehiculo v : vehiculos) {
            // 403: alguna bicicleta es de otro municipio (o no está asignada a ninguno)
            if (v.getMunicipio() == null || !v.getMunicipio().getId().equals(municipio.getId()))
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Alguna de las bicicletas no pertenece a su municipio");
            if (v.getEstacion() == null || !v.getEstacion().getId().equals(estacion.getId()))
                nuevas++;
        }

        // 409: no caben en la estación
        long ocupadas = this.dao.countByEstacion(estacion);
        if (ocupadas + nuevas > estacion.getCapacidad())
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La estación solo admite " + estacion.getCapacidad() + " bicicletas y ya tiene " + ocupadas);

        // Las bicicletas adoptan las coordenadas de la estación
        Feature punto = estacion.getFeature();
        for (Vehiculo v : vehiculos) {
            v.setEstacion(estacion);
            v.setLatitud(punto.getLatitude());
            v.setLongitud(punto.getLongitude());
        }
        this.dao.saveAll(vehiculos);
    }

    public List<VehiculoDto> getVehiculos() {
        return this.dao.findAll().stream()
                .map(v -> new VehiculoDto(v.getId(), v.getMatricula(),
                        v.getLatitud(), v.getLongitud(), v.getDistanciaTotal(), v.getBateria(),
                        v.getActivo(), v.getMunicipio() != null ? v.getMunicipio().getName() : null))
                .toList();
    }
}