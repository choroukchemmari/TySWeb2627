package edu.uclm.esi.tysweb.beusu.services;

import java.time.Duration;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

/**
 * Llamadas de beusu a bealquileres para la compra de bicicletas.
 * Se identifica con una clave de servidor compartida (X-Api-Key).
 */
@Service
public class AlquileresClient {

    public record Prerreserva(String reservaId, String municipio, int cantidad, int importe, String expira) {
    }

    private final RestClient client;

    public AlquileresClient(@Value("${bealquileres.url}") String url,
            @Value("${servicios.api-key}") String apiKey,
            @Value("${bealquileres.timeout-ms:3000}") int timeoutMs) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(timeoutMs));
        factory.setReadTimeout(Duration.ofMillis(timeoutMs));
        this.client = RestClient.builder()
                .baseUrl(url)
                .requestFactory(factory)
                .defaultHeader("X-Api-Key", apiKey)
                .build();
    }

    /** Datos de la prerreserva (cantidad e importe): beusu no se fía de lo que diga el cliente. */
    public Prerreserva consultar(String reservaId) {
        try {
            return this.client.get()
                    .uri("/{id}", reservaId)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        throw traducir(res.getStatusCode());
                    })
                    .body(Prerreserva.class);
        } catch (ResourceAccessException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "bealquileres no responde");
        }
    }

    /** El pago está confirmado: bealquileres hace definitiva la asignación de las bicicletas. */
    public void confirmar(String reservaId) {
        try {
            this.client.post()
                    .uri("/confirmar")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("reservaId", reservaId))
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        throw traducir(res.getStatusCode());
                    })
                    .toBodilessEntity();
        } catch (ResourceAccessException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "bealquileres no responde");
        }
    }

    private ResponseStatusException traducir(HttpStatusCode status) {
        return switch (status.value()) {
            case 404 -> new ResponseStatusException(HttpStatus.NOT_FOUND, "La prerreserva no existe");
            case 409 -> new ResponseStatusException(HttpStatus.CONFLICT, "La compra ya estaba confirmada");
            case 410 -> new ResponseStatusException(HttpStatus.GONE, "La prerreserva ha caducado");
            default -> new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Error al comunicar con bealquileres");
        };
    }
}