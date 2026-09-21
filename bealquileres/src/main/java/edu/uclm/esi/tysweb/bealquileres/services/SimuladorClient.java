package edu.uclm.esi.tysweb.bealquileres.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import edu.uclm.esi.tysweb.bealquileres.dto.VehiculoSimulado;

@Component
public class SimuladorClient {

    private final RestClient restClient;

    public SimuladorClient(@Value("${simulador.url:http://localhost:8082}") String simuladorUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(simuladorUrl)
                .build();
    }

    public List<VehiculoSimulado> getVehiculos(String city) {
        List<VehiculoSimulado> vehiculos = restClient.get()
                .uri("/simulador/getVehiculos/{city}", city)
                .retrieve()
                .body(new ParameterizedTypeReference<List<VehiculoSimulado>>() {});

        return vehiculos == null ? List.of() : vehiculos;
    }
}