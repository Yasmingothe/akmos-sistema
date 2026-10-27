package br.unitins.akmos.resource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
class ProdutoResourceTest {

    static final String USUARIO = "admin";
    static final String SENHA = "senha-teste";

    static long criarProduto(String nome, String preco, int estoque, boolean destaque, boolean ativo) {
        return given()
                .auth().preemptive().basic(USUARIO, SENHA)
                .contentType(ContentType.JSON)
                .body("""
                        {"nome":"%s","categoria":"Teste","descricao":"Produto de teste","preco":%s,
                         "estoque":%d,"destaque":%b,"ativo":%b}
                        """.formatted(nome, preco, estoque, destaque, ativo))
                .when().post("/api/produtos")
                .then()
                .statusCode(201)
                .extract().jsonPath().getLong("id");
    }

    @Test
    void cadastroExigeLogin() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"nome":"Sem login","preco":10,"estoque":1}
                        """)
                .when().post("/api/produtos")
                .then()
                .statusCode(401);
    }

    @Test
    void deveCadastrarEBuscarProduto() {
        long id = criarProduto("Produto Busca", "49.90", 7, false, true);

        given()
                .auth().preemptive().basic(USUARIO, SENHA)
                .when().get("/api/produtos/" + id)
                .then()
                .statusCode(200)
                .body("nome", equalTo("Produto Busca"))
                .body("estoque", equalTo(7))
                .body("disponivel", equalTo(true));
    }

    @Test
    void deveRecusarPrecoZerado() {
        given()
                .auth().preemptive().basic(USUARIO, SENHA)
                .contentType(ContentType.JSON)
                .body("""
                        {"nome":"Preço inválido","preco":0,"estoque":1}
                        """)
                .when().post("/api/produtos")
                .then()
                .statusCode(400);
    }

    @Test
    void listaPublicaMostraSoAtivosESemEstoque() {
        criarProduto("Produto Visível", "10.00", 5, true, true);
        criarProduto("Produto Escondido", "10.00", 5, true, false);

        given()
                .when().get("/api/produtos/publico?destaque=true")
                .then()
                .statusCode(200)
                .body("nome", hasItem("Produto Visível"))
                .body("nome", not(hasItem("Produto Escondido")))
                .body("[0].estoque", equalTo(null));
    }

    @Test
    void deveAjustarEstoqueSemDeixarNegativo() {
        long id = criarProduto("Produto Estoque", "20.00", 2, false, true);

        given()
                .auth().preemptive().basic(USUARIO, SENHA)
                .contentType(ContentType.JSON)
                .body("{\"ajuste\": 3}")
                .when().patch("/api/produtos/" + id + "/estoque")
                .then()
                .statusCode(200)
                .body("estoque", equalTo(5));

        given()
                .auth().preemptive().basic(USUARIO, SENHA)
                .contentType(ContentType.JSON)
                .body("{\"ajuste\": -10}")
                .when().patch("/api/produtos/" + id + "/estoque")
                .then()
                .statusCode(400)
                .body("mensagem", containsString("negativo"));
    }

    @Test
    void deveEnviarEMostrarFoto() {
        long id = criarProduto("Produto Foto", "30.00", 1, false, true);
        byte[] png = { (byte) 0x89, 'P', 'N', 'G', 13, 10, 26, 10, 0, 0, 0, 0 };

        String url = given()
                .auth().preemptive().basic(USUARIO, SENHA)
                .multiPart("imagem", "foto.png", png, "image/png")
                .when().put("/api/produtos/" + id + "/imagem")
                .then()
                .statusCode(200)
                .body("fotoEnviada", equalTo(true))
                .body("imagemUrl", notNullValue())
                .extract().jsonPath().getString("imagemUrl");

        given()
                .when().get(url)
                .then()
                .statusCode(200)
                .contentType("image/png");
    }

    @Test
    void deveRecusarArquivoQueNaoEFoto() {
        long id = criarProduto("Produto Arquivo", "30.00", 1, false, true);

        given()
                .auth().preemptive().basic(USUARIO, SENHA)
                .multiPart("imagem", "virus.exe", new byte[] { 1, 2, 3 }, "application/octet-stream")
                .when().put("/api/produtos/" + id + "/imagem")
                .then()
                .statusCode(400);
    }

    @Test
    void deveExcluirProdutoForaDeKit() {
        long id = criarProduto("Produto Excluir", "15.00", 1, false, true);

        given()
                .auth().preemptive().basic(USUARIO, SENHA)
                .when().delete("/api/produtos/" + id)
                .then()
                .statusCode(204);

        given()
                .auth().preemptive().basic(USUARIO, SENHA)
                .when().get("/api/produtos/" + id)
                .then()
                .statusCode(404);
    }
}
