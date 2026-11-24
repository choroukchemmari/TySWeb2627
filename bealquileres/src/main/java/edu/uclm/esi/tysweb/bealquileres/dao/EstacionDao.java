package edu.uclm.esi.tysweb.bealquileres.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import edu.uclm.esi.tysweb.bealquileres.model.Estacion;
import edu.uclm.esi.tysweb.bealquileres.model.Feature;
import jakarta.persistence.LockModeType;

public interface EstacionDao extends JpaRepository<Estacion, String> {

    boolean existsByFeature(Feature feature);

    // Bloquea la fila de la estación hasta que termine la transacción,
    // para que dos peticiones simultáneas no superen su capacidad (RC-CON)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Estacion e where e.id = :id")
    Optional<Estacion> findByIdForUpdate(@Param("id") String id);
}