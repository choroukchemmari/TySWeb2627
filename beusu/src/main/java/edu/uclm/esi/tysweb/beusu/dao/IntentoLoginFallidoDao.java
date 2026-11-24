package edu.uclm.esi.tysweb.beusu.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.uclm.esi.tysweb.beusu.model.IntentoLoginFallido;

public interface IntentoLoginFallidoDao extends JpaRepository<IntentoLoginFallido, Long> {
}