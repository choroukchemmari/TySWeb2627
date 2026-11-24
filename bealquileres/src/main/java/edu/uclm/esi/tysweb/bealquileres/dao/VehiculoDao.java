package edu.uclm.esi.tysweb.bealquileres.dao;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import edu.uclm.esi.tysweb.bealquileres.model.Estacion;
import edu.uclm.esi.tysweb.bealquileres.model.Municipio;
import edu.uclm.esi.tysweb.bealquileres.model.Vehiculo;

public interface VehiculoDao extends JpaRepository<Vehiculo, Long> {

    List<Vehiculo> findByMunicipioIsNull();

    Integer countByMunicipioIdIsNull();

    long countByEstacion(Estacion estacion);

    List<Vehiculo> findByReservaId(String reservaId);

    @Query(value = "SELECT * FROM vehiculo WHERE municipio_id IS NULL LIMIT :cantidad", nativeQuery = true)
    List<Vehiculo> getBicisLibres(Municipio municipio, @Param("cantidad") Integer cantidad);

    // Una bicicleta está libre si no tiene municipio, o si su prerreserva ha caducado
    @Query(value = "SELECT COUNT(*) FROM vehiculo WHERE municipio_id IS NULL "
            + "OR (prerreserva_expira IS NOT NULL AND prerreserva_expira < :ahora)", nativeQuery = true)
    long contarLibres(@Param("ahora") LocalDateTime ahora);

    // FOR UPDATE bloquea las filas hasta el final de la transacción (RC-CON-03)
    @Query(value = "SELECT * FROM vehiculo WHERE municipio_id IS NULL "
            + "OR (prerreserva_expira IS NOT NULL AND prerreserva_expira < :ahora) "
            + "ORDER BY id LIMIT :cantidad FOR UPDATE", nativeQuery = true)
    List<Vehiculo> bloquearLibres(@Param("ahora") LocalDateTime ahora, @Param("cantidad") int cantidad);

}