package edu.uclm.esi.tysweb.beusu.http;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.uclm.esi.tysweb.beusu.dto.RegistroRequest;
import edu.uclm.esi.tysweb.beusu.services.CuentaService;

@RestController
@RequestMapping("/api/cuentas")
@CrossOrigin("*")
public class CuentaController {

    @Autowired
    private CuentaService service;

    @PostMapping("/registrar")
    public void registrar(@RequestBody RegistroRequest request) {
        this.service.registrar(request);
    }

    @GetMapping("/confirmar/{token}")
    public void confirmar(@PathVariable String token) {
        this.service.confirmar(token);
    }
}