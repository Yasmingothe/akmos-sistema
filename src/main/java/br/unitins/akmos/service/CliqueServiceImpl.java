package br.unitins.akmos.service;

import java.time.LocalDate;
import java.util.List;

import br.unitins.akmos.dto.CliqueDTO;
import br.unitins.akmos.dto.CliqueResponseDTO;
import br.unitins.akmos.dto.ResumoDTO;
import br.unitins.akmos.model.Clique;
import br.unitins.akmos.repository.CliqueRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;

@ApplicationScoped
public class CliqueServiceImpl implements CliqueService {

    /** Quando o relatório é pedido sem datas, mostra os últimos 30 dias. */
    private static final int DIAS_PADRAO = 30;

    @Inject
    CliqueRepository repository;

    @Override
    @Transactional
    public CliqueResponseDTO registrar(CliqueDTO dto) {
        Clique clique = new Clique();
        clique.setTipo(dto.tipo());
        clique.setItem(dto.item().trim());
        clique.setSecao(dto.secao());
        clique.setDispositivo(dto.dispositivo());

        repository.persist(clique);
        return CliqueResponseDTO.valueOf(clique);
    }

    @Override
    public List<CliqueResponseDTO> listar(LocalDate inicio, LocalDate fim, int pagina, int tamanho) {
        LocalDate[] periodo = periodo(inicio, fim);
        return repository.findByPeriodo(periodo[0], periodo[1])
                .page(pagina, tamanho)
                .list()
                .stream()
                .map(CliqueResponseDTO::valueOf)
                .toList();
    }

    @Override
    public ResumoDTO resumo(LocalDate inicio, LocalDate fim) {
        LocalDate[] periodo = periodo(inicio, fim);
        LocalDate de = periodo[0];
        LocalDate ate = periodo[1];

        return new ResumoDTO(
                de,
                ate,
                repository.contarNoPeriodo(de, ate),
                repository.contarPorTipo(de, ate),
                repository.contarPorItem(de, ate),
                repository.contarPorSecao(de, ate),
                repository.contarPorDispositivo(de, ate),
                repository.contarPorDia(de, ate));
    }

    /** Preenche as datas que vierem vazias e confere se o início vem antes do fim. */
    private LocalDate[] periodo(LocalDate inicio, LocalDate fim) {
        LocalDate ate = (fim != null) ? fim : LocalDate.now();
        LocalDate de = (inicio != null) ? inicio : ate.minusDays(DIAS_PADRAO - 1);

        if (de.isAfter(ate)) {
            throw new BadRequestException("A data de início deve ser anterior ou igual à data de fim.");
        }
        return new LocalDate[] { de, ate };
    }
}
