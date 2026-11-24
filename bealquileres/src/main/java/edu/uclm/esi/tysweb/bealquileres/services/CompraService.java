package edu.uclm.esi.tysweb.bealquileres.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import edu.uclm.esi.tysweb.bealquileres.dao.FeatureDao;
import edu.uclm.esi.tysweb.bealquileres.dao.MunicipioDao;
import edu.uclm.esi.tysweb.bealquileres.dao.VehiculoDao;
import edu.uclm.esi.tysweb.bealquileres.dto.PrerreservaDto;
import edu.uclm.esi.tysweb.bealquileres.model.Municipio;
import edu.uclm.esi.tysweb.bealquileres.model.Vehiculo;

@Service
public class CompraService {

    private final VehiculoDao vehiculoDao;
    private final MunicipioDao municipioDao;
    private final FeatureDao featureDao;

    @Value("${bealquileres.precio-bicicleta:1200}")
    private int precioBicicleta;

    @Value("${bealquileres.prerreserva.minutos:30}")
    private int minutosPrerreserva;

    public CompraService(VehiculoDao vehiculoDao, MunicipioDao municipioDao, FeatureDao featureDao) {
        this.vehiculoDao = vehiculoDao;
        this.municipioDao = municipioDao;
        this.featureDao = featureDao;
    }

    /**
     * Comprar bicicletas, fase 1: prerreserva. Las bicicletas libres se asignan al
     * municipio del funcionario (sacado de la sesión certificada por beusu) durante
     * unos minutos. Si no se paga a tiempo, vuelven a estar libres.
     */
    @Transactional
    public PrerreservaDto prerreservar(String nombreMunicipio, Integer cantidad) {
        // 400: faltan datos
        if (cantidad == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tienes que indicar la cantidad");

        // 406: cantidad fuera de rango
        if (cantidad < 1 || cantidad > 100)
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "La cantidad tiene que estar entre 1 y 100");

        // 409: precondición: los puntos de interés del municipio deben estar cargados
        Municipio municipio = this.municipioDao.findByName(nombreMunicipio);
        if (municipio == null || !this.featureDao.existsByMunicipio(municipio))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Los puntos de interés del municipio no están cargados");

        // 409: no hay bicicletas suficientes
        LocalDateTime ahora = LocalDateTime.now();
        long libres = this.vehiculoDao.contarLibres(ahora);
        if (cantidad > libres)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Solo hay " + libres + " bicicletas sin asignar");

        // Se bloquean las filas elegidas, para que otra compra simultánea no las preasigne también
        List<Vehiculo> bicis = this.vehiculoDao.bloquearLibres(ahora, cantidad);
        if (bicis.size() < cantidad)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Solo hay " + bicis.size() + " bicicletas sin asignar");

        String reservaId = UUID.randomUUID().toString();
        LocalDateTime expira = ahora.plusMinutes(this.minutosPrerreserva);
        for (Vehiculo v : bicis) {
            v.setMunicipio(municipio);
            v.setReservaId(reservaId);
            v.setPrerreservaExpira(expira);
        }
        this.vehiculoDao.saveAll(bicis);

        return new PrerreservaDto(reservaId, municipio.getName(), cantidad, cantidad * this.precioBicicleta, expira);
    }

    /**
     * Datos de una prerreserva vigente. Lo usa beusu para calcular el importe a cobrar
     * sin fiarse de lo que diga el cliente.
     */
    @Transactional(readOnly = true)
    public PrerreservaDto consultar(String reservaId) {
        List<Vehiculo> bicis = vigentes(reservaId);
        return new PrerreservaDto(reservaId, bicis.get(0).getMunicipio().getName(), bicis.size(),
                bicis.size() * this.precioBicicleta, bicis.get(0).getPrerreservaExpira());
    }

    /**
     * Comprar bicicletas, fase 2: el pago se ha confirmado, así que la asignación pasa a ser definitiva.
     */
    @Transactional
    public void confirmar(String reservaId) {
        List<Vehiculo> bicis = vigentes(reservaId);
        for (Vehiculo v : bicis)
            v.setPrerreservaExpira(null);
        this.vehiculoDao.saveAll(bicis);
    }

    // Bicicletas de una prerreserva que existe, no está confirmada ya y no ha caducado
    private List<Vehiculo> vigentes(String reservaId) {
        List<Vehiculo> bicis = (reservaId == null || reservaId.isBlank()) ? List.of()
                : this.vehiculoDao.findByReservaId(reservaId);

        // 404: no existe (o sus bicicletas se reasignaron tras caducar)
        if (bicis.isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "La prerreserva no existe");

        // 409: ya estaba confirmada
        if (bicis.get(0).getPrerreservaExpira() == null)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La compra ya estaba confirmada");

        // 410: caducó
        if (bicis.get(0).getPrerreservaExpira().isBefore(LocalDateTime.now()))
            throw new ResponseStatusException(HttpStatus.GONE, "La prerreserva ha caducado");

        return bicis;
    }
}