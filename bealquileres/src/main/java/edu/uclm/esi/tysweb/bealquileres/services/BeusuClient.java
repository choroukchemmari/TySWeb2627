package edu.uclm.esi.tysweb.bealquileres.services;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import edu.uclm.esi.tysweb.bealquileres.dto.SesionDto;

@Service
public class BeusuClient {

    private final RestClient restClient;

    public BeusuClient(@Value("${beusu.url}") String url,
                       @Value("${beusu.timeout-ms}") int timeoutMs) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(timeoutMs));
        factory.setReadTimeout(Duration.ofMillis(timeoutMs));
        this.restClient = RestClient.builder().baseUrl(url).requestFactory(factory).build();
    }

    public SesionDto validar(String sessionToken) {
        try {
            return this.restClient.get()
                    .uri("/validateSession?sessionToken={t}", sessionToken)
                    .retrieve()
                    .body(SesionDto.class);
        } catch (HttpClientErrorException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sesión no válida o caducada");
        } catch (RestClientException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "No se puede validar la sesión ahora mismo");
        }
    }
}