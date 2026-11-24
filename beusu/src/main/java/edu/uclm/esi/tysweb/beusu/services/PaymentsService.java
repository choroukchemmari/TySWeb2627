package edu.uclm.esi.tysweb.beusu.services;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;

import edu.uclm.esi.tysweb.beusu.dao.CompraDao;
import edu.uclm.esi.tysweb.beusu.dto.SesionDto;
import edu.uclm.esi.tysweb.beusu.model.Compra;
import edu.uclm.esi.tysweb.beusu.services.AlquileresClient.Prerreserva;

@Service
public class PaymentsService {

    private static final Logger log = LoggerFactory.getLogger(PaymentsService.class);

    // La clave secreta NO va en el código ni en git: se lee de una variable de entorno
    @Value("${stripe.secret-key:}")
    private String secretKey;

    private final CompraDao compraDao;
    private final AlquileresClient alquileres;

    public PaymentsService(CompraDao compraDao, AlquileresClient alquileres) {
        this.compraDao = compraDao;
        this.alquileres = alquileres;
    }

    /**
     * Fase 1 del pago (prepay): pide a Stripe un PaymentIntent por el importe de la
     * prerreserva y devuelve su client_secret, que identifica la transacción.
     */
    public String prepay(SesionDto sesion, String reservaId) {
        // 400: falta la prerreserva
        if (reservaId == null || reservaId.isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Hay que indicar la prerreserva");

        // El importe lo decide bealquileres (404, 409 o 410 si la prerreserva no es válida)
        Prerreserva prerreserva = this.alquileres.consultar(reservaId);

        // 403: la prerreserva es de otro municipio
        if (!prerreserva.municipio().equalsIgnoreCase(sesion.municipio()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "La prerreserva no pertenece a su municipio");

        // Si ya se inició el pago de esta prerreserva, se reutiliza
        Compra existente = this.compraDao.findByReservaId(reservaId);
        if (existente != null) {
            if (existente.isConfirmada())
                throw new ResponseStatusException(HttpStatus.CONFLICT, "La compra ya estaba confirmada");
            return existente.getIdTransaccion();
        }

        comprobarClave();
        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setCurrency("eur")
                    .setAmount((long) prerreserva.importe() * 100) // Stripe trabaja en céntimos
                    .addPaymentMethodType("card")
                    .build();
            PaymentIntent intent = PaymentIntent.create(params);

            Compra compra = new Compra();
            compra.setIdTransaccion(intent.getClientSecret());
            compra.setReservaId(reservaId);
            compra.setCorreo(sesion.correo());
            compra.setCantidad(prerreserva.cantidad());
            compra.setImporte(prerreserva.importe());
            this.compraDao.save(compra);
            return compra.getIdTransaccion();
        } catch (StripeException e) {
            // El motivo real solo va al registro del servidor, no al cliente
            log.error("Stripe no pudo crear el pago: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "No se pudo iniciar el pago");
        }
    }

    /**
     * Fase final (confirm): el cliente dice que ha pagado, pero beusu NO se lo cree:
     * se lo pregunta a Stripe. Solo si el pago consta como completado se confirma la
     * compra en bealquileres.
     */
    public void confirm(SesionDto sesion, String idTransaccion) {
        // 400: falta la transacción
        if (idTransaccion == null || idTransaccion.isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Hay que indicar la transacción");

        // 404: no existe
        Compra compra = this.compraDao.findByIdTransaccion(idTransaccion);
        if (compra == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "La transacción no existe");

        // 403: es de otro usuario
        if (!compra.getCorreo().equalsIgnoreCase(sesion.correo()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "La transacción no es suya");

        // 409: ya estaba confirmada
        if (compra.isConfirmada())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La compra ya estaba confirmada");

        comprobarClave();
        try {
            // El id del PaymentIntent es la parte del client_secret anterior a "_secret_"
            String paymentIntentId = idTransaccion.substring(0, idTransaccion.indexOf("_secret_"));
            PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);

            // 402: el pago no se ha completado (o no es por el importe esperado)
            if (!"succeeded".equals(intent.getStatus()) || intent.getAmount() != (long) compra.getImporte() * 100)
                throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED, "El pago no se ha completado");
        } catch (StripeException e) {
            log.error("Stripe no pudo comprobar el pago: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "No se pudo comprobar el pago");
        }

        // El pago es real: bealquileres hace definitiva la asignación de las bicicletas
        this.alquileres.confirmar(compra.getReservaId());

        compra.setFechaConfirmacion(LocalDateTime.now());
        this.compraDao.save(compra);
    }

    private void comprobarClave() {
        if (this.secretKey == null || this.secretKey.isBlank())
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "El proveedor de pagos no está configurado");
        Stripe.apiKey = this.secretKey;
    }
}