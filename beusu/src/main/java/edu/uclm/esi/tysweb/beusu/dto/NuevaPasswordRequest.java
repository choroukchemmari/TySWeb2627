package edu.uclm.esi.tysweb.beusu.dto;

public record NuevaPasswordRequest(String token, String password, String confirmacionPassword) {
}