package br.unitins.akmos.config;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import br.unitins.akmos.model.Kit;
import br.unitins.akmos.model.Produto;
import br.unitins.akmos.repository.KitRepository;
import br.unitins.akmos.repository.ProdutoRepository;
import io.quarkus.logging.Log;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

/**
 * Na primeira vez que o sistema liga (banco vazio), cadastra os produtos e kits
 * da Revista Akmos 03/2026, para o site já abrir igual à versão aprovada.
 * Depois disso o revendedor altera tudo pelo painel.
 */
@ApplicationScoped
public class DadosIniciais {

    @Inject
    ProdutoRepository produtoRepository;

    @Inject
    KitRepository kitRepository;

    @ConfigProperty(name = "akmos.dados-iniciais", defaultValue = "true")
    boolean ativo;

    private final Map<String, Produto> produtos = new HashMap<>();

    @Transactional
    void carregar(@Observes StartupEvent evento) {
        if (!ativo || produtoRepository.count() > 0) {
            return;
        }
        cadastrarProdutos();
        cadastrarKits();
        Log.infof("Dados iniciais cadastrados: %d produtos e %d kits.", produtoRepository.count(), kitRepository.count());
    }

    private void cadastrarProdutos() {
        // Controle de peso
        produto("Alerti+ Energia", "Energia e foco", "Cafeína, tirosina e colina para mais energia, foco e concentração. 60 cápsulas.", "99.90", 10, true, "/img/alerti-mais.png");
        produto("Ativa+", "Controle de peso", "L-carnitina, coenzima Q10 e inulina para apoiar o metabolismo e a disposição. 30 cápsulas.", "97.50", 10, true, "/img/ativa-mais.png");
        produto("Carbo Fire", "Controle de peso", "Psyllium, quitosana, cromo e L-triptofano. Auxilia na saciedade e no controle de peso. 60 cápsulas.", "107.50", 10, true, "/img/carbo-fire.png");
        produto("Slim Fit Creme", "Corpo", "Creme com ativos vegetais que auxilia na redução de medidas e no aspecto da celulite. 150 g.", "117.50", 8, true, "/img/slim-fit.png");
        produto("Nutri Shake+", "Controle de peso", "Shake com whey e colágeno para substituir refeições. Sabores chocolate e baunilha. 600 g.", "139.70", 2, false, null);
        produto("Body Tea", "Controle de peso", "Chá de amora, hibisco, carqueja e chá branco que auxilia na digestão e no controle de peso. 120 g.", "57.90", 10, false, "/img/body-tea.png");
        // Treino
        produto("Creatina Gummy", "Treino", "Creatina em gomas sabor maçã verde. Auxilia no desempenho físico em exercícios de alta intensidade. 50 gummys.", "147.70", 3, true, "/img/creatina-gummy.jpg");
        produto("Magnum Power", "Treino", "Quatro tipos de magnésio para o funcionamento muscular e a formação de ossos. 60 cápsulas.", "124.50", 10, false, "/img/magnum-power.png");
        produto("ReHidrata", "Treino", "Isotônico em pó com água de coco e eletrólitos, zero açúcar. Sabores limão e tangerina. 90 g.", "129.70", 10, false, null);
        // Beleza
        produto("Young 4K", "Beleza", "Colágeno Verisol®, vitamina C e zinco para a saúde da pele. 160 g.", "159.70", 10, false, null);
        produto("LePhuv Beauty", "Beleza", "Ácido hialurônico em gotas sublinguais de absorção rápida. 30 mL.", "112.00", 10, true, "/img/lephuv-beauty.png");
        produto("AK Hair+ Gummy", "Beleza", "Biotina, vitaminas e ácido hialurônico em gomas sabor tangerina para cabelos, pele e unhas. 30 gomas.", "137.50", 10, false, "/img/hair-gummy.png");
        // Energia
        produto("Super B12", "Energia e foco", "Vitamina B12 com vitamina C e ferro. 30 cápsulas.", "64.70", 10, false, "/img/super-b12.png");
        produto("Right Now", "Energia e foco", "Zinco e complexo de vitaminas que ajuda a reduzir o cansaço mental. 30 cápsulas.", "57.90", 10, false, null);
        // Bem-estar
        produto("Pro Woman", "Bem-estar", "Multivitamínico feminino com ômega 3, luteína e 28 vitaminas e minerais. 60 cápsulas.", "177.50", 10, false, "/img/pro-woman.png");
        produto("Full Ômega", "Bem-estar", "Ômega 3, vitamina E e óleos de linhaça, prímula e borragem. 60 cápsulas.", "137.90", 10, false, null);
        // Imunidade
        produto("Cúrcuma Complex", "Imunidade", "Cúrcuma com vitamina C para o sistema imune e o metabolismo energético. 30 cápsulas.", "99.50", 10, false, "/img/curcuma.png");
        produto("Chlorella", "Imunidade", "Chlorella orgânica com ômega 3, rica em nutrientes. 90 cápsulas.", "124.50", 10, false, "/img/chlorella.png");
        produto("LePhuv BGL Shield", "Imunidade", "Beta glucana de levedura em gotas sublinguais. 30 mL.", "112.00", 10, false, "/img/lephuv-bgl.png");
        // Articulações
        produto("Day Flex+", "Articulações", "Colágeno tipo II com magnésio e vitaminas D e K2 para articulações e ossos. 30 cápsulas.", "137.90", 10, false, "/img/day-flex.png");
        produto("LePhuv Artic", "Articulações", "Cúrcuma, glucosamina, condroitina e MSM em gotas sublinguais. 30 mL.", "112.00", 10, false, "/img/lephuv-artic.png");
        produto("Doctor Fit Nanotech", "Articulações", "Gel para massagem com arnica e alecrim, de ação refrescante. 90 g.", "69.70", 10, false, null);
        // Sono
        produto("Melatonina Relaxed", "Sono", "Melatonina em gotas sabor maracujá que auxilia na qualidade do sono. 20 mL.", "79.70", 10, false, "/img/melatonina.jpg");
        produto("Óleo Essencial Sweet Relax", "Sono", "Óleo essencial de lavanda para relaxamento e bem-estar. 10 mL.", "87.50", 10, false, null);
    }

    private void cadastrarKits() {
        kit(1, "Kit AK Body", "Emagrecimento",
                "Programa completo de 30 dias: orientação alimentar, plano de treinos e suplementos que auxiliam no controle de peso.",
                "620.40", "Brindes: cartilha, shakeira e tableteira AK Body", "/img/kit-ak-body.jpg",
                "Alerti+ Energia", "Ativa+", "Carbo Fire", "Slim Fit Creme", "Nutri Shake+", "Body Tea");
        kit(2, "Kit Pele, Cabelo e Unhas", "Beleza",
                "Beleza de dentro pra fora: colágeno Verisol®, ácido hialurônico e biotina para apoiar a pele, os fios e as unhas.",
                null, null, "/img/kit-pele.jpg",
                "Young 4K", "LePhuv Beauty", "AK Hair+ Gummy");
        kit(3, "Kit Treino e Performance", "Treino",
                "Para quem treina e quer mais força e resistência: creatina em gomas, magnésio e hidratação com eletrólitos.",
                null, null, "/img/kit-treino.jpg",
                "Creatina Gummy", "Magnum Power", "ReHidrata");
        kit(4, "Kit Energia e Foco", "Energia",
                "Para a rotina corrida: cafeína, tirosina, vitaminas do complexo B e zinco para mais disposição e concentração.",
                null, null, "/img/kit-energia.jpg",
                "Alerti+ Energia", "Super B12", "Right Now");
        kit(5, "Kit Equilíbrio Feminino", "Mulher",
                "Multivitamínico feminino, ômegas com óleo de prímula e magnésio para apoiar o bem-estar do corpo e da mente.",
                null, null, "/img/kit-equilibrio.jpg",
                "Pro Woman", "Full Ômega", "Magnum Power");
        kit(6, "Kit Imunidade", "Imunidade",
                "Cúrcuma com vitamina C, chlorella orgânica e beta glucana para apoiar as defesas do organismo.",
                null, null, "/img/kit-imunidade.jpg",
                "Cúrcuma Complex", "Chlorella", "LePhuv BGL Shield");
        kit(7, "Kit Articulações", "Articulações",
                "Colágeno tipo II, cúrcuma, glucosamina e gel de massagem para quem quer se movimentar com mais conforto.",
                null, null, "/img/kit-articulacoes.jpg",
                "Day Flex+", "LePhuv Artic", "Doctor Fit Nanotech");
        kit(8, "Kit Sono e Relaxamento", "Sono",
                "Melatonina em gotas, magnésio e óleo essencial de lavanda para desacelerar e dormir melhor.",
                null, null, "/img/kit-sono.jpg",
                "Melatonina Relaxed", "Magnum Power", "Óleo Essencial Sweet Relax");
    }

    private void produto(String nome, String categoria, String descricao, String preco, int estoque,
            boolean destaque, String imagem) {
        Produto p = new Produto();
        p.setNome(nome);
        p.setCategoria(categoria);
        p.setDescricao(descricao);
        p.setPreco(new BigDecimal(preco));
        p.setEstoque(estoque);
        p.setDestaque(destaque);
        p.setAtivo(true);
        p.setImagemArquivo(imagem);
        produtoRepository.persist(p);
        produtos.put(nome, p);
    }

    private void kit(int ordem, String nome, String objetivo, String descricao, String preco, String brinde,
            String imagem, String... nomesProdutos) {
        Kit k = new Kit();
        k.setOrdem(ordem);
        k.setNome(nome);
        k.setObjetivo(objetivo);
        k.setDescricao(descricao);
        k.setPreco(preco == null ? null : new BigDecimal(preco));
        k.setBrinde(brinde);
        k.setAtivo(true);
        k.setImagemArquivo(imagem);
        for (String nomeProduto : nomesProdutos) {
            Produto p = produtos.get(nomeProduto);
            if (p == null) {
                throw new IllegalStateException("Produto do kit não cadastrado: " + nomeProduto);
            }
            k.getProdutos().add(p);
        }
        kitRepository.persist(k);
    }
}
