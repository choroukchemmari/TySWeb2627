package edu.uclm.esi.tysweb.beusu.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.uclm.esi.tysweb.beusu.model.Compra;

public interface CompraDao extends JpaRepository<Compra, String> {

    Compra findByIdTransaccion(String idTransaccion);

    Compra findByReservaId(String reservaId);

}