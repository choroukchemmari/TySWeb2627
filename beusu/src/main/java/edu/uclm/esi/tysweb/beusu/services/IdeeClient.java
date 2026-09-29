package edu.uclm.esi.tysweb.beusu.services;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import tools.jackson.databind.JsonNode;

@Component
public class IdeeClient {

    private final RestClient restClient;

    public IdeeClient() {
        this.restClient = RestClient.builder()
                .baseUrl("https://api-features.idee.es")
                .build();
    }

    public boolean existeMunicipio(String municipio) {
        JsonNode response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/collections/address/items")
                    .queryParam("component_AddressAreaName", municipio)
                    .queryParam("f", "json")
                    .queryParam("limit", 1)
                    .build())
                .retrieve()
                .body(JsonNode.class);

        if (response == null)
            return false;

        return response.path("numberMatched").asInt() > 0;
    }
}