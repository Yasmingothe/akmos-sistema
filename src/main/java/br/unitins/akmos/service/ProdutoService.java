package br.unitins.akmos.service;

import java.util.List;

import br.unitins.akmos.dto.EstoqueDTO;
import br.unitins.akmos.dto.ImagemDTO;
import br.unitins.akmos.dto.ProdutoDTO;
import br.unitins.akmos.dto.ProdutoPublicoDTO;
import br.unitins.akmos.dto.ProdutoResponseDTO;

public interface ProdutoService {

    List<ProdutoPublicoDTO> listarPublicos(boolean somenteDestaques);

    List<ProdutoResponseDTO> listarTodos();

    ProdutoResponseDTO buscar(Long id);

    ProdutoResponseDTO criar(ProdutoDTO dto);

    ProdutoResponseDTO atualizar(Long id, ProdutoDTO dto);

    ProdutoResponseDTO ajustarEstoque(Long id, EstoqueDTO dto);

    void excluir(Long id);

    ProdutoResponseDTO salvarImagem(Long id, byte[] dados, String tipo);

    ProdutoResponseDTO removerImagem(Long id);

    ImagemDTO buscarImagem(Long id);
}
