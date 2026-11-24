package edu.uclm.esi.tysweb.beusu.services;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import edu.uclm.esi.tysweb.beusu.dao.CuentaDao;
import edu.uclm.esi.tysweb.beusu.dao.IntentoLoginFallidoDao;
import edu.uclm.esi.tysweb.beusu.dto.LoginRequest;
import edu.uclm.esi.tysweb.beusu.dto.LoginResponse;
import edu.uclm.esi.tysweb.beusu.dto.RegistroRequest;
import edu.uclm.esi.tysweb.beusu.dto.SesionDto;
import edu.uclm.esi.tysweb.beusu.model.Cuenta;
import edu.uclm.esi.tysweb.beusu.model.IntentoLoginFallido;

@Service
public class CuentaService {

    @Autowired
    private CuentaDao dao;

    @Autowired
    private IntentoLoginFallidoDao intentoLoginFallidoDao;

    @Autowired
    private IdeeClient ideeClient;

    @Autowired
    private EmailService emailService;

    @Value("${beusu.token.minutos-caducidad}")
    private long minutosCaducidad;

    @Value("${beusu.sesion.minutos-caducidad}")
    private long minutosCaducidadSesion;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public void registrar(RegistroRequest request) {

        if (!request.password().equals(request.confirmacionPassword()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Las contraseñas no coinciden");

        Cuenta existente = this.dao.findByCorreo(request.correo());

        if (existente != null) {
            if (existente.isActiva())
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una cuenta activa con ese correo");

            this.dao.delete(existente);
        }

        if (!this.ideeClient.existe(request.municipio()))
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

    public void confirmar(String token) {

        Cuenta cuenta = this.dao.findByToken(token);

        if (cuenta == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Token no válido");

        if (cuenta.isActiva())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La cuenta ya está confirmada");

        LocalDateTime caducidad = cuenta.getFechaCreacionToken().plusMinutes(this.minutosCaducidad);

        if (LocalDateTime.now().isAfter(caducidad))
            throw new ResponseStatusException(HttpStatus.GONE, "El token ha caducado, vuelve a registrarte");

        cuenta.setFechaValidacion(LocalDateTime.now());
        this.dao.save(cuenta);

        this.emailService.enviarCuentaConfirmada(cuenta.getCorreo());
    }

    public LoginResponse login(LoginRequest request) {

        Cuenta cuenta = this.dao.findByCorreo(request.correo());

        if (cuenta == null) {
            this.registrarIntentoFallido(request.correo(), "Correo no encontrado");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos");
        }

        if (!this.passwordEncoder.matches(request.password(), cuenta.getPassword())) {
            this.registrarIntentoFallido(request.correo(), "Contraseña incorrecta");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos");
        }

        if (!cuenta.isActiva())
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "La cuenta todavía no ha sido confirmada");

        String tokenSesion = UUID.randomUUID().toString();
        cuenta.setTokenSesion(tokenSesion);
        cuenta.setHoraTokenSesion(LocalDateTime.now());
        this.dao.save(cuenta);

        return new LoginResponse(tokenSesion, cuenta.getNombre(), cuenta.getApellidos(), cuenta.getCorreo());
    }

    public SesionDto validateSession(String tokenSesion) {

        Cuenta cuenta = this.dao.findByTokenSesion(tokenSesion);

        if (cuenta == null)
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sesión no válida");

        LocalDateTime caducidadSesion = cuenta.getHoraTokenSesion().plusMinutes(this.minutosCaducidadSesion);

        if (LocalDateTime.now().isAfter(caducidadSesion)) {
            cuenta.setTokenSesion(null);
            this.dao.save(cuenta);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "La sesión ha caducado");
        }

        return new SesionDto(cuenta.getNombre(), cuenta.getApellidos(), cuenta.getCorreo(), cuenta.getMunicipio());
    }

    public void logout(String tokenSesion) {
        Cuenta cuenta = this.dao.findByTokenSesion(tokenSesion);
        if (cuenta != null) {
            cuenta.setTokenSesion(null);
            this.dao.save(cuenta);
        }
    }

    private void registrarIntentoFallido(String correo, String motivo) {
        IntentoLoginFallido intento = new IntentoLoginFallido();
        intento.setCorreo(correo);
        intento.setFecha(LocalDateTime.now());
        intento.setMotivo(motivo);
        this.intentoLoginFallidoDao.save(intento);
    }
}