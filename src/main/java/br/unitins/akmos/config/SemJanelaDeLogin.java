package br.unitins.akmos.config;

import io.vertx.ext.web.Router;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

/**
 * Quando a senha está errada, o navegador costuma abrir uma janelinha própria de login
 * (por causa do cabeçalho WWW-Authenticate). Como o painel tem a sua própria tela de login,
 * este filtro remove esse cabeçalho e a janelinha não aparece.
 */
@ApplicationScoped
public class SemJanelaDeLogin {

    void registrar(@Observes Router router) {
        router.route().order(-100).handler(rc -> {
            rc.addHeadersEndHandler(v -> rc.response().headers().remove("WWW-Authenticate"));
            rc.next();
        });
    }
}
