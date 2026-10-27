/* =========================================================
   PAINEL DO REVENDEDOR AKMOS
   Login, produtos (cadastro, foto, estoque) e kits.
   Conversa com a API do backend Quarkus em /api/...
   ========================================================= */

const ESTOQUE_BAIXO = 3;
const CHAVE_LOGIN = 'akmos-cred'; // a mesma do relatório de cliques: entra uma vez, vale para os dois

let credencial = null;
let produtos = [];
let kits = [];
let produtoEditando = null; // null = novo produto
let kitEditando = null;
let fotoProdutoNova = null; // arquivo escolhido e ainda não enviado
let fotoKitNova = null;

/* ---------------- utilidades ---------------- */

const $ = (id) => document.getElementById(id);
const reais = (v) => Number(v).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });

function esc(texto) {
  const div = document.createElement('div');
  div.textContent = texto == null ? '' : String(texto);
  return div.innerHTML;
}

/** Aceita "99,90", "1.234,56" ou "99.90". Vazio = null. */
function lerPreco(texto) {
  const t = String(texto || '').trim().replace(/\s|R\$/g, '');
  if (!t) return null;
  const normal = t.includes(',') ? t.replace(/\./g, '').replace(',', '.') : t;
  const numero = Number(normal);
  return Number.isFinite(numero) ? Math.round(numero * 100) / 100 : NaN;
}

const precoParaCampo = (v) => (v == null ? '' : Number(v).toFixed(2).replace('.', ','));

function ler(chave) { try { return sessionStorage.getItem(chave); } catch (e) { return null; } }
function gravar(chave, valor) { try { sessionStorage.setItem(chave, valor); } catch (e) { /* sem armazenamento */ } }
function apagar(chave) { try { sessionStorage.removeItem(chave); } catch (e) { /* sem armazenamento */ } }

let temporizadorAviso;
function aviso(mensagem, erro = false) {
  const el = $('aviso');
  el.textContent = mensagem;
  el.classList.toggle('erro-aviso', erro);
  el.classList.add('mostrar');
  clearTimeout(temporizadorAviso);
  temporizadorAviso = setTimeout(() => el.classList.remove('mostrar'), erro ? 6000 : 3000);
}

function miniatura(url, alt) {
  return url
    ? `<img src="${esc(url)}" alt="${esc(alt)}" loading="lazy">`
    : '<span class="foto-vazia">sem foto</span>';
}

/* ---------------- conversa com a API ---------------- */

class ErroApi extends Error {
  constructor(mensagem, status) { super(mensagem); this.status = status; }
}

async function api(metodo, caminho, corpo, ehFormulario = false) {
  const opcoes = { method: metodo, headers: { Authorization: 'Basic ' + credencial } };
  if (corpo !== undefined) {
    if (ehFormulario) {
      opcoes.body = corpo; // FormData: o navegador define o Content-Type sozinho
    } else {
      opcoes.headers['Content-Type'] = 'application/json';
      opcoes.body = JSON.stringify(corpo);
    }
  }
  const resposta = await fetch(caminho, opcoes);
  if (resposta.status === 401 || resposta.status === 403) {
    sair('Sua sessão terminou. Entre de novo.');
    throw new ErroApi('Sessão encerrada.', resposta.status);
  }
  if (resposta.status === 204) return null;
  const dados = await resposta.json().catch(() => null);
  if (!resposta.ok) {
    let mensagem = 'Algo deu errado (erro ' + resposta.status + ').';
    if (dados && dados.mensagem) mensagem = dados.mensagem;
    else if (dados && dados.violations) mensagem = dados.violations.map((v) => v.message).join(' ');
    throw new ErroApi(mensagem, resposta.status);
  }
  return dados;
}

/* ---------------- login ---------------- */

async function entrar(usuario, senha) {
  credencial = btoa(unescape(encodeURIComponent(usuario + ':' + senha)));
  const resposta = await fetch('/api/auth/eu', { headers: { Authorization: 'Basic ' + credencial } });
  if (!resposta.ok) {
    credencial = null;
    throw new Error(resposta.status === 401 ? 'Usuário ou senha incorretos.' : 'Não foi possível entrar (erro ' + resposta.status + ').');
  }
  gravar(CHAVE_LOGIN, credencial);
  mostrarPainel();
}

function sair(mensagem) {
  credencial = null;
  apagar(CHAVE_LOGIN);
  $('tela-painel').hidden = true;
  $('btn-sair').hidden = true;
  $('tela-login').hidden = false;
  $('login-erro').textContent = mensagem || '';
  document.querySelectorAll('dialog[open]').forEach((d) => d.close());
}

async function mostrarPainel() {
  $('tela-login').hidden = true;
  $('tela-painel').hidden = false;
  $('btn-sair').hidden = false;
  abrirAba(location.hash.replace('#', '') || 'inicio');
  await recarregar();
}

/* ---------------- abas ---------------- */

function abrirAba(nome) {
  if (!['inicio', 'produtos', 'kits', 'cliques'].includes(nome)) nome = 'inicio';
  document.querySelectorAll('.aba[data-aba]').forEach((b) => b.classList.toggle('ativa', b.dataset.aba === nome));
  document.querySelectorAll('.aba-conteudo').forEach((s) => { s.hidden = s.id !== 'aba-' + nome; });
  history.replaceState(null, '', '#' + nome);
  window.scrollTo(0, 0);
  if (nome === 'cliques' && credencial) carregarRelatorio();
}

/* ---------------- carregar e desenhar ---------------- */

async function recarregar() {
  try {
    [produtos, kits] = await Promise.all([api('GET', '/api/produtos'), api('GET', '/api/kits')]);
    desenharTudo();
  } catch (e) {
    if (!(e instanceof ErroApi && e.status === 401)) aviso(e.message, true);
  }
  carregarCliques();
}

let resumoCliques = null;    // últimos 30 dias
let cliquesAnteriores = null; // os 30 dias antes desses (para comparar)

/** Data local no formato AAAA-MM-DD, "dias" atrás. */
function dataAtras(dias) {
  const d = new Date();
  d.setDate(d.getDate() - dias);
  return d.toLocaleDateString('sv-SE');
}

async function carregarCliques() {
  try {
    const [atual, anterior] = await Promise.all([
      api('GET', `/api/cliques/resumo?inicio=${dataAtras(29)}&fim=${dataAtras(0)}`),
      api('GET', `/api/cliques/resumo?inicio=${dataAtras(59)}&fim=${dataAtras(30)}`),
    ]);
    resumoCliques = atual;
    cliquesAnteriores = anterior.total;
  } catch (e) {
    resumoCliques = null;
  }
  desenharInicio();
}

function desenharTudo() {
  desenharInicio();
  desenharProdutos();
  desenharKits();
  $('categorias').innerHTML = [...new Set(produtos.map((p) => p.categoria).filter(Boolean))]
    .sort().map((c) => `<option value="${esc(c)}">`).join('');
}

function linhaProduto(p, seloExtra = '') {
  const baixo = p.estoque <= ESTOQUE_BAIXO;
  const selos = [
    p.destaque ? '<span class="selo destaque">Destaque</span>' : '',
    !p.ativo ? '<span class="selo inativo">Fora do site</span>' : '',
    p.ativo && p.estoque === 0 ? '<span class="selo esgotado">Esgotado</span>' : '',
    seloExtra,
  ].join('');
  return `
    <div class="linha${p.ativo ? '' : ' inativo'}">
      <div class="miniatura">${miniatura(p.imagemUrl, p.nome)}</div>
      <div class="info">
        <div class="nome">${esc(p.nome)}</div>
        <div class="categoria">${esc(p.categoria || 'Sem categoria')}</div>
        <div class="selos">${selos}</div>
      </div>
      <div class="preco">${reais(p.preco)}</div>
      <div class="estoque" aria-label="Estoque de ${esc(p.nome)}">
        <button class="icone" data-estoque="${p.id}" data-ajuste="-1" title="Saiu 1 unidade" ${p.estoque === 0 ? 'disabled' : ''}>−</button>
        <span class="qtd${baixo ? ' baixo' : ''}">${p.estoque}</span>
        <button class="icone" data-estoque="${p.id}" data-ajuste="1" title="Chegou 1 unidade">+</button>
      </div>
      <div class="acoes">
        <button class="icone" data-editar-produto="${p.id}" title="Editar">✎</button>
        <button class="icone perigo" data-excluir-produto="${p.id}" title="Excluir">🗑</button>
      </div>
    </div>`;
}

/** Cliques no produto + cliques nos kits que têm esse produto (30 dias). */
function interesse(produto) {
  if (!resumoCliques) return 0;
  const cliques = Object.fromEntries(resumoCliques.porItem.map((i) => [i.nome, i.total]));
  let total = cliques[produto.nome] || 0;
  kits.forEach((k) => {
    if (k.produtos.some((p) => p.id === produto.id)) total += cliques[k.nome] || 0;
  });
  return total;
}

function barrasHorizontais(lista, formatar) {
  const max = Math.max(1, ...lista.map((i) => i.valor));
  return '<div class="hbarras">' + lista.map((i) => `
    <div class="hbarra" tabindex="0" data-dica="${esc(i.nome)}: ${esc(formatar(i.valor))}">
      <span class="hb-nome">${esc(i.nome)}</span>
      <span class="hb-trilho"><span class="hb-preench" style="width:${Math.max(1.5, (i.valor / max) * 100)}%"></span></span>
      <span class="hb-valor">${esc(formatar(i.valor))}</span>
    </div>`).join('') + '</div>';
}

/** Gráfico de barras com um dia por barra, do início ao fim do resumo. */
function desenharDias(el, resumo) {
  if (!resumo) { el.innerHTML = '<div class="vazio">Não foi possível carregar os cliques agora.</div>'; return; }
  if (!resumo.total) {
    el.innerHTML = '<div class="vazio">Nenhum clique neste período. Eles aparecem aqui quando os clientes tocarem nos botões de WhatsApp do site.</div>';
    return;
  }
  const mapa = Object.fromEntries(resumo.porDia.map((d) => [d.nome, d.total]));
  const dias = [];
  for (let d = new Date(resumo.inicio + 'T12:00'); d.toLocaleDateString('sv-SE') <= resumo.fim; d.setDate(d.getDate() + 1)) {
    const data = d.toLocaleDateString('sv-SE');
    dias.push({ data, total: mapa[data] || 0 });
  }
  const max = Math.max(1, ...dias.map((d) => d.total));
  const curta = (iso) => iso.split('-').reverse().slice(0, 2).join('/');
  const meio = dias[Math.floor(dias.length / 2)];
  const ultimo = dias[dias.length - 1];
  el.innerHTML = `
    <div class="g-dias">
      <div class="g-eixo"><span>${max}</span><span>0</span></div>
      <div class="g-area">
        <div class="g-barras">
          ${dias.map((d) => `<div class="g-col" tabindex="0" data-dica="${curta(d.data)}: ${textoCliques(d.total)}"><div class="g-barra${d.total ? '' : ' zero'}" style="height:${(d.total / max) * 100}%"></div></div>`).join('')}
        </div>
      </div>
    </div>
    <div class="g-legenda-x"><span>${curta(dias[0].data)}</span>${dias.length > 2 ? `<span>${curta(meio.data)}</span>` : ''}<span>${ultimo.data === dataAtras(0) ? 'hoje' : curta(ultimo.data)}</span></div>`;
}

function desenharInicio() {
  const ativos = produtos.filter((p) => p.ativo);
  const valor = produtos.reduce((t, p) => t + Number(p.preco) * p.estoque, 0);
  const unidades = produtos.reduce((t, p) => t + p.estoque, 0);
  $('n-valor').textContent = reais(valor);
  $('n-valor-sub').textContent = `${unidades} unidades no total`;
  $('n-produtos').textContent = ativos.length;
  $('n-produtos-sub').textContent = `de ${produtos.length} cadastrados · ${kits.filter((k) => k.ativo).length} kits no site`;

  const esgotados = ativos.filter((p) => p.estoque === 0).length;
  const baixos = ativos.filter((p) => p.estoque <= ESTOQUE_BAIXO);
  $('n-baixo').textContent = baixos.length;
  $('n-baixo-sub').textContent = `${esgotados} esgotado(s) · ${baixos.length - esgotados} acabando`;

  if (resumoCliques) {
    const atual = resumoCliques.total;
    $('n-cliques').textContent = atual;
    let comparacao = 'Ainda sem cliques registrados';
    if (cliquesAnteriores > 0) {
      const variacao = Math.round(((atual - cliquesAnteriores) / cliquesAnteriores) * 100);
      comparacao = variacao === 0 ? 'Igual aos 30 dias anteriores'
        : `${variacao > 0 ? '▲' : '▼'} ${Math.abs(variacao)}% em relação aos 30 dias anteriores`;
    } else if (atual > 0) {
      comparacao = 'Primeiros cliques registrados';
    }
    $('n-cliques-sub').textContent = comparacao;
  } else {
    $('n-cliques').textContent = '-';
    $('n-cliques-sub').textContent = '';
  }

  desenharDias($('g-dias'), resumoCliques);

  const procurados = resumoCliques ? resumoCliques.porItem.slice(0, 6).map((i) => ({ nome: i.nome, valor: i.total })) : [];
  $('g-procurados').innerHTML = procurados.length
    ? barrasHorizontais(procurados, (v) => `${v} clique${v === 1 ? '' : 's'}`)
    : '<div class="vazio">Sem cliques em kits ou produtos nos últimos 30 dias.</div>';

  const porCategoria = {};
  produtos.forEach((p) => {
    const c = p.categoria || 'Sem categoria';
    porCategoria[c] = (porCategoria[c] || 0) + Number(p.preco) * p.estoque;
  });
  const categorias = Object.entries(porCategoria).map(([nome, v]) => ({ nome, valor: v })).sort((a, b) => b.valor - a.valor);
  $('g-categorias').innerHTML = categorias.length
    ? barrasHorizontais(categorias, (v) => reais(v))
    : '<div class="vazio">Nenhum produto cadastrado.</div>';

  const repor = baixos
    .map((p) => ({ p, procura: interesse(p) }))
    .sort((a, b) => b.procura - a.procura || a.p.estoque - b.p.estoque);
  $('lista-baixo').innerHTML = repor.length
    ? repor.map(({ p, procura }) => linhaProduto(p, procura
      ? `<span class="selo ${procura >= 5 ? 'procurado' : 'interesse'}">${procura >= 5 ? '🔥 Muito procurado · ' : ''}${procura} clique${procura === 1 ? '' : 's'} em 30 dias</span>`
      : '')).join('')
    : '<div class="vazio">Tudo certo! Nenhum produto com estoque baixo. ✅</div>';
}

function desenharProdutos() {
  const busca = $('busca-produto').value.trim().toLowerCase();
  const filtro = $('filtro-produto').value;
  const lista = produtos.filter((p) => {
    const texto = (p.nome + ' ' + (p.categoria || '')).toLowerCase();
    if (busca && !texto.includes(busca)) return false;
    if (filtro === 'ativos') return p.ativo;
    if (filtro === 'inativos') return !p.ativo;
    if (filtro === 'destaque') return p.destaque;
    if (filtro === 'baixo') return p.estoque <= ESTOQUE_BAIXO;
    return true;
  });
  $('lista-produtos').innerHTML = lista.length
    ? lista.map(linhaProduto).join('')
    : '<div class="vazio">Nenhum produto encontrado.</div>';
}

function desenharKits() {
  $('lista-kits').innerHTML = kits.length ? kits.map((k) => {
    const selos = [
      !k.ativo ? '<span class="selo inativo">Fora do site</span>' : '',
      k.ativo && !k.disponivel ? '<span class="selo esgotado">Falta estoque</span>' : '',
      k.ativo && k.disponivel ? '<span class="selo ok">No site</span>' : '',
    ].join('');
    return `
      <article class="cartao-kit${k.ativo ? '' : ' inativo'}">
        <div class="capa">${k.imagemUrl ? `<img src="${esc(k.imagemUrl)}" alt="${esc(k.nome)}" loading="lazy">` : 'sem foto'}</div>
        <div class="corpo">
          <span class="objetivo">Posição ${k.ordem} · ${esc(k.objetivo)}</span>
          <h3>${esc(k.nome)}</h3>
          <div class="itens">${k.produtos.map((p) => esc(p.nome)).join(' · ')}</div>
          <div class="selos">${selos}</div>
          <div class="rodape">
            <div class="valor"><small>${k.precoEspecial != null ? 'Preço especial' : 'Soma dos produtos'}</small>${reais(k.preco)}</div>
            <div class="acoes">
              <button class="icone" data-editar-kit="${k.id}" title="Editar">✎</button>
              <button class="icone perigo" data-excluir-kit="${k.id}" title="Excluir">🗑</button>
            </div>
          </div>
        </div>
      </article>`;
  }).join('') : '<div class="vazio">Nenhum kit cadastrado ainda.</div>';
}

/* ---------------- estoque rápido ---------------- */

async function ajustarEstoque(id, ajuste) {
  try {
    const atualizado = await api('PATCH', `/api/produtos/${id}/estoque`, { ajuste });
    produtos = produtos.map((p) => (p.id === atualizado.id ? atualizado : p));
    const kitsAtualizados = await api('GET', '/api/kits'); // a disponibilidade dos kits depende do estoque
    kits = kitsAtualizados;
    desenharTudo();
    aviso(`${atualizado.nome}: ${atualizado.estoque} em estoque`);
  } catch (e) {
    aviso(e.message, true);
  }
}

/* ---------------- fotos ---------------- */

/** Diminui a foto antes de enviar (máx. 1200 px, JPG), para o site carregar rápido. */
function prepararFoto(arquivo) {
  return new Promise((resolve, reject) => {
    if (!/^image\/(jpeg|png|webp)$/.test(arquivo.type)) {
      reject(new Error('Escolha uma foto JPG, PNG ou WEBP.'));
      return;
    }
    const img = new Image();
    const url = URL.createObjectURL(arquivo);
    img.onload = () => {
      URL.revokeObjectURL(url);
      const escala = Math.min(1, 1200 / Math.max(img.width, img.height));
      const canvas = document.createElement('canvas');
      canvas.width = Math.round(img.width * escala);
      canvas.height = Math.round(img.height * escala);
      const ctx = canvas.getContext('2d');
      ctx.fillStyle = '#ffffff'; // fundo branco para PNG transparente
      ctx.fillRect(0, 0, canvas.width, canvas.height);
      ctx.drawImage(img, 0, 0, canvas.width, canvas.height);
      canvas.toBlob((blob) => (blob ? resolve(blob) : reject(new Error('Não foi possível ler a foto.'))), 'image/jpeg', 0.85);
    };
    img.onerror = () => { URL.revokeObjectURL(url); reject(new Error('Não foi possível abrir essa foto.')); };
    img.src = url;
  });
}

async function enviarFoto(tipo, id, arquivo) {
  const blob = await prepararFoto(arquivo);
  const formulario = new FormData();
  formulario.append('imagem', blob, 'foto.jpg');
  return api('PUT', `/api/${tipo}/${id}/imagem`, formulario, true);
}

function mostrarPrevia(elemento, url) {
  elemento.innerHTML = url ? `<img src="${esc(url)}" alt="Prévia da foto">` : '<span class="foto-vazia">sem foto</span>';
}

/* ---------------- formulário de produto ---------------- */

function abrirProduto(produto) {
  produtoEditando = produto || null;
  fotoProdutoNova = null;
  $('form-produto').reset();
  $('titulo-produto').textContent = produto ? 'Editar produto' : 'Novo produto';
  $('p-nome').value = produto ? produto.nome : '';
  $('p-categoria').value = produto ? produto.categoria || '' : '';
  $('p-preco').value = produto ? precoParaCampo(produto.preco) : '';
  $('p-descricao').value = produto ? produto.descricao || '' : '';
  $('p-estoque').value = produto ? produto.estoque : 0;
  $('p-ativo').checked = produto ? produto.ativo : true;
  $('p-destaque').checked = produto ? produto.destaque : false;
  $('erro-produto').textContent = '';
  mostrarPrevia($('previa-produto'), produto ? produto.imagemUrl : null);
  $('remover-foto-produto').hidden = !(produto && produto.fotoEnviada);
  $('dialog-produto').showModal();
  $('p-nome').focus();
}

async function salvarProduto(evento) {
  evento.preventDefault();
  const preco = lerPreco($('p-preco').value);
  const estoque = Number($('p-estoque').value);
  if (!$('p-nome').value.trim()) return ($('erro-produto').textContent = 'Informe o nome do produto.');
  if (preco == null || Number.isNaN(preco) || preco <= 0) return ($('erro-produto').textContent = 'Informe um preço válido, por exemplo 99,90.');
  if (!Number.isInteger(estoque) || estoque < 0) return ($('erro-produto').textContent = 'O estoque deve ser um número inteiro, zero ou maior.');

  const dados = {
    nome: $('p-nome').value.trim(),
    categoria: $('p-categoria').value.trim(),
    descricao: $('p-descricao').value.trim(),
    preco,
    estoque,
    ativo: $('p-ativo').checked,
    destaque: $('p-destaque').checked,
  };
  const botao = $('salvar-produto');
  botao.disabled = true;
  try {
    let salvo = produtoEditando
      ? await api('PUT', `/api/produtos/${produtoEditando.id}`, dados)
      : await api('POST', '/api/produtos', dados);
    if (fotoProdutoNova) salvo = await enviarFoto('produtos', salvo.id, fotoProdutoNova);
    $('dialog-produto').close();
    aviso(produtoEditando ? 'Produto atualizado.' : 'Produto cadastrado.');
    await recarregar();
  } catch (e) {
    $('erro-produto').textContent = e.message;
  } finally {
    botao.disabled = false;
  }
}

async function excluirProduto(produto) {
  if (!confirm(`Excluir "${produto.nome}"? Essa ação não pode ser desfeita.\n\nDica: para só tirar do site, edite e desmarque "Mostrar no site".`)) return;
  try {
    await api('DELETE', `/api/produtos/${produto.id}`);
    aviso('Produto excluído.');
    await recarregar();
  } catch (e) {
    aviso(e.message, true);
  }
}

/* ---------------- formulário de kit ---------------- */

function desenharEscolhaProdutos(selecionados) {
  const busca = $('k-busca').value.trim().toLowerCase();
  $('k-produtos').innerHTML = produtos
    .filter((p) => !busca || p.nome.toLowerCase().includes(busca) || selecionados.has(p.id))
    .map((p) => `
      <label class="check">
        <input type="checkbox" value="${p.id}" ${selecionados.has(p.id) ? 'checked' : ''}>
        <span>${esc(p.nome)} ${p.ativo ? '' : '<small>(fora do site)</small>'}</span>
        <small>${reais(p.preco)} · estoque ${p.estoque}</small>
      </label>`).join('');
  atualizarResumoKit();
}

let selecaoKit = new Set();
function atualizarResumoKit() {
  const escolhidos = produtos.filter((p) => selecaoKit.has(p.id));
  const soma = escolhidos.reduce((t, p) => t + Number(p.preco), 0);
  $('k-resumo').textContent = escolhidos.length
    ? `${escolhidos.length} produto(s) escolhido(s) · soma: ${reais(soma)}`
    : 'Nenhum produto escolhido.';
  $('k-preco').placeholder = escolhidos.length ? `Vazio = ${reais(soma)} (soma)` : 'Vazio = soma dos produtos';
}

function abrirKit(kit) {
  kitEditando = kit || null;
  fotoKitNova = null;
  $('form-kit').reset();
  $('titulo-kit').textContent = kit ? 'Editar kit' : 'Novo kit';
  $('k-nome').value = kit ? kit.nome : '';
  $('k-objetivo').value = kit ? kit.objetivo : '';
  $('k-descricao').value = kit ? kit.descricao || '' : '';
  $('k-preco').value = kit ? precoParaCampo(kit.precoEspecial) : '';
  $('k-ordem').value = kit ? kit.ordem : (kits.reduce((m, k) => Math.max(m, k.ordem), 0) + 1);
  $('k-brinde').value = kit ? kit.brinde || '' : '';
  $('k-ativo').checked = kit ? kit.ativo : true;
  $('erro-kit').textContent = '';
  selecaoKit = new Set(kit ? kit.produtos.map((p) => p.id) : []);
  desenharEscolhaProdutos(selecaoKit);
  mostrarPrevia($('previa-kit'), kit ? kit.imagemUrl : null);
  $('remover-foto-kit').hidden = !(kit && kit.fotoEnviada);
  $('dialog-kit').showModal();
  $('k-nome').focus();
}

async function salvarKit(evento) {
  evento.preventDefault();
  const preco = lerPreco($('k-preco').value);
  if (!$('k-nome').value.trim()) return ($('erro-kit').textContent = 'Informe o nome do kit.');
  if (!$('k-objetivo').value.trim()) return ($('erro-kit').textContent = 'Informe o objetivo do kit (ex.: Emagrecimento).');
  if (!selecaoKit.size) return ($('erro-kit').textContent = 'Escolha pelo menos um produto para o kit.');
  if (Number.isNaN(preco) || (preco != null && preco <= 0)) return ($('erro-kit').textContent = 'Preço especial inválido. Deixe vazio para usar a soma.');

  // mantém a ordem da lista de produtos
  const ids = produtos.filter((p) => selecaoKit.has(p.id)).map((p) => p.id);
  const dados = {
    nome: $('k-nome').value.trim(),
    objetivo: $('k-objetivo').value.trim(),
    descricao: $('k-descricao').value.trim(),
    preco,
    brinde: $('k-brinde').value.trim(),
    ordem: Number($('k-ordem').value) || 0,
    ativo: $('k-ativo').checked,
    idsProdutos: ids,
  };
  const botao = $('salvar-kit');
  botao.disabled = true;
  try {
    let salvo = kitEditando
      ? await api('PUT', `/api/kits/${kitEditando.id}`, dados)
      : await api('POST', '/api/kits', dados);
    if (fotoKitNova) salvo = await enviarFoto('kits', salvo.id, fotoKitNova);
    $('dialog-kit').close();
    aviso(kitEditando ? 'Kit atualizado.' : 'Kit cadastrado.');
    await recarregar();
  } catch (e) {
    $('erro-kit').textContent = e.message;
  } finally {
    botao.disabled = false;
  }
}

async function excluirKit(kit) {
  if (!confirm(`Excluir o "${kit.nome}"? Os produtos continuam cadastrados.`)) return;
  try {
    await api('DELETE', `/api/kits/${kit.id}`);
    aviso('Kit excluído.');
    await recarregar();
  } catch (e) {
    aviso(e.message, true);
  }
}

/* ---------------- eventos ---------------- */

$('form-login').addEventListener('submit', async (e) => {
  e.preventDefault();
  $('login-erro').textContent = '';
  try {
    await entrar($('login-usuario').value.trim(), $('login-senha').value);
    $('login-senha').value = '';
  } catch (erro) {
    $('login-erro').textContent = erro.message;
  }
});

$('btn-sair').addEventListener('click', () => {
  sair();
  window.location.href = '/'; // depois de sair, volta para o site
});

document.querySelectorAll('.aba[data-aba]').forEach((b) => b.addEventListener('click', () => abrirAba(b.dataset.aba)));
$('busca-produto').addEventListener('input', desenharProdutos);
$('filtro-produto').addEventListener('change', desenharProdutos);
$('k-busca').addEventListener('input', () => desenharEscolhaProdutos(selecaoKit));
$('k-produtos').addEventListener('change', (e) => {
  const id = Number(e.target.value);
  if (e.target.checked) selecaoKit.add(id); else selecaoKit.delete(id);
  atualizarResumoKit();
});

// Cliques nos botões das listas (funciona para itens desenhados depois)
document.addEventListener('click', (e) => {
  const alvo = e.target.closest('button, [data-acao]');
  if (!alvo) return;
  const d = alvo.dataset;
  if (d.acao === 'novo-produto') abrirProduto(null);
  else if (d.acao === 'novo-kit') abrirKit(null);
  else if (d.estoque) ajustarEstoque(Number(d.estoque), Number(d.ajuste));
  else if (d.editarProduto) abrirProduto(produtos.find((p) => p.id === Number(d.editarProduto)));
  else if (d.excluirProduto) excluirProduto(produtos.find((p) => p.id === Number(d.excluirProduto)));
  else if (d.editarKit) abrirKit(kits.find((k) => k.id === Number(d.editarKit)));
  else if (d.excluirKit) excluirKit(kits.find((k) => k.id === Number(d.excluirKit)));
  else if (alvo.hasAttribute('data-fechar')) alvo.closest('dialog').close();
});

$('form-produto').addEventListener('submit', salvarProduto);
$('form-cliques').addEventListener('submit', (e) => { e.preventDefault(); carregarRelatorio(); });
$('form-kit').addEventListener('submit', salvarKit);

$('foto-produto').addEventListener('change', (e) => {
  fotoProdutoNova = e.target.files[0] || null;
  if (fotoProdutoNova) mostrarPrevia($('previa-produto'), URL.createObjectURL(fotoProdutoNova));
});
$('foto-kit').addEventListener('change', (e) => {
  fotoKitNova = e.target.files[0] || null;
  if (fotoKitNova) mostrarPrevia($('previa-kit'), URL.createObjectURL(fotoKitNova));
});

$('remover-foto-produto').addEventListener('click', async () => {
  if (!produtoEditando || !confirm('Remover a foto enviada deste produto?')) return;
  try {
    const atualizado = await api('DELETE', `/api/produtos/${produtoEditando.id}/imagem`);
    produtoEditando = atualizado;
    mostrarPrevia($('previa-produto'), atualizado.imagemUrl);
    $('remover-foto-produto').hidden = true;
    aviso('Foto removida.');
    recarregar();
  } catch (e) { aviso(e.message, true); }
});
$('remover-foto-kit').addEventListener('click', async () => {
  if (!kitEditando || !confirm('Remover a foto enviada deste kit?')) return;
  try {
    const atualizado = await api('DELETE', `/api/kits/${kitEditando.id}/imagem`);
    kitEditando = atualizado;
    mostrarPrevia($('previa-kit'), atualizado.imagemUrl);
    $('remover-foto-kit').hidden = true;
    aviso('Foto removida.');
    recarregar();
  } catch (e) { aviso(e.message, true); }
});

/* ---------------- aba Cliques (relatório) ---------------- */

const NOMES_SECAO = {
  kits: 'Carrossel de kits', produtos: 'Produtos em destaque', 'botao-flutuante': 'Botão verde flutuante',
  topo: 'Topo do site', destaque: 'Destaque (início do site)', 'chamada-final': 'Chamada final',
  rodape: 'Rodapé', duvidas: 'Dúvidas',
};
const NOMES_TIPO = { KIT: 'Kits', PRODUTO: 'Produtos', GERAL: 'Outros botões' };
const NOMES_APARELHO = { celular: 'Celular', computador: 'Computador' };
const brData = (iso) => iso.split('-').reverse().join('/');
const contagem = (lista, nomes) => lista.map((i) => ({ nome: (nomes && nomes[i.nome]) || i.nome, valor: i.total }));
const textoCliques = (v) => `${v} clique${v === 1 ? '' : 's'}`;

function mostrarBarras(id, lista, vazio) {
  $(id).innerHTML = lista.length ? barrasHorizontais(lista, textoCliques) : `<div class="vazio">${vazio}</div>`;
}

async function carregarRelatorio() {
  if (!$('c-inicio').value) {
    $('c-inicio').value = dataAtras(29);
    $('c-fim').value = dataAtras(0);
  }
  if ($('c-inicio').value > $('c-fim').value) {
    aviso('A data "De" precisa ser antes da data "Até".', true);
    return;
  }
  try {
    const r = await api('GET', `/api/cliques/resumo?inicio=${$('c-inicio').value}&fim=${$('c-fim').value}`);
    $('c-total').textContent = r.total;
    $('c-periodo').textContent = `de ${brData(r.inicio)} a ${brData(r.fim)}`;
    const nomesKits = new Set(kits.map((k) => k.nome));
    const kit = r.porItem.find((i) => nomesKits.has(i.nome) || i.nome.toLowerCase().startsWith('kit'));
    $('c-kit').textContent = kit ? kit.nome : '-';
    $('c-kit-sub').textContent = kit ? textoCliques(kit.total) : 'nenhum clique em kits';
    const celular = r.porDispositivo.find((d) => d.nome === 'celular');
    $('c-celular').textContent = r.total ? Math.round(((celular ? celular.total : 0) / r.total) * 100) + '%' : '-';

    desenharDias($('c-dias'), r);
    mostrarBarras('c-itens', contagem(r.porItem.slice(0, 10)), 'Nenhum clique em kits ou produtos no período.');
    mostrarBarras('c-secoes', contagem(r.porSecao, NOMES_SECAO), 'Nenhum clique no período.');
    mostrarBarras('c-tipos', contagem(r.porTipo, NOMES_TIPO), 'Nenhum clique no período.');
    mostrarBarras('c-dispositivos', contagem(r.porDispositivo, NOMES_APARELHO), 'Nenhum clique no período.');
  } catch (e) {
    if (!(e instanceof ErroApi && e.status === 401)) aviso(e.message, true);
  }
}

/* ---------------- dica dos gráficos (mouse, teclado ou toque) ---------------- */

function mostrarDica(alvo) {
  const dica = $('dica');
  const r = alvo.getBoundingClientRect();
  dica.textContent = alvo.dataset.dica;
  dica.hidden = false;
  const largura = dica.offsetWidth;
  dica.style.left = Math.min(window.innerWidth - largura - 8, Math.max(8, r.left + r.width / 2 - largura / 2)) + 'px';
  dica.style.top = Math.max(8, r.top - dica.offsetHeight - 10) + 'px';
}
function esconderDica() { $('dica').hidden = true; }
document.addEventListener('mouseover', (e) => { const a = e.target.closest('[data-dica]'); if (a) mostrarDica(a); });
document.addEventListener('mouseout', (e) => { if (e.target.closest('[data-dica]')) esconderDica(); });
document.addEventListener('focusin', (e) => { const a = e.target.closest('[data-dica]'); if (a) mostrarDica(a); });
document.addEventListener('focusout', esconderDica);
window.addEventListener('scroll', esconderDica, { passive: true });


/* ---------------- início ---------------- */

credencial = ler(CHAVE_LOGIN);
if (credencial) {
  mostrarPainel();
}
