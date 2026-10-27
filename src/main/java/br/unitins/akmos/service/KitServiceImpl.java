package br.unitins.akmos.service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import br.unitins.akmos.dto.ImagemDTO;
import br.unitins.akmos.dto.KitDTO;
import br.unitins.akmos.dto.KitPublicoDTO;
import br.unitins.akmos.dto.KitResponseDTO;
import br.unitins.akmos.exception.NegocioException;
import br.unitins.akmos.model.Kit;
import br.unitins.akmos.model.Produto;
import br.unitins.akmos.repository.KitRepository;
import br.unitins.akmos.repository.ProdutoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class KitServiceImpl implements KitService {

    @Inject
    KitRepository repository;

    @Inject
    ProdutoRepository produtoRepository;

    @Override
    @Transactional
    public List<KitPublicoDTO> listarPublicos() {
        return repository.listarAtivos().stream()
                .map(KitPublicoDTO::valueOf)
                .toList();
    }

    @Override
    @Transactional
    public List<KitResponseDTO> listarTodos() {
        return repository.listarTodos().stream()
                .map(KitResponseDTO::valueOf)
                .toList();
    }

    @Override
    @Transactional
    public KitResponseDTO buscar(Long id) {
        return KitResponseDTO.valueOf(buscarEntidade(id));
    }

    @Override
    @Transactional
    public KitResponseDTO criar(KitDTO dto) {
        Kit kit = new Kit();
        preencher(kit, dto);
        repository.persist(kit);
        return KitResponseDTO.valueOf(kit);
    }

    @Override
    @Transactional
    public KitResponseDTO atualizar(Long id, KitDTO dto) {
        Kit kit = buscarEntidade(id);
        preencher(kit, dto);
        return KitResponseDTO.valueOf(kit);
    }

    @Override
    @Transactional
    public void excluir(Long id) {
        repository.delete(buscarEntidade(id));
    }

    @Override
    @Transactional
    public KitResponseDTO salvarImagem(Long id, byte[] dados, String tipo) {
        ImagemValidador.validar(dados, tipo);
        Kit kit = buscarEntidade(id);
        kit.setImagem(dados);
        kit.setImagemTipo(tipo);
        kit.setImagemVersao(System.currentTimeMillis());
        return KitResponseDTO.valueOf(kit);
    }

    @Override
    @Transactional
    public KitResponseDTO removerImagem(Long id) {
        Kit kit = buscarEntidade(id);
        kit.setImagem(null);
        kit.setImagemTipo(null);
        kit.setImagemVersao(null);
        return KitResponseDTO.valueOf(kit);
    }

    @Override
    @Transactional
    public ImagemDTO buscarImagem(Long id) {
        Kit kit = buscarEntidade(id);
        if (kit.getImagemTipo() == null) {
            throw NegocioException.naoEncontrado("Este kit não tem foto enviada.");
        }
        return new ImagemDTO(kit.getImagem(), kit.getImagemTipo());
    }

    private Kit buscarEntidade(Long id) {
        Kit kit = repository.findById(id);
        if (kit == null) {
            throw NegocioException.naoEncontrado("Kit não encontrado.");
        }
        return kit;
    }

    private void preencher(Kit kit, KitDTO dto) {
        kit.setNome(dto.nome().trim());
        kit.setObjetivo(dto.objetivo().trim());
        kit.setDescricao(ProdutoServiceImpl.vazioParaNulo(dto.descricao()));
        kit.setPreco(dto.preco());
        kit.setBrinde(ProdutoServiceImpl.vazioParaNulo(dto.brinde()));
        kit.setOrdem(dto.ordem() == null ? 0 : dto.ordem());
        kit.setAtivo(dto.ativo() == null || dto.ativo());

        // Mantém a ordem escolhida e ignora produtos repetidos
        List<Produto> produtos = new ArrayList<>();
        for (Long idProduto : new LinkedHashSet<>(dto.idsProdutos())) {
            if (idProduto == null) {
                continue;
            }
            Produto produto = produtoRepository.findById(idProduto);
            if (produto == null) {
                throw NegocioException.invalido("Produto " + idProduto + " não encontrado.");
            }
            produtos.add(produto);
        }
        kit.getProdutos().clear();
        kit.getProdutos().addAll(produtos);
    }
}
