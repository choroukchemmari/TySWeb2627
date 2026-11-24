package edu.uclm.esi.tysweb.bealquileres.dto;

import java.util.List;

public record UbicarBicicletasRequestDto(String estacionId, List<Long> vehiculoIds) {
}