package edu.uclm.esi.tysweb.beusu.dto;

public record LoginResponse(String token, String nombre, String apellidos, String correo) {
}