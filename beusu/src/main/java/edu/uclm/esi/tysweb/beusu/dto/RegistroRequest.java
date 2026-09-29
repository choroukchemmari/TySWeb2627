package edu.uclm.esi.tysweb.beusu.dto;

public record RegistroRequest(
        String nombre,
        String apellidos,
        String correo,
        String municipio,
        Double latitud,
        Double longitud,
        String password,
        String confirmacionPassword) {
}