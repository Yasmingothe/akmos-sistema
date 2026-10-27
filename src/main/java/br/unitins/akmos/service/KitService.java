package br.unitins.akmos.service;

import java.util.List;

import br.unitins.akmos.dto.ImagemDTO;
import br.unitins.akmos.dto.KitDTO;
import br.unitins.akmos.dto.KitPublicoDTO;
import br.unitins.akmos.dto.KitResponseDTO;

public interface KitService {

    List<KitPublicoDTO> listarPublicos();

    List<KitResponseDTO> listarTodos();

    KitResponseDTO buscar(Long id);

    KitResponseDTO criar(KitDTO dto);

    KitResponseDTO atualizar(Long id, KitDTO dto);

    void excluir(Long id);

    KitResponseDTO salvarImagem(Long id, byte[] dados, String tipo);

    KitResponseDTO removerImagem(Long id);

    ImagemDTO buscarImagem(Long id);
}
