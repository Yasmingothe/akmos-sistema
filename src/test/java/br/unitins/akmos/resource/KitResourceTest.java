package br.unitins.akmos.resource;

import static br.unitins.akmos.resource.ProdutoResourceTest.SENHA;
import static br.unitins.akmos.resource.ProdutoResourceTest.USUARIO;
import static br.unitins.akmos.resource.ProdutoResourceTest.criarProduto;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
class KitResourceTest {

    private long criarKit(String nome, String preco, long... idsProdutos) {
        StringBuilder ids = new StringBuilder();
        for (long id : idsProdutos) {
            ids.append(ids.isEmpty() ? "" : ",").append(id);
        }
        return given()
                .auth().preemptive().basic(USUARIO, SENHA)
                .contentType(ContentType.JSON)
                .body("""
                        {"nome":"%s","objetivo":"Teste","descricao":"Kit de teste","preco":%s,
                         "ordem":1,"ativo":true,"idsProdutos":[%s]}
                        """.formatted(nome, preco, ids))
                .when().post("/api/kits")
                .then()
                .statusCode(201)
                .extract().jsonPath().getLong("id");
    }

    @Test
    void precoDoKitEASomaDosProdutos() {
        long a = criarProduto("Kit Soma A", "10.50", 5, false, true);
        long b = criarProduto("Kit Soma B", "20.00", 5, false, true);
        long kit = criarKit("Kit Soma", "null", a, b);

        given()
                .auth().preemptive().basic(USUARIO, SENHA)
                .when().get("/api/kits/" + kit)
                .then()
                .statusCode(200)
                .body("preco", equalTo(30.50f))
                .body("precoEspecial", equalTo(null))
                .body("produtos", hasSize(2));
    }

    @Test
    void kitComPrecoEspecial() {
        long a = criarProduto("Kit Especial A", "100.00", 5, false, true);
        long kit = criarKit("Kit Especial", "89.90", a);

        given()
                .auth().preemptive().basic(USUARIO, SENHA)
                .when().get("/api/kits/" + kit)
                .then()
                .statusCode(200)
                .body("preco", equalTo(89.90f))
                .body("somaProdutos", equalTo(100.00f));
    }

    @Test
    void kitSemProdutoNaoDisponivelESemEstoque() {
        long a = criarProduto("Kit Esgotado A", "10.00", 0, false, true);
        criarKit("Kit Esgotado", "null", a);

        given()
                .when().get("/api/kits/publico")
                .then()
                .statusCode(200)
                .body("find { it.nome == 'Kit Esgotado' }.disponivel", equalTo(false))
                .body("find { it.nome == 'Kit Esgotado' }.itens", hasItem("Kit Esgotado A"));
    }

    @Test
    void kitPrecisaDeProduto() {
        given()
                .auth().preemptive().basic(USUARIO, SENHA)
                .contentType(ContentType.JSON)
                .body("""
                        {"nome":"Kit Vazio","objetivo":"Teste","idsProdutos":[]}
                        """)
                .when().post("/api/kits")
                .then()
                .statusCode(400);
    }

    @Test
    void naoExcluiProdutoQueEstaEmKit() {
        long a = criarProduto("Produto Preso", "10.00", 5, false, true);
        criarKit("Kit Que Prende", "null", a);

        given()
                .auth().preemptive().basic(USUARIO, SENHA)
                .when().delete("/api/produtos/" + a)
                .then()
                .statusCode(409)
                .body("mensagem", containsString("Kit Que Prende"));
    }

    @Test
    void loginDoPainel() {
        given()
                .auth().preemptive().basic(USUARIO, SENHA)
                .when().get("/api/auth/eu")
                .then()
                .statusCode(200)
                .body("usuario", equalTo("admin"));

        given()
                .auth().preemptive().basic(USUARIO, "errada")
                .when().get("/api/auth/eu")
                .then()
                .statusCode(401);
    }
}
