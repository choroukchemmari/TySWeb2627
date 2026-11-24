package edu.uclm.esi.tysweb.bealquileres.dto;

import java.time.LocalDateTime;

public record PrerreservaDto(String reservaId, String municipio, int cantidad, int importe, LocalDateTime expira) {
}