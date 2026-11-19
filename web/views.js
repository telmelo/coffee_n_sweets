(function () {
    'use strict';
    const $ = (s) => document.querySelector(s);

    if (typeof Chart === 'undefined') {
        const a = $('#aviso');
        a.textContent = 'Não foi possível carregar o Chart.js.';
        a.hidden = false;
        return;
    }

    const COR = '#e0457b';
    const PALETA = ['#e0457b', '#8a1c47', '#f08cb0', '#7fb8e0', '#c2306a', '#f9d3e1', '#4f8fc0'];
    const COR_FORMA = { 'Crédito': '#e0457b', 'Débito': '#7fb8e0', 'Dinheiro': '#8a1c47' };

    Chart.defaults.font.family = getComputedStyle(document.body).fontFamily;
    Chart.defaults.color = '#4a2338';
    Chart.defaults.borderColor = 'rgba(224, 69, 123, 0.15)';
    Chart.defaults.maintainAspectRatio = false;
    Chart.defaults.plugins.legend.labels.usePointStyle = true;

    const moeda = (v) => Number(v).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
    const inteiro = (v) => Number(v).toLocaleString('pt-BR', { maximumFractionDigits: 0 });
    const pct = (v) => Number(v).toLocaleString('pt-BR', { minimumFractionDigits: 1, maximumFractionDigits: 1 }) + '%';
    const dataBR = (iso) => (iso ? iso.slice(8, 10) + '/' + iso.slice(5, 7) + '/' + iso.slice(0, 4) : '—');

    // ---------- helpers (mesmo padrão do dashboard.js) ----------
    const graficos = {};

    function desenhar(id, temDados, config, mensagem) {
        const canvas = document.getElementById(id);
        if (!canvas) return;
        const aviso = canvas.closest('.secao').querySelector('.sem-dados');
        if (graficos[id]) { graficos[id].destroy(); delete graficos[id]; }
        canvas.hidden = !temDados;
        aviso.hidden = temDados;
        if (!temDados) { aviso.textContent = mensagem || 'Sem dados para exibir.'; return; }
        graficos[id] = new Chart(canvas, config);
    }

    function cartoes(id, itens) {
        const alvo = document.getElementById(id);
        alvo.replaceChildren();
        alvo.classList.add('prontos');   // ativa o balanço no hover definido no CSS
        itens.forEach(([titulo, valor]) => {
            const c = document.createElement('div');
            c.className = 'cartao';
            const t = document.createElement('div');
            t.textContent = titulo;
            const v = document.createElement('div');
            v.className = 'valor';
            v.textContent = valor;
            c.append(t, v);
            alvo.append(c);
        });
    }

    function tabela(id, colunas, linhas, mensagem) {
        const alvo = document.getElementById(id);
        alvo.replaceChildren();
        if (!linhas.length) {
            const p = document.createElement('p');
            p.textContent = mensagem || 'Nenhum registro.';
            alvo.append(p);
            return;
        }
        const t = document.createElement('table');
        const cab = t.createTHead().insertRow();
        colunas.forEach(([titulo]) => {
            const th = document.createElement('th');
            th.textContent = titulo;
            cab.append(th);
        });
        const corpo = t.createTBody();
        linhas.forEach((l) => {
            const tr = corpo.insertRow();
            colunas.forEach(([, valor]) => { tr.insertCell().textContent = valor(l); });
        });
        alvo.append(t);
    }

    function somarPor(linhas, chave, campo) {
        const mapa = new Map();
        linhas.forEach((l) => mapa.set(l[chave], (mapa.get(l[chave]) || 0) + l[campo]));
        return mapa;
    }

    // ---------- aba: vendas (vw_vendas_detalhadas) ----------
    function renderVendas(d) {
        const v = d.vendas;
        const faturamento = v.reduce((s, l) => s + l.subtotal, 0);
        const itens = v.reduce((s, l) => s + l.quantidade, 0);
        const pedidos = new Set(v.map((l) => l.pedido));
        const viagem = new Set(v.filter((l) => l.tipo === 'Para viagem').map((l) => l.pedido));

        cartoes('cartoes-vendas', [
            ['Faturamento (itens)', moeda(faturamento)],
            ['Pedidos', inteiro(pedidos.size)],
            ['Unidades vendidas', inteiro(itens)],
            ['Ticket médio', moeda(pedidos.size ? faturamento / pedidos.size : 0)],
            ['Pedidos para viagem', pct(pedidos.size ? viagem.size * 100 / pedidos.size : 0)]
        ]);

        const porDia = somarPor(v, 'data', 'subtotal');
        desenhar('g-v-dia', porDia.size > 0, {
            type: 'line',
            data: {
                labels: [...porDia.keys()].map((x) => dataBR(x).slice(0, 5)),
                datasets: [{ label: 'Faturamento', data: [...porDia.values()], borderColor: COR,
                    backgroundColor: 'rgba(224, 69, 123, 0.15)', fill: true, tension: 0.25, pointRadius: 3 }]
            },
            options: {
                plugins: { legend: { display: false }, tooltip: { callbacks: { label: (c) => ' ' + moeda(c.parsed.y) } } },
                scales: { y: { beginAtZero: true, ticks: { callback: (x) => moeda(x) } } }
            }
        });

        const porForma = somarPor(v, 'forma', 'subtotal');
        const formas = [...porForma.keys()];
        desenhar('g-v-forma', formas.length > 0, {
            type: 'doughnut',
            data: { labels: formas, datasets: [{ data: [...porForma.values()],
                    backgroundColor: formas.map((f, i) => COR_FORMA[f] || PALETA[i % PALETA.length]),
                    borderColor: '#fff', borderWidth: 2 }] },
            options: { cutout: '55%', plugins: { legend: { position: 'bottom' },
                    tooltip: { callbacks: { label: (c) => ' ' + c.label + ': ' + moeda(c.parsed) } } } }
        });

        const topProdutos = [...somarPor(v, 'produto', 'subtotal').entries()]
            .sort((a, b) => b[1] - a[1]).slice(0, 8);
        desenhar('g-v-produto', topProdutos.length > 0, {
            type: 'bar',
            data: { labels: topProdutos.map((p) => p[0]),
                datasets: [{ data: topProdutos.map((p) => p[1]), backgroundColor: COR, borderRadius: 6 }] },
            options: {
                indexAxis: 'y',
                plugins: { legend: { display: false }, tooltip: { callbacks: { label: (c) => ' ' + moeda(c.parsed.x) } } },
                scales: { x: { beginAtZero: true, ticks: { callback: (x) => moeda(x) } } }
            }
        });

        tabela('tab-vendas', [
            ['Pedido', (l) => '#' + l.pedido], ['Item', (l) => String(l.item)],
            ['Data', (l) => dataBR(l.data)], ['Hora', (l) => (l.horario || '').slice(0, 5)],
            ['Cliente', (l) => l.cliente], ['Atendente', (l) => l.atendente],
            ['Mesa', (l) => l.mesa], ['Produto', (l) => l.produto],
            ['Qtd', (l) => inteiro(l.quantidade)], ['Preço', (l) => moeda(l.preco)],
            ['Subtotal', (l) => moeda(l.subtotal)], ['Pagamento', (l) => l.forma]
        ], v, 'Nenhuma venda no filtro selecionado.');
    }

    // ---------- aba: produção (vw_producao_barista_equipamento) ----------
    function renderProducao(d) {
        const p = d.producao;
        cartoes('cartoes-producao', [
            ['Baristas', inteiro(new Set(p.map((l) => l.barista)).size)],
            ['Equipamentos usados', inteiro(new Set(p.map((l) => l.equipamento)).size)],
            ['Itens preparados', inteiro(p.reduce((s, l) => s + l.itens, 0))],
            ['Unidades produzidas', inteiro(p.reduce((s, l) => s + l.unidades, 0))]
        ]);

        const porBarista = somarPor(p, 'barista', 'unidades');
        desenhar('g-p-barista', porBarista.size > 0, {
            type: 'bar',
            data: {
                labels: [...porBarista.keys()],
                datasets: [{ label: 'Unidades produzidas', data: [...porBarista.values()],
                    backgroundColor: COR, borderRadius: 6 }]
            },
            options: {
                plugins: { legend: { display: false } },
                scales: { y: { beginAtZero: true, ticks: { precision: 0 } } }
            }
        }, 'Nenhuma produção registrada.');

        const porEquip = [...somarPor(p, 'equipamento', 'unidades').entries()]
            .sort((a, b) => b[1] - a[1]);
        desenhar('g-p-equip', porEquip.length > 0, {
            type: 'bar',
            data: {
                labels: porEquip.map((e) => e[0]),
                datasets: [{ label: 'Unidades produzidas', data: porEquip.map((e) => e[1]),
                    backgroundColor: COR, borderRadius: 6 }]
            },
            options: {
                indexAxis: 'y',
                plugins: { legend: { display: false } },
                scales: { x: { beginAtZero: true, ticks: { precision: 0 } } }
            }
        }, 'Nenhuma produção registrada.');

        tabela('tab-producao', [
            ['Barista', (l) => l.barista], ['Equipamento', (l) => l.equipamento],
            ['Itens', (l) => inteiro(l.itens)], ['Unidades', (l) => inteiro(l.unidades)],
            ['Produtos diferentes', (l) => inteiro(l.produtos)],
            ['Primeiro preparo', (l) => dataBR(l.primeiro)], ['Último preparo', (l) => dataBR(l.ultimo)]
        ], p, 'Nenhuma produção para o filtro selecionado.');
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
        paineis.forEach((x) => { x.hidden = x.dataset.painel !== nome; });
        if (atualizarHash) history.replaceState(null, '', '#' + nome);
        Object.values(graficos).forEach((g) => g.resize());
    }
    abas.forEach((a) => a.addEventListener('click', () => mostrar(a.dataset.aba, true)));
    window.addEventListener('hashchange', () => mostrar(location.hash.slice(1), false));

    // ---------- filtros e carregamento ----------
    const campo = { inicio: $('#f-inicio'), fim: $('#f-fim'), forma: $('#f-forma'), tipo: $('#f-tipo'),
        barista: $('#p-barista'), equipamento: $('#p-equip') };
    const botao = $('#f-aplicar');

    function parametros() {
        const p = new URLSearchParams();
        Object.entries(campo).forEach(([chave, el]) => { if (el.value) p.set(chave, el.value); });
        return p;
    }

    function preencherSelect(select, opcoes, valor) {
        if (select.options.length === 1) opcoes.forEach(([v, rotulo]) => select.add(new Option(rotulo, v)));
        select.value = valor == null ? '' : String(valor);
    }

    function preencherFiltros(d) {
        const f = d.filtro;
        campo.inicio.min = campo.fim.min = f.minimo;
        campo.inicio.max = campo.fim.max = f.maximo;
        campo.inicio.value = f.inicio;
        campo.fim.value = f.fim;
        preencherSelect(campo.forma, f.formas.map((x) => [x, x]), f.forma);
        preencherSelect(campo.tipo, f.tipos.map((x) => [x, x]), f.tipo);
        preencherSelect(campo.barista, d.baristas.map((b) => [b.id, b.nome]), f.barista);
        preencherSelect(campo.equipamento, d.equipamentos.map((e) => [e.id, e.nome]), f.equipamento);
    }

    function aviso(texto) {
        const a = $('#aviso');
        a.textContent = texto || '';
        a.hidden = !texto;
    }

    async function carregar() {
        aviso(null);
        botao.disabled = true;
        try {
            const resposta = await fetch('/api/views?' + parametros());
            const dados = await resposta.json();
            if (!resposta.ok) throw new Error(dados.erro || 'Erro ao carregar as views.');
            preencherFiltros(dados);
            renderVendas(dados);
            renderProducao(dados);
        } catch (erro) {
            aviso(erro.message);
        } finally {
            botao.disabled = false;
        }
    }

    $('#filtro').addEventListener('submit', (e) => { e.preventDefault(); carregar(); });
    $('#f-limpar').addEventListener('click', () => {
        Object.values(campo).forEach((el) => { el.value = ''; });
        carregar();
    });
    campo.barista.addEventListener('change', carregar);
    campo.equipamento.addEventListener('change', carregar);

    mostrar(location.hash.slice(1), false);
    carregar();
})();