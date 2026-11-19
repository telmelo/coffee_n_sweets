/*
 * Dashboard interativo do Coffee n' Sweets.
 * Todos os dados vêm de GET /api/dashboard (consultas ao MySQL); este arquivo só desenha.
 * Gráficos: Chart.js (web/js/chart.umd.js). O boxplot é a única imagem, gerada em Python.
 */
(function () {
    'use strict';

    const $ = (seletor) => document.querySelector(seletor);

    if (typeof Chart === 'undefined') {
        const aviso = $('#aviso');
        aviso.textContent = 'Não foi possível carregar o Chart.js (/js/chart.umd.js). '
            + 'Confira se o arquivo está em web/js e se a rota /js foi registrada no Servidor.';
        aviso.hidden = false;
        return;
    }

    // ---------- aparência ----------
    const COR = '#e0457b';
    const PALETA = ['#e0457b', '#8a1c47', '#f08cb0', '#7fb8e0', '#c2306a', '#f9d3e1', '#4f8fc0'];
    const COR_FORMA = { 'Crédito': '#e0457b', 'Débito': '#7fb8e0', 'Dinheiro': '#8a1c47' };
    const COR_CATEGORIA = { 'Ouro': '#e6b422', 'Prata': '#b0b7c3', 'Bronze': '#c8864b', 'Novo': '#f08cb0' };
    const SEMANA = ['Dom', 'Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb'];

    Chart.defaults.font.family = getComputedStyle(document.body).fontFamily;
    Chart.defaults.color = getComputedStyle(document.documentElement).getPropertyValue('--texto').trim() || '#4a2338';
    Chart.defaults.borderColor = 'rgba(224, 69, 123, 0.15)';
    Chart.defaults.maintainAspectRatio = false;
    Chart.defaults.plugins.legend.labels.usePointStyle = true;

    // ---------- formatação ----------
    const moeda = (v) => Number(v).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
    const numero = (v) => Number(v).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    const inteiro = (v) => Number(v).toLocaleString('pt-BR', { maximumFractionDigits: 0 });
    const pct = (v) => Number(v).toLocaleString('pt-BR', { minimumFractionDigits: 1, maximumFractionDigits: 1 }) + '%';
    const dataBR = (iso) => (iso ? iso.slice(8, 10) + '/' + iso.slice(5, 7) + '/' + iso.slice(0, 4) : '—');
    const diaMes = (iso) => dataBR(iso).slice(0, 5);
    const dataHoraBR = (iso) => (iso ? dataBR(iso) + ' ' + iso.slice(11, 16) : '—');
    const corForma = (forma, i) => COR_FORMA[forma] || PALETA[i % PALETA.length];

    // ---------- gráficos ----------
    const graficos = {};

    function desenhar(id, temDados, config, mensagem) {
        const canvas = document.getElementById(id);
        if (!canvas) return;

        // Encontra a secção pai de forma segura, ignorando o .canvas-container
        const secao = canvas.closest('.secao');
        const aviso = secao ? secao.querySelector('.sem-dados') : null;

        if (graficos[id]) {
            graficos[id].destroy();
            delete graficos[id];
        }

        canvas.hidden = !temDados;
        if (aviso) {
            aviso.hidden = temDados;
        }

        if (!temDados) {
            if (aviso) {
                aviso.textContent = mensagem || 'Sem dados para exibir.';
            }
            return;
        }

        graficos[id] = new Chart(canvas, config);
    }

    function barrasH(id, rotulos, valores, serie, formato, mensagem) {
        desenhar(id, rotulos.length > 0, {
            type: 'bar',
            data: { labels: rotulos, datasets: [{ label: serie, data: valores, backgroundColor: COR, borderRadius: 6 }] },
            options: {
                indexAxis: 'y',
                plugins: {
                    legend: { display: false },
                    tooltip: { callbacks: { label: (c) => ' ' + formato(c.parsed.x) } }
                },
                scales: { x: { beginAtZero: true, ticks: { callback: (v) => formato(v) } } }
            }
        }, mensagem);
    }

    function rosca(id, tipo, rotulos, valores, cores, formato, aoClicar) {
        desenhar(id, valores.some((v) => v > 0), {
            type: tipo,
            data: {
                labels: rotulos,
                datasets: [{ data: valores, backgroundColor: cores, borderColor: '#fff', borderWidth: 2 }]
            },
            options: {
                cutout: tipo === 'doughnut' ? '55%' : 0,
                onClick: aoClicar,
                onHover: aoClicar ? (e, itens) => { e.native.target.style.cursor = itens.length ? 'pointer' : 'default'; } : undefined,
                plugins: {
                    legend: { position: 'bottom' },
                    tooltip: {
                        callbacks: {
                            label: (c) => {
                                const total = c.dataset.data.reduce((a, b) => a + b, 0);
                                return ' ' + c.label + ': ' + formato(c.parsed) + ' (' + pct(c.parsed / total * 100) + ')';
                            }
                        }
                    }
                }
            }
        });
    }

    // ---------- cartões e tabelas ----------
    function cartoes(id, itens) {
        const alvo = document.getElementById(id);
        alvo.replaceChildren();
        itens.forEach(([titulo, valor, dica]) => {
            const cartao = document.createElement('div');
            cartao.className = 'cartao';
            const t = document.createElement('div');
            t.textContent = titulo;
            const v = document.createElement('div');
            v.className = 'valor';
            v.textContent = valor;
            cartao.append(t, v);
            if (dica) {
                const d = document.createElement('div');
                d.className = 'dica';
                d.textContent = dica;
                cartao.append(d);
            }
            alvo.append(cartao);
        });
    }

    // colunas: [[titulo, (linha, indice) => texto], ...]; tudo entra com textContent (sem HTML injetado)
    function tabela(id, colunas, linhas, mensagem) {
        const alvo = document.getElementById(id);
        alvo.replaceChildren();
        if (!linhas.length) {
            const p = document.createElement('p');
            p.className = 'vazio';
            p.textContent = mensagem || 'Nenhum registro.';
            alvo.append(p);
            return;
        }
        const t = document.createElement('table');
        const cabecalho = t.createTHead().insertRow();
        colunas.forEach(([titulo]) => {
            const th = document.createElement('th');
            th.textContent = titulo;
            cabecalho.append(th);
        });
        const corpo = t.createTBody();
        linhas.forEach((linha, i) => {
            const tr = corpo.insertRow();
            colunas.forEach(([, valor]) => { tr.insertCell().textContent = valor(linha, i); });
        });
        alvo.append(t);
    }

    function somarPor(linhas, chave, campo) {
        const mapa = new Map();
        linhas.forEach((l) => mapa.set(l[chave], (mapa.get(l[chave]) || 0) + l[campo]));
        return mapa;
    }

    // ---------- aba: visão geral ----------
    function renderGeral(d) {
        const k = d.kpis;
        cartoes('cartoes-geral', [
            ['Faturamento no período', moeda(k.faturamento)],
            ['Pedidos no período', inteiro(k.pedidos)],
            ['Ticket médio', moeda(k.media)],
            ['Pedidos para viagem', pct(k.viagemPct)],
            ['Clientes cadastrados', inteiro(k.clientes)],
            ['Produtos no cardápio', inteiro(k.produtos)]
        ]);

        const porDia = somarPor(d.faturamentoDia, 'data', 'valor');
        const dias = [...porDia.keys()];
        desenhar('g-dia', dias.length > 0, {
            type: 'line',
            data: {
                labels: dias.map(diaMes),
                datasets: [{
                    label: 'Faturamento', data: [...porDia.values()], borderColor: COR,
                    backgroundColor: 'rgba(224, 69, 123, 0.15)', fill: true, tension: 0.25, pointRadius: 3
                }]
            },
            options: {
                plugins: { legend: { display: false }, tooltip: { callbacks: { label: (c) => ' ' + moeda(c.parsed.y) } } },
                scales: { y: { beginAtZero: true, ticks: { callback: (v) => moeda(v) } } }
            }
        });

        const porForma = somarPor(d.faturamentoDia, 'forma', 'valor');
        const formas = [...porForma.keys()];
        rosca('g-forma', 'doughnut', formas, [...porForma.values()], formas.map(corForma), moeda,
            (evento, itens, grafico) => {
                if (!itens.length) return;
                if (evento.type !== 'click') return;
                campoForma.value = grafico.data.labels[itens[0].index];
                carregar();
            });

        const pedidosSemana = new Array(7).fill(0);
        d.faturamentoDia.forEach((r) => { pedidosSemana[new Date(r.data + 'T00:00:00').getDay()] += r.pedidos; });
        desenhar('g-semana', pedidosSemana.some((v) => v > 0), {
            type: 'radar',
            data: {
                labels: SEMANA,
                datasets: [{
                    label: 'Pedidos', data: pedidosSemana, borderColor: COR,
                    backgroundColor: 'rgba(224, 69, 123, 0.25)', pointBackgroundColor: COR
                }]
            },
            options: {
                plugins: { legend: { display: false } },
                scales: { r: { beginAtZero: true, ticks: { precision: 0, backdropColor: 'transparent' } } }
            }
        });
    }

    // ---------- aba: vendas e estatísticas ----------
    function renderVendas(d) {
        const k = d.kpis;
        const temPedidos = k.pedidos > 0;
        cartoes('cartoes-estat', [
            ['Média', temPedidos ? moeda(k.media) : '—', 'valor médio por pedido'],
            ['Mediana', temPedidos ? moeda(k.mediana) : '—', 'valor central dos pedidos'],
            ['Moda', k.moda || '—', 'forma de pagamento mais usada'],
            ['Variância', temPedidos ? numero(k.variancia) : '—', 'em R$², populacional'],
            ['Desvio padrão', temPedidos ? moeda(k.desvio) : '—', 'dispersão em torno da média'],
            ['Coeficiente de variação', temPedidos && k.media > 0 ? pct(k.desvio / k.media * 100) : '—', 'desvio padrão ÷ média']
        ]);

        const rows = d.faturamentoDia;
        const dias = [...new Set(rows.map((r) => r.data))];
        const formas = [...new Set(rows.map((r) => r.forma))].sort();
        const valor = new Map(rows.map((r) => [r.data + '|' + r.forma, r.valor]));
        desenhar('g-empilhado', dias.length > 0, {
            type: 'bar',
            data: {
                labels: dias.map(diaMes),
                datasets: formas.map((forma, i) => ({
                    label: forma,
                    data: dias.map((dia) => valor.get(dia + '|' + forma) || 0),
                    backgroundColor: corForma(forma, i),
                    borderRadius: 3
                }))
            },
            options: {
                interaction: { mode: 'index', intersect: false },
                plugins: { tooltip: { callbacks: { label: (c) => ' ' + c.dataset.label + ': ' + moeda(c.parsed.y) } } },
                scales: {
                    x: { stacked: true },
                    y: { stacked: true, beginAtZero: true, ticks: { callback: (v) => moeda(v) } }
                }
            }
        });

        desenhar('g-hist', d.histograma.length > 0, {
            type: 'bar',
            data: {
                labels: d.histograma.map((h) => h.faixa),
                datasets: [{
                    label: 'Pedidos', data: d.histograma.map((h) => h.quantidade),
                    backgroundColor: COR, borderRadius: 4, categoryPercentage: 1, barPercentage: 0.95
                }]
            },
            options: {
                plugins: { legend: { display: false } },
                scales: {
                    x: { grid: { display: false } },
                    y: { beginAtZero: true, ticks: { precision: 0 }, title: { display: true, text: 'Pedidos' } }
                }
            }
        });

        tabela('tab-faturamento', [
            ['Data', (r) => dataBR(r.data)],
            ['Forma de pagamento', (r) => r.forma],
            ['Faturamento', (r) => moeda(r.valor)]
        ], rows, 'Nenhum pedido no período selecionado.');
    }

    // ---------- aba: produtos ----------
    function renderProdutos(d) {
        barrasH('g-top', d.topProdutos.map((p) => p.nome), d.topProdutos.map((p) => p.total),
            'Unidades vendidas', inteiro, 'Nenhuma venda no período selecionado.');

        const nunca = d.naoVendidos.length;
        rosca('g-vendidos', 'doughnut', ['Já vendidos', 'Nunca vendidos'],
            [Math.max(0, d.totalProdutos - nunca), nunca], [COR, '#7fb8e0'], inteiro);

        const maisCaros = [...d.naoVendidos].sort((a, b) => b.preco - a.preco).slice(0, 15);
        barrasH('g-nao-vendidos', maisCaros.map((p) => p.nome), maisCaros.map((p) => p.preco),
            'Preço', moeda, 'Todos os produtos já foram vendidos pelo menos uma vez.');

        tabela('tab-top', [
            ['#', (p, i) => String(i + 1)],
            ['Produto', (p) => p.nome],
            ['Unidades vendidas', (p) => inteiro(p.total)]
        ], d.topProdutos, 'Nenhuma venda no período selecionado.');

        tabela('tab-nao-vendidos', [
            ['Produto', (p) => p.nome],
            ['Preço', (p) => moeda(p.preco)]
        ], d.naoVendidos, 'Todos os produtos já foram vendidos pelo menos uma vez.');

        tabela('tab-cafes', [
            ['Café', (c) => c.cafe],
            ['Grão', (c) => c.grao],
            ['Origem', (c) => c.origem],
            ['Fornecedor', (c) => c.fornecedor],
            ['Preço/kg', (c) => moeda(c.precoKg)]
        ], d.cafes);

        tabela('tab-log', [
            ['Produto', (l) => l.produto],
            ['De', (l) => moeda(l.de)],
            ['Para', (l) => moeda(l.para)],
            ['Quando', (l) => dataHoraBR(l.quando)],
            ['Usuário', (l) => l.usuario]
        ], d.logsPreco, 'Nenhuma alteração de preço registrada até o momento.');
    }

    // ---------- aba: clientes ----------
    function renderClientes(d) {
        const total = d.categorias.reduce((s, c) => s + c.quantidade, 0);
        rosca('g-categorias', 'pie', d.categorias.map((c) => c.categoria), d.categorias.map((c) => c.quantidade),
            d.categorias.map((c, i) => COR_CATEGORIA[c.categoria] || PALETA[i % PALETA.length]), inteiro);

        // faixas de inatividade calculadas a partir do maior valor (5 faixas)
        const dias = d.ultimoPedido.map((c) => c.dias);
        let rotulos = [];
        let contagem = [];
        if (dias.length) {
            const maximo = Math.max(...dias);
            const passo = Math.max(1, Math.ceil((maximo + 1) / 5));
            contagem = new Array(Math.floor(maximo / passo) + 1).fill(0);
            dias.forEach((x) => { contagem[Math.floor(x / passo)]++; });
            rotulos = contagem.map((_, i) => (passo === 1 ? String(i) : (i * passo) + '–' + ((i + 1) * passo - 1)) + ' dias');
        }
        desenhar('g-inatividade', dias.length > 0, {
            type: 'bar',
            data: { labels: rotulos, datasets: [{ label: 'Clientes', data: contagem, backgroundColor: COR, borderRadius: 4 }] },
            options: {
                plugins: { legend: { display: false } },
                scales: { y: { beginAtZero: true, ticks: { precision: 0 }, title: { display: true, text: 'Clientes' } } }
            }
        }, 'Nenhum cliente com pedidos registrados.');

        const maisTempo = d.ultimoPedido.slice(0, 10);
        barrasH('g-ultimo', maisTempo.map((c) => c.cliente), maisTempo.map((c) => c.dias),
            'Dias sem comprar', (v) => inteiro(v) + ' dias', 'Nenhum cliente com pedidos registrados.');

        const cafe = d.cafeSemLanche.slice(0, 10);
        barrasH('g-cafe', cafe.map((c) => c.nome), cafe.map((c) => c.gasto),
            'Gasto total', moeda, 'Nenhum cliente comprou café sem nunca ter comprado lanche.');

        tabela('tab-categorias', [
            ['Categoria', (c) => c.categoria],
            ['Clientes', (c) => inteiro(c.quantidade)],
            ['Percentual', (c) => pct(total ? c.quantidade / total * 100 : 0)]
        ], d.categorias);

        tabela('tab-ultimo', [
            ['Cliente', (c) => c.cliente],
            ['Último pedido', (c) => '#' + c.pedido],
            ['Data', (c) => dataBR(c.data)],
            ['Pagamento', (c) => c.forma],
            ['Dias sem comprar', (c) => inteiro(c.dias)]
        ], d.ultimoPedido, 'Nenhum cliente com pedidos registrados.');

        const semLanche = d.cafeSemLanche.length;
        const demaisClientes = Math.max(0, (d.kpis.clientes || 0) - semLanche);

        rosca(
            'g-cafe',
            'doughnut',
            ['Só café (nunca lanche)', 'Demais clientes'],
            [semLanche, demaisClientes],
            [COR, '#f08cb0'],
            (v) => inteiro(v) + (v === 1 ? ' cliente' : ' clientes')
        );

        tabela('tab-cafe', [
            ['Cliente', (c) => c.nome],
            ['E-mail', (c) => c.email]
        ], d.cafeSemLanche, 'Nenhum cliente comprou café sem nunca ter comprado lanche.');
    }

    // ---------- aba: operação ----------
    function agruparMesas(linhas) {
        const mapa = new Map();
        linhas.forEach((l) => {
            const semMesa = l.mesa === null;
            const chave = semMesa ? 'sem' : String(l.mesa);
            if (!mapa.has(chave)) {
                mapa.set(chave, {
                    rotulo: semMesa ? 'Sem mesa' : 'Mesa ' + l.mesa,
                    local: semMesa ? 'Sem mesa' : l.localizacao,
                    ordem: semMesa ? Number.MAX_SAFE_INTEGER : l.mesa,
                    pedidos: 0,
                    valor: 0
                });
            }
            const grupo = mapa.get(chave);
            if (l.pedido !== null) {
                grupo.pedidos += 1;
                grupo.valor += l.valor || 0;
            }
        });
        return [...mapa.values()].sort((a, b) => a.ordem - b.ordem);
    }

    function renderMesas(d) {
        const metrica = $('#m-metrica').value; // 'pedidos' ou 'valor'
        const formato = metrica === 'pedidos' ? inteiro : moeda;
        const grupos = agruparMesas(d.mesas);
        const locais = [...new Set(grupos.map((g) => g.local))];

        desenhar('g-mesas', grupos.length > 0, {
            type: 'bar',
            data: {
                labels: grupos.map((g) => g.rotulo),
                datasets: locais.map((local, i) => ({
                    label: local,
                    data: grupos.map((g) => (g.local === local ? g[metrica] : null)),
                    backgroundColor: PALETA[i % PALETA.length],
                    borderRadius: 4
                }))
            },
            options: {
                plugins: { tooltip: { callbacks: { label: (c) => ' ' + c.dataset.label + ': ' + formato(c.parsed.y) } } },
                scales: {
                    x: { stacked: true },
                    y: { stacked: true, beginAtZero: true, ticks: { precision: 0, callback: (v) => formato(v) } }
                }
            }
        });

        const porLocal = locais.map((local) => grupos.filter((g) => g.local === local).reduce((s, g) => s + g[metrica], 0));
        rosca('g-local', 'doughnut', locais, porLocal, locais.map((_, i) => PALETA[i % PALETA.length]), formato);
    }

    function renderOperacao(d) {
        renderMesas(d);

        barrasH('g-baristas', d.baristas.map((b) => b.nome), d.baristas.map((b) => b.itens),
            'Itens preparados', inteiro, 'Nenhum barista acima da média.');

        tabela('tab-mesas', [
            ['Mesa', (m) => (m.mesa === null ? 'Sem mesa' : String(m.mesa))],
            ['Localização', (m) => m.localizacao || '—'],
            ['Pedido', (m) => (m.pedido === null ? 'Sem pedidos' : '#' + m.pedido)],
            ['Data', (m) => dataBR(m.data)],
            ['Valor total', (m) => (m.valor === null ? '—' : moeda(m.valor))]
        ], d.mesas);

        tabela('tab-baristas', [
            ['Barista', (b) => b.nome],
            ['Itens preparados', (b) => inteiro(b.itens)]
        ], d.baristas, 'Nenhum barista acima da média.');
    }

    function render(d) {
        renderGeral(d);
        renderVendas(d);
        renderProdutos(d);
        renderClientes(d);
        renderOperacao(d);
    }

    // ---------- abas ----------
    const abas = [...document.querySelectorAll('.aba')];
    const paineis = [...document.querySelectorAll('.painel')];

    function mostrar(nome, atualizarHash) {
        if (!abas.some((a) => a.dataset.aba === nome)) nome = abas[0].dataset.aba;
        abas.forEach((a) => {
            const ativa = a.dataset.aba === nome;
            a.classList.toggle('ativa', ativa);
            a.setAttribute('aria-selected', String(ativa));
        });
        paineis.forEach((p) => { p.hidden = p.dataset.painel !== nome; });
        if (atualizarHash) history.replaceState(null, '', '#' + nome);
        Object.values(graficos).forEach((g) => g.resize());
    }

    abas.forEach((a) => a.addEventListener('click', () => mostrar(a.dataset.aba, true)));
    window.addEventListener('hashchange', () => mostrar(location.hash.slice(1), false));

    // ---------- filtro e carregamento ----------
    const campoInicio = $('#f-inicio');
    const campoFim = $('#f-fim');
    const campoForma = $('#f-forma');
    const botaoAplicar = $('#f-aplicar');
    let dadosAtuais = null;
    let ultimoBoxplot = 0;

    function parametros() {
        const p = new URLSearchParams();
        if (campoInicio.value) p.set('inicio', campoInicio.value);
        if (campoFim.value) p.set('fim', campoFim.value);
        if (campoForma.value) p.set('forma', campoForma.value);
        return p;
    }

    function mostrarAviso(texto) {
        const aviso = $('#aviso');
        if (aviso) {
            aviso.textContent = texto || '';
            aviso.hidden = !texto;
        }
    }

    function preencherFiltro(f) {
        campoInicio.min = campoFim.min = f.minimo;
        campoInicio.max = campoFim.max = f.maximo;
        campoInicio.value = f.inicio;
        campoFim.value = f.fim;
        if (campoForma.options.length === 1) {
            f.formas.forEach((forma) => campoForma.add(new Option(forma, forma)));
        }
        campoForma.value = f.forma || '';
    }

    async function carregar() {
        mostrarAviso(null);
        botaoAplicar.disabled = true;
        try {
            const resposta = await fetch('/api/dashboard?' + parametros());
            const dados = await resposta.json();
            if (!resposta.ok) throw new Error(dados.erro || 'Erro ao carregar o dashboard.');
            dadosAtuais = dados;
            preencherFiltro(dados.filtro);
            render(dados);
            carregarBoxplot();
        } catch (erro) {
            mostrarAviso(erro.message);
        } finally {
            botaoAplicar.disabled = false;
        }
    }

    // o boxplot é gerado em Python sob demanda, com o mesmo filtro dos demais gráficos
    async function carregarBoxplot() {
        const minha = ++ultimoBoxplot;
        const imagem = $('#boxplot-img');
        const mensagem = $('#boxplot-msg');
        if (mensagem) {
            mensagem.textContent = 'Gerando o boxplot…';
            mensagem.hidden = false;
        }
        if (imagem) imagem.hidden = true;

        try {
            const resposta = await fetch('/api/boxplot?' + parametros());
            const corpo = await resposta.json();
            if (minha !== ultimoBoxplot) return;
            if (!resposta.ok || !corpo.ok) throw new Error(corpo.erro || 'Não foi possível gerar o boxplot.');

            // Define a nova imagem com timestamp para evitar cache
            imagem.src = '/graficos/faturamento_boxplot.png?v=' + Date.now();

            // Oculta a mensagem e exibe a imagem imediatamente após o sucesso da API
            if (mensagem) mensagem.hidden = true;
            if (imagem) imagem.hidden = false;
        } catch (erro) {
            if (minha === ultimoBoxplot && mensagem) {
                mensagem.textContent = erro.message;
            }
        }
    }

    $('#filtro').addEventListener('submit', (evento) => {
        evento.preventDefault();
        carregar();
    });

    $('#f-limpar').addEventListener('click', () => {
        campoInicio.value = '';
        campoFim.value = '';
        campoForma.value = '';
        carregar();
    });

    $('#m-metrica').addEventListener('change', () => {
        if (dadosAtuais) renderMesas(dadosAtuais);
    });

    mostrar(location.hash.slice(1), false);
    carregar();
})();