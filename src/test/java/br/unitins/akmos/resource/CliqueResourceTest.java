package br.unitins.akmos.resource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
class CliqueResourceTest {

    private static final String USUARIO = "admin";
    private static final String SENHA = "senha-teste"; // definida em %test no application.properties

    @Test
    void deveRegistrarCliqueDeKit() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"tipo":"KIT","item":"Kit AK Body","secao":"kits","dispositivo":"celular"}
                        """)
                .when().post("/api/cliques")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("item", equalTo("Kit AK Body"))
                .body("dataHora", notNullValue());
    }

    @Test
    void deveRecusarCliqueSemItem() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"tipo":"KIT","item":"","secao":"kits"}
                        """)
                .when().post("/api/cliques")
                .then()
                .statusCode(400);
    }

    @Test
    void deveRecusarDispositivoInvalido() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"tipo":"PRODUTO","item":"Carbo Fire","dispositivo":"tablet"}
                        """)
                .when().post("/api/cliques")
                .then()
                .statusCode(400);
    }

    @Test
    void relatorioExigeLogin() {
        given()
                .when().get("/api/cliques/resumo")
                .then()
                .statusCode(401);
    }

    @Test
    void relatorioRecusaSenhaErrada() {
        given()
                .auth().preemptive().basic(USUARIO, "errada")
                .when().get("/api/cliques/resumo")
                .then()
                .statusCode(401);
    }

    @Test
    void relatorioContaOsCliques() {
        for (int i = 0; i < 2; i++) {
            given()
                    .contentType(ContentType.JSON)
                    .body("""
                            {"tipo":"PRODUTO","item":"Creatina Gummy","secao":"produtos","dispositivo":"computador"}
                            """)
                    .when().post("/api/cliques")
                    .then()
                    .statusCode(201);
        }

        given()
                .auth().preemptive().basic(USUARIO, SENHA)
                .when().get("/api/cliques/resumo")
                .then()
                .statusCode(200)
                .body("total", greaterThanOrEqualTo(2))
                .body("porItem.nome", hasItem("Creatina Gummy"))
                .body("porTipo.nome", hasItem("PRODUTO"))
                .body("porDispositivo.nome", hasItem("computador"));
    }

    @Test
    void relatorioRecusaPeriodoInvertido() {
        given()
                .auth().preemptive().basic(USUARIO, SENHA)
                .queryParam("inicio", "2026-10-31")
                .queryParam("fim", "2026-10-01")
                .when().get("/api/cliques/resumo")
                .then()
                .statusCode(400);
    }
}
