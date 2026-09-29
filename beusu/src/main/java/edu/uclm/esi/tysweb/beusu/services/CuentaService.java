package edu.uclm.esi.tysweb.beusu.services;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import edu.uclm.esi.tysweb.beusu.dao.CuentaDao;
import edu.uclm.esi.tysweb.beusu.dto.RegistroRequest;
import edu.uclm.esi.tysweb.beusu.model.Cuenta;

@Service
public class CuentaService {

    @Autowired
    private CuentaDao dao;

    @Autowired
    private IdeeClient ideeClient;

    @Autowired
    private EmailService emailService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public void registrar(RegistroRequest request) {

        if (!request.password().equals(request.confirmacionPassword()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Las contraseñas no coinciden");

        Cuenta existente = this.dao.findByCorreo(request.correo());

        if (existente != null) {
            if (existente.isActiva())
                // Escenario alternativo 2: ya tiene cuenta activa
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una cuenta activa con ese correo");

            // Escenario alternativo 1: cuenta creada pero no activa -> se elimina y se repite el alta
            this.dao.delete(existente);
        }

        if (!this.ideeClient.existeMunicipio(request.municipio()))
            // Escenario alternativo 3: el municipio no existe
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe el municipio " + request.municipio());

        Cuenta cuenta = new Cuenta();
        cuenta.setNombre(request.nombre());
        cuenta.setApellidos(request.apellidos());
        cuenta.setCorreo(request.correo());
        cuenta.setMunicipio(request.municipio());
        cuenta.setLatitud(request.latitud());
        cuenta.setLongitud(request.longitud());
        cuenta.setPassword(this.passwordEncoder.encode(request.password()));

        String token = UUID.randomUUID().toString();
        cuenta.setToken(token);
        cuenta.setFechaCreacionToken(LocalDateTime.now());
        cuenta.setFechaValidacion(null);

        this.dao.save(cuenta);

        this.emailService.enviarConfirmacionRegistro(cuenta.getCorreo(), token);
    }
}