package edu.uclm.esi.tysweb.bealquileres.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import edu.uclm.esi.tysweb.bealquileres.model.Municipio;
import edu.uclm.esi.tysweb.bealquileres.model.Vehiculo;

public interface VehiculoDao extends JpaRepository<Vehiculo, String> {

    List<Vehiculo> findByMunicipioIsNull();

    Integer countByMunicipioIdIsNull();

    @Query(value = "SELECT * FROM vehiculo WHERE municipio_id IS NULL LIMIT :cantidad", nativeQuery = true)
    List<Vehiculo> getBicisLibres(Municipio municipio, @Param("cantidad") Integer cantidad);

}