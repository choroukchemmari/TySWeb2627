package edu.uclm.esi.tysweb.beusu.http;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.uclm.esi.tysweb.beusu.dto.ConfirmRequest;
import edu.uclm.esi.tysweb.beusu.dto.PrepayResponse;
import edu.uclm.esi.tysweb.beusu.dto.SesionDto;
import edu.uclm.esi.tysweb.beusu.services.CuentaService;
import edu.uclm.esi.tysweb.beusu.services.PaymentsService;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin("*")
public class PaymentsController {

    private final PaymentsService payments;
    private final CuentaService cuentas;

    public PaymentsController(PaymentsService payments, CuentaService cuentas) {
        this.payments = payments;
        this.cuentas = cuentas;
    }

    // Mensaje 1.1 del flujo: devuelve el client_secret (idTransaccion) de Stripe
    @PostMapping("/prepay")
    public PrepayResponse prepay(@RequestParam(required = false) String reservaId,
            @RequestHeader(value = "X-Session-Token", required = false) String token) {
        SesionDto sesion = this.cuentas.validateSession(token);
        return new PrepayResponse(this.payments.prepay(sesion, reservaId));
    }

    // Mensaje 3.3 del flujo: el cliente dice que ha pagado; beusu lo comprueba con Stripe
    @PostMapping("/confirm")
    public void confirm(@RequestBody ConfirmRequest body,
            @RequestHeader(value = "X-Session-Token", required = false) String token) {
        SesionDto sesion = this.cuentas.validateSession(token);
        this.payments.confirm(sesion, body.idTransaccion());
    }
}