package br.unitins.akmos.service;

import java.time.LocalDate;
import java.util.List;

import br.unitins.akmos.dto.CliqueDTO;
import br.unitins.akmos.dto.CliqueResponseDTO;
import br.unitins.akmos.dto.ResumoDTO;

public interface CliqueService {

    CliqueResponseDTO registrar(CliqueDTO dto);

    List<CliqueResponseDTO> listar(LocalDate inicio, LocalDate fim, int pagina, int tamanho);

    ResumoDTO resumo(LocalDate inicio, LocalDate fim);
}
