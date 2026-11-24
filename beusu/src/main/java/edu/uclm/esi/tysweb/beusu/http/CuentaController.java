package edu.uclm.esi.tysweb.beusu.http;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.view.RedirectView;

import edu.uclm.esi.tysweb.beusu.dto.LoginRequest;
import edu.uclm.esi.tysweb.beusu.dto.LoginResponse;
import edu.uclm.esi.tysweb.beusu.dto.NuevaPasswordRequest;
import edu.uclm.esi.tysweb.beusu.dto.RegistroRequest;
import edu.uclm.esi.tysweb.beusu.dto.SesionDto;
import edu.uclm.esi.tysweb.beusu.dto.SolicitarPasswordRequest;
import edu.uclm.esi.tysweb.beusu.services.CuentaService;

@RestController
@RequestMapping("/api/cuentas")
@CrossOrigin("*")
public class CuentaController {

    @Autowired
    private CuentaService service;

    @Value("${feayto.url}")
    private String feaytoUrl;

    @PostMapping("/registrar")
    public void registrar(@RequestBody RegistroRequest request) {
        this.service.registrar(request);
    }

    @GetMapping("/confirmar")
    public RedirectView confirmar(@RequestParam String token) {
        this.service.confirmar(token);
        return new RedirectView(this.feaytoUrl + "/login");
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return this.service.login(request);
    }

    @GetMapping("/validateSession")
    public SesionDto validateSession(@RequestParam String sessionToken) {
        return this.service.validateSession(sessionToken);
    }

    @PostMapping("/logout")
    public void logout(@RequestParam String sessionToken) {
        this.service.logout(sessionToken);
    }

    // Responde igual exista o no el correo (RC-SEC-03)
    @PostMapping("/solicitarNuevaPassword")
    public void solicitarNuevaPassword(@RequestBody SolicitarPasswordRequest request) {
        this.service.solicitarNuevaPassword(request.correo());
    }

    // Enlace del correo: valida el token y redirige a la página de feayto
    @GetMapping("/nuevaPassword")
    public RedirectView nuevaPassword(@RequestParam(required = false) String token) {
        try {
            this.service.validarTokenRecuperacion(token);
            return new RedirectView(this.feaytoUrl + "/nueva-password?token=" + token);
        } catch (ResponseStatusException e) {
            return new RedirectView(this.feaytoUrl + "/nueva-password?error=1");
        }
    }

    @PostMapping("/nuevaPassword")
    public void crearNuevaPassword(@RequestBody NuevaPasswordRequest request) {
        this.service.crearNuevaPassword(request);
    }
}