package br.unitins.akmos.dto;

import java.time.LocalDate;
import java.util.List;

/** Relatório de cliques de um período. */
public record ResumoDTO(
        LocalDate inicio,
        LocalDate fim,
        long total,
        List<ContagemDTO> porTipo,
        List<ContagemDTO> porItem, // só kits e produtos
        List<ContagemDTO> porSecao,
        List<ContagemDTO> porDispositivo,
        List<ContagemDTO> porDia) {
}
