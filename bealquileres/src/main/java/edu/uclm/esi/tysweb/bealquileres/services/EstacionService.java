package edu.uclm.esi.tysweb.bealquileres.services;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import edu.uclm.esi.tysweb.bealquileres.dao.EstacionDao;
import edu.uclm.esi.tysweb.bealquileres.dao.FeatureDao;
import edu.uclm.esi.tysweb.bealquileres.dao.MunicipioDao;
import edu.uclm.esi.tysweb.bealquileres.dto.EstacionDto;
import edu.uclm.esi.tysweb.bealquileres.model.Estacion;
import edu.uclm.esi.tysweb.bealquileres.model.Feature;
import edu.uclm.esi.tysweb.bealquileres.model.Municipio;

@Service
public class EstacionService {

    private final EstacionDao estacionDao;
    private final FeatureDao featureDao;
    private final MunicipioDao municipioDao;

    public EstacionService(EstacionDao estacionDao, FeatureDao featureDao, MunicipioDao municipioDao) {
        this.estacionDao = estacionDao;
        this.featureDao = featureDao;
        this.municipioDao = municipioDao;
    }

    /**
     * Ubicar estación. El municipio viene de la sesión certificada por beusu,
     * nunca del cliente (requisito 9.1).
     */
    @Transactional
    public EstacionDto ubicar(String nombreMunicipio, String featureId, Integer capacidad) {
        // 400: faltan datos
        if (featureId == null || featureId.isBlank() || capacidad == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Hay que indicar el punto de interés y la capacidad");

        // 406: capacidad no válida
        if (capacidad < 1)
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "La capacidad debe ser al menos 1");

        // 409: precondición: los puntos de interés del municipio deben estar cargados
        Municipio municipio = this.municipioDao.findByName(nombreMunicipio);
        if (municipio == null || !this.featureDao.existsByMunicipio(municipio))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Los puntos de interés del municipio no están cargados");

        // 404: el punto de interés no existe
        Feature feature = this.featureDao.findById(featureId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El punto de interés no existe"));

        // 403: el punto de interés es de otro municipio
        if (!feature.getMunicipio().getId().equals(municipio.getId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "El punto de interés no pertenece a su municipio");

        // 409: ya hay una estación en ese punto
        if (this.estacionDao.existsByFeature(feature))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya hay una estación en ese punto de interés");

        Estacion estacion = new Estacion();
        estacion.setFeature(feature);
        estacion.setMunicipio(municipio);
        estacion.setCapacidad(capacidad);
        this.estacionDao.save(estacion);

        return new EstacionDto(estacion.getId(), feature.getId(), feature.getLatitude(), feature.getLongitude(),
                estacion.getCapacidad());
    }
}