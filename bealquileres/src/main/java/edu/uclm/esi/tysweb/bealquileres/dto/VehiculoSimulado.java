package edu.uclm.esi.tysweb.bealquileres.dto;

public record VehiculoSimulado(String matricula,
        Double latitudOrigen, Double longitudOrigen, String nombreOrigen,
        Double latitudDestino, Double longitudDestino, String nombreDestino,
        Double latitudActual, Double longitudActual, String direccion,
        Double kmh, Double distanciaRecorrida) {
}