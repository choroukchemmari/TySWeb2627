package edu.uclm.esi.tysweb.beusu.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.uclm.esi.tysweb.beusu.model.Cuenta;

public interface CuentaDao extends JpaRepository<Cuenta, Long> {

    Cuenta findByCorreo(String correo);

    Cuenta findByToken(String token);

}