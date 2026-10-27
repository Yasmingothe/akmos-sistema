package br.unitins.akmos.model;

/**
 * O que foi clicado na landing page.
 * KIT     = botão "Quero esse kit"
 * PRODUTO = botão "Pedir no WhatsApp" de um produto em destaque
 * GERAL   = qualquer outro botão de WhatsApp (topo, destaque, flutuante, chamada final...)
 */
public enum TipoClique {
    KIT,
    PRODUTO,
    GERAL
}
