/* =========================================================
   SITE AKMOS
   1. Carrega os kits e produtos cadastrados no painel do revendedor.
      Se o servidor não responder, o site mostra a versão que já está no HTML.
   2. Conta os cliques nos botões de WhatsApp (relatório do painel).
   ========================================================= */

// TROCAR pelo número do revendedor (55 + DDD + número). Troque também nos links do index.html.
const WHATSAPP = '5563999564173';

// Site e servidor no mesmo endereço = ''. Pelo Live Server (porta 5500), usa o servidor local.
const API = location.port === '5500' ? 'http://localhost:8080' : '';

/* ---------- utilidades ---------- */

const reais = (valor) => Number(valor).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });

// Todo texto vindo do banco passa por aqui antes de ir para o HTML
function esc(texto) {
  const div = document.createElement('div');
  div.textContent = texto == null ? '' : String(texto);
  return div.innerHTML;
}

const linkWhats = (mensagem) => 'https://wa.me/' + WHATSAPP + '?text=' + encodeURIComponent(mensagem);

function urlImagem(url) {
  if (!url) return null;
  return url.startsWith('/api/') ? API + url : url;
}

function htmlImagem(url, alt) {
  const src = urlImagem(url);
  return src
    ? `<img src="${esc(src)}" alt="${esc(alt)}" loading="lazy">`
    : '<div class="foto-vazia">Foto em breve</div>';
}

/* ---------- kits ---------- */

function htmlKit(kit) {
  const itens = kit.itens.map((i) => `<li>${esc(i)}</li>`).join('');
  const brinde = kit.brinde ? `<span class="kit-brinde">🎁 ${esc(kit.brinde)}</span>` : '';
  const selo = kit.disponivel ? '' : '<span class="selo-indisponivel">Sob consulta</span>';
  const mensagem = kit.disponivel
    ? `Olá! Vi no site o ${kit.nome} (${reais(kit.preco)}) e tenho interesse. Pode me passar as condições?`
    : `Olá! Vi no site o ${kit.nome}. Ele está disponível?`;
  return `
    <article class="kit${kit.disponivel ? '' : ' indisponivel'}">
      <div class="kit-imagem">
        ${htmlImagem(kit.imagemUrl, kit.nome + ' - produtos Akmos')}
        <span class="kit-selo">${esc(kit.objetivo)}</span>
        ${selo}
      </div>
      <div class="kit-corpo">
        <h3>${esc(kit.nome)}</h3>
        <p>${esc(kit.descricao)}</p>
        <span class="kit-inclui">O kit inclui:</span>
        <ul class="kit-lista">${itens}</ul>
        ${brinde}
        <div class="kit-preco">
          <span class="kit-rotulo">${kit.precoEspecial ? 'Preço do kit' : 'Valor do kit'}</span>
          <span class="preco-kit">${reais(kit.preco)}</span>
        </div>
        <a class="btn btn-bloco${kit.disponivel ? '' : ' btn-consultar'}" href="${esc(linkWhats(mensagem))}" target="_blank" rel="noopener">${kit.disponivel ? 'Quero esse kit' : 'Consultar disponibilidade'}</a>
      </div>
    </article>`;
}

/* ---------- produtos ---------- */

function htmlProduto(p) {
  const selo = p.disponivel ? '' : '<span class="selo-indisponivel">Esgotado</span>';
  const mensagem = p.disponivel
    ? `Olá! Tenho interesse no produto ${p.nome}.`
    : `Olá! O produto ${p.nome} vai chegar de novo? Quero ser avisado(a).`;
  return `
    <article class="card${p.disponivel ? '' : ' indisponivel'}">
      <div class="card-imagem">
        ${htmlImagem(p.imagemUrl, p.nome + ' Akmos')}
        ${selo}
      </div>
      <div class="card-corpo">
        <span class="card-categoria">${esc(p.categoria || 'Akmos')}</span>
        <h3>${esc(p.nome)}</h3>
        <p>${esc(p.descricao)}</p>
        <div class="card-rodape">
          <span class="preco">${reais(p.preco)}</span>
          <a class="btn btn-card${p.disponivel ? '' : ' btn-consultar'}" href="${esc(linkWhats(mensagem))}" target="_blank" rel="noopener">${p.disponivel ? 'Pedir no WhatsApp' : 'Avise-me'}</a>
        </div>
      </div>
    </article>`;
}

/* ---------- carregamento ---------- */

async function buscar(caminho) {
  const resposta = await fetch(API + caminho);
  if (!resposta.ok) throw new Error('Erro ' + resposta.status);
  return resposta.json();
}

async function carregarCatalogo() {
  try {
    const [kits, produtos] = await Promise.all([
      buscar('/api/kits/publico'),
      buscar('/api/produtos/publico?destaque=true'),
    ]);
    if (kits.length) {
      document.getElementById('trilho-kits').innerHTML = kits.map(htmlKit).join('');
    }
    if (produtos.length) {
      document.getElementById('grade-produtos').innerHTML = produtos.map(htmlProduto).join('');
    }
  } catch (erro) {
    // Servidor fora do ar: continua mostrando os kits e produtos que já estão no HTML
    console.warn('Não foi possível carregar o catálogo do servidor:', erro.message);
  }
}

/* ---------- contador de cliques no WhatsApp ---------- */

const NOMES_SECAO = { inicio: 'destaque', kits: 'kits', produtos: 'produtos', duvidas: 'duvidas', chamada: 'chamada-final' };

function descobrirSecao(link) {
  if (link.classList.contains('whats-flutuante')) return 'botao-flutuante';
  if (link.closest('header')) return 'topo';
  if (link.closest('footer')) return 'rodape';
  const secao = link.closest('section');
  if (!secao) return 'outro';
  const chave = secao.id || secao.classList[0];
  return NOMES_SECAO[chave] || chave;
}

function registrarClique(link) {
  const kit = link.closest('.kit');
  const produto = link.closest('.card');
  const cartao = kit || produto;
  const clique = {
    tipo: kit ? 'KIT' : produto ? 'PRODUTO' : 'GERAL',
    item: (cartao
      ? cartao.querySelector('h3').textContent
      : (link.textContent.trim() || link.getAttribute('aria-label') || 'WhatsApp')
    ).trim().slice(0, 120),
    secao: descobrirSecao(link),
    dispositivo: window.matchMedia('(max-width: 800px)').matches ? 'celular' : 'computador',
  };
  fetch(API + '/api/cliques', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(clique),
    keepalive: true,
  }).catch(() => { /* servidor fora do ar: o WhatsApp abre do mesmo jeito */ });
}

// Funciona também para os kits e produtos que chegam do servidor depois
document.addEventListener('click', (evento) => {
  const link = evento.target.closest('a[href*="wa.me"]');
  if (link) registrarClique(link);
});

carregarCatalogo();

/* ---------- entrada do revendedor (login no próprio site) ---------- */

const CHAVE_LOGIN = 'akmos-cred'; // a mesma usada pelo painel: entrou aqui, já está logado lá

function credencialSalva() {
  try { return sessionStorage.getItem(CHAVE_LOGIN); } catch (e) { return null; }
}

function atualizarBotaoEntrar() {
  const logado = !!credencialSalva();
  document.getElementById('btn-entrar-texto').textContent = logado ? 'Meu painel' : 'Entrar';
}

const janelaLogin = document.getElementById('janela-login');

document.querySelectorAll('[data-abrir-login]').forEach((link) => {
  link.addEventListener('click', (evento) => {
    if (credencialSalva()) return; // já entrou: o link leva direto ao painel
    evento.preventDefault();
    document.getElementById('site-login-erro').textContent = '';
    janelaLogin.showModal();
    document.getElementById('site-usuario').focus();
  });
});

document.getElementById('fechar-login').addEventListener('click', () => janelaLogin.close());
janelaLogin.addEventListener('click', (evento) => {
  if (evento.target === janelaLogin) janelaLogin.close(); // clicou fora da caixa
});

document.getElementById('form-login-site').addEventListener('submit', async (evento) => {
  evento.preventDefault();
  const erro = document.getElementById('site-login-erro');
  const botao = document.getElementById('site-entrar');
  const usuario = document.getElementById('site-usuario').value.trim();
  const senha = document.getElementById('site-senha').value;
  const credencial = btoa(unescape(encodeURIComponent(usuario + ':' + senha)));
  erro.textContent = '';
  botao.disabled = true;
  try {
    const resposta = await fetch(API + '/api/auth/eu', { headers: { Authorization: 'Basic ' + credencial } });
    if (resposta.status === 401) throw new Error('Usuário ou senha incorretos.');
    if (!resposta.ok) throw new Error('Não foi possível entrar agora (erro ' + resposta.status + ').');
    try { sessionStorage.setItem(CHAVE_LOGIN, credencial); } catch (e) { /* sem armazenamento */ }
    window.location.href = '/admin/'; // mesma aba
  } catch (e) {
    erro.textContent = e.message.startsWith('Failed') ? 'Servidor fora do ar. Tente de novo em instantes.' : e.message;
    botao.disabled = false;
  }
});

atualizarBotaoEntrar();
