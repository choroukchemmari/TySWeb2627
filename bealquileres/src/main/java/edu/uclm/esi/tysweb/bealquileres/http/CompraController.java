package edu.uclm.esi.tysweb.bealquileres.http;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import edu.uclm.esi.tysweb.bealquileres.dto.CompraRequestDto;
import edu.uclm.esi.tysweb.bealquileres.dto.ConfirmarCompraRequestDto;
import edu.uclm.esi.tysweb.bealquileres.dto.PrerreservaDto;
import edu.uclm.esi.tysweb.bealquileres.dto.SesionDto;
import edu.uclm.esi.tysweb.bealquileres.services.CompraService;

@RestController
@RequestMapping("/api/compras")
@CrossOrigin("*")
public class CompraController {

    private final CompraService service;

    // Clave compartida entre beusu y bealquileres para las llamadas entre servidores
    @Value("${servicios.api-key}")
    private String apiKey;

    public CompraController(CompraService service) {
        this.service = service;
    }

    // Lo llama el funcionario desde feayto (protegido por el token de sesión)
    @PostMapping("/prerreservar")
    public ResponseEntity<PrerreservaDto> prerreservar(@RequestBody CompraRequestDto body,
            @RequestAttribute("sesion") SesionDto sesion) {
        PrerreservaDto dto = this.service.prerreservar(sesion.municipio(), body.cantidad());
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    // Lo llama beusu (clave de servidor) para saber cuánto cobrar
    @GetMapping("/{reservaId}")
    public PrerreservaDto consultar(@PathVariable String reservaId,
            @RequestHeader(value = "X-Api-Key", required = false) String clave) {
        comprobarClave(clave);
        return this.service.consultar(reservaId);
    }

    // Lo llama beusu (clave de servidor) cuando Stripe ha confirmado el pago
    @PostMapping("/confirmar")
    public void confirmar(@RequestBody ConfirmarCompraRequestDto body,
            @RequestHeader(value = "X-Api-Key", required = false) String clave) {
        comprobarClave(clave);
        this.service.confirmar(body.reservaId());
    }

    private void comprobarClave(String clave) {
        boolean valida = clave != null && MessageDigest.isEqual(
                clave.getBytes(StandardCharsets.UTF_8), this.apiKey.getBytes(StandardCharsets.UTF_8));
        if (!valida)
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Clave de servidor no válida");
    }
}
