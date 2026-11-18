/*
  Coffee n' Sweets — vida na vitrine.
  Tudo aqui é enfeite progressivo: sem JavaScript (ou sem o GSAP do CDN)
  a página continua completa, só fica parada.
  Ideia central: a loja "abre" uma vez — o letreiro acende, o toldo desce
  e o título cai no lugar. Depois disso, o movimento só responde ao usuário
  ou mostra dados chegando (gráficos e números).
*/
(function () {
  "use strict";

  var doc = document.documentElement;
  var gsap = window.gsap;
  var calmo = window.matchMedia("(prefers-reduced-motion: reduce)").matches;
  var animar = !!gsap && !calmo;
  var primeiraVisita = !doc.classList.contains("ja-aberta");
  var CORES = ["#e0457b", "#7a1f4b", "#ffd6e7", "#cfe8f7", "#d7263d", "#ffffff"];

  if (gsap && window.ScrollTrigger) gsap.registerPlugin(window.ScrollTrigger);

  function todos(seletor, raiz) {
    return Array.prototype.slice.call((raiz || document).querySelectorAll(seletor));
  }

  /* ---------- Menu: marcador que mostra a página atual ---------- */
  function iniciarMenu() {
    var menu = document.querySelector(".menu");
    if (!menu) return;
    var links = todos("a", menu);
    var atual = null;
    links.forEach(function (a) {
      var rota = a.getAttribute("href");
      if (location.pathname === rota || location.pathname.indexOf(rota + "/") === 0) atual = a;
    });
    if (atual) atual.setAttribute("aria-current", "page");

    var marcador = document.createElement("span");
    marcador.className = "menu-marcador";
    marcador.setAttribute("aria-hidden", "true");
    menu.appendChild(marcador);
    menu.classList.add("com-marcador");

    var alvo = atual;
    function mover(link) {
      todos("a", menu).forEach(function (a) { a.classList.toggle("sob-marcador", a === link); });
      if (!link) { marcador.style.opacity = "0"; return; }
      marcador.style.opacity = "1";
      marcador.style.width = link.offsetWidth + "px";
      marcador.style.height = link.offsetHeight + "px";
      marcador.style.transform = "translate(" + link.offsetLeft + "px," + link.offsetTop + "px)";
    }
    mover(alvo);
    // só anima depois do primeiro posicionamento, para não "voar" ao carregar
    requestAnimationFrame(function () {
      requestAnimationFrame(function () { marcador.classList.add("pronto"); });
    });

    links.forEach(function (a) {
      a.addEventListener("pointerenter", function () { mover(a); });
      a.addEventListener("focus", function () { mover(a); });
    });
    menu.addEventListener("pointerleave", function () { mover(alvo); });
    menu.addEventListener("focusout", function (e) {
      if (!menu.contains(e.relatedTarget)) mover(alvo);
    });
    // a fonte do menu chega depois do primeiro desenho; remede quando os links mudam de tamanho
    var remedir = function () { mover(menu.querySelector(".sob-marcador") || alvo); };
    if ("ResizeObserver" in window) {
      var ro = new ResizeObserver(remedir);
      links.forEach(function (a) { ro.observe(a); });
    }
    window.addEventListener("resize", remedir);
  }

  /* ---------- Topo: ganha uma faixa de toldo quando o título sai da tela ---------- */
  function iniciarTopo() {
    var topo = document.querySelector(".topo");
    var cabecalho = document.querySelector(".cabecalho");
    if (!topo || !cabecalho || !("IntersectionObserver" in window)) return;
    new IntersectionObserver(function (entradas) {
      topo.classList.toggle("rolou", !entradas[0].isIntersecting);
    }, { rootMargin: "-90px 0px 0px 0px" }).observe(cabecalho);
  }

  /* ---------- Título: letras que caem como confeitos ---------- */
  function separarLetras(h1) {
    var texto = h1.textContent;
    h1.setAttribute("aria-label", texto);
    h1.textContent = "";
    var letras = [];
    texto.split(" ").forEach(function (palavra, i, lista) {
      var p = document.createElement("span");
      p.className = "palavra";
      p.setAttribute("aria-hidden", "true");
      Array.prototype.forEach.call(palavra, function (ch) {
        var l = document.createElement("span");
        l.className = "letra";
        l.textContent = ch;
        p.appendChild(l);
        letras.push(l);
      });
      h1.appendChild(p);
      if (i < lista.length - 1) h1.appendChild(document.createTextNode(" "));
    });
    return letras;
  }

  /* ---------- A abertura da loja ---------- */
  function abertura() {
    var h1 = document.querySelector(".cabecalho h1");
    var tl = gsap.timeline({ defaults: { ease: "power3.out" } });
    var atraso = primeiraVisita ? 0.45 : 0.05;

    if (primeiraVisita) {
      // o letreiro acende com duas piscadas, como neon ligando
      tl.fromTo(".marca", { opacity: 0.2 }, {
        keyframes: { opacity: [0.2, 1, 0.35, 0.9, 0.5, 1] }, duration: 0.5, ease: "none"
      }, 0);
      tl.add(function () { document.querySelector(".marca").classList.add("acesa"); }, 0.45);
    } else {
      var marca = document.querySelector(".marca");
      if (marca) marca.classList.add("acesa");
    }

    if (h1) {
      var letras = separarLetras(h1);
      tl.from(letras, {
        y: -46,
        rotation: function () { return gsap.utils.random(-25, 25); },
        opacity: 0,
        duration: 0.7,
        ease: "back.out(2.2)",
        stagger: 0.028
      }, atraso);
    }

    var barra = document.querySelector(".barra");
    if (barra) tl.from(barra, { scale: 0.6, opacity: 0, duration: 0.55, ease: "back.out(2.5)" }, atraso + 0.25);

    // linhas das tabelas de cadastro entram em cascata curta
    todos(".tabela-caixa").forEach(function (caixa) {
      tl.from(caixa, { y: 18, opacity: 0, duration: 0.5 }, atraso + 0.15);
      var linhas = todos("tr", caixa).filter(function (tr) { return tr.querySelector("td"); }).slice(0, 18);
      tl.from(linhas, { opacity: 0, x: -14, duration: 0.4, stagger: 0.025 }, atraso + 0.3);
    });

    var formularios = todos(".formulario, .form-linha");
    if (formularios.length) {
      tl.from(formularios, { y: 22, opacity: 0, duration: 0.6, stagger: 0.12 }, atraso + 0.15);
      todos(".formulario .campo").forEach(function (campo, i) {
        tl.from(campo, { opacity: 0, y: 10, duration: 0.4 }, atraso + 0.3 + i * 0.06);
      });
    }

    return tl;
  }

  /* ---------- Dashboard: etiquetas de preço que balançam ---------- */
  function lerNumero(texto) {
    var m = texto.trim().match(/^(R\$\s?)?(-?[\d.]+(?:,\d+)?)$/);
    if (!m) return null;
    var bruto = m[2];
    var casas = bruto.indexOf(",") >= 0 ? bruto.split(",")[1].length : 0;
    return {
      prefixo: m[1] || "",
      valor: parseFloat(bruto.replace(/\./g, "").replace(",", ".")),
      casas: casas
    };
  }

  function formatar(n, casas) {
    return n.toLocaleString("pt-BR", { minimumFractionDigits: casas, maximumFractionDigits: casas });
  }

  function iniciarCartoes(tl) {
    var caixa = document.querySelector(".conteudo .cartoes");
    if (!caixa) return;
    var cartoes = todos(".cartao", caixa);

    tl.from(cartoes, {
      opacity: 0,
      y: -24,
      rotation: -14,
      transformOrigin: "18px 50%",
      duration: 1.3,
      ease: "elastic.out(1, 0.42)",
      stagger: 0.07,
      clearProps: "transform",
      onComplete: function () { caixa.classList.add("prontos"); }
    }, primeiraVisita ? 0.6 : 0.2);

    cartoes.forEach(function (cartao, i) {
      var el = cartao.querySelector(".valor");
      if (!el) return;
      var num = lerNumero(el.textContent);
      if (!num) return;
      var final = el.textContent;
      var estado = { v: 0 };
      // reserva a largura final para o número não "empurrar" a etiqueta enquanto conta
      el.style.minWidth = el.offsetWidth + "px";
      tl.to(estado, {
        v: num.valor,
        duration: 1.4,
        ease: "power2.out",
        onUpdate: function () { el.textContent = num.prefixo + formatar(estado.v, num.casas); },
        onComplete: function () { el.textContent = final; el.style.minWidth = ""; }
      }, (primeiraVisita ? 0.7 : 0.3) + i * 0.07);
    });
  }

  /* ---------- Gráficos SVG: barras crescem, linha se desenha ---------- */
  function criarDefs() {
    var ns = "http://www.w3.org/2000/svg";
    var svg = document.createElementNS(ns, "svg");
    svg.setAttribute("width", "0");
    svg.setAttribute("height", "0");
    svg.setAttribute("aria-hidden", "true");
    svg.style.position = "absolute";
    svg.innerHTML =
      '<defs>' +
      '<linearGradient id="cns-barra" x1="0" y1="0" x2="0" y2="1">' +
      '<stop offset="0" stop-color="#f06b9b"/><stop offset="1" stop-color="#c2306a"/></linearGradient>' +
      '<linearGradient id="cns-barra-h" x1="0" y1="0" x2="1" y2="0">' +
      '<stop offset="0" stop-color="#c2306a"/><stop offset="1" stop-color="#f06b9b"/></linearGradient>' +
      '<linearGradient id="cns-area" x1="0" y1="0" x2="0" y2="1">' +
      '<stop offset="0" stop-color="#e0457b" stop-opacity=".32"/><stop offset="1" stop-color="#e0457b" stop-opacity="0"/></linearGradient>' +
      '</defs>';
    document.body.appendChild(svg);
  }

  function prepararDicas(svg, horizontal) {
    todos("rect, circle", svg).forEach(function (marca) {
      var titulo = marca.querySelector("title");
      var texto = "";
      if (titulo) {
        texto = titulo.textContent;
        titulo.remove(); // a dica própria substitui a nativa
      } else if (marca.tagName.toLowerCase() === "rect") {
        // barras verticais: o valor vem logo depois, o rótulo em seguida
        var valor = marca.nextElementSibling;
        var rotulo = valor && valor.nextElementSibling;
        if (valor && rotulo) texto = rotulo.textContent + ": " + valor.textContent;
      }
      if (texto) {
        marca.setAttribute("data-dica", texto);
        marca.setAttribute("tabindex", "0");
        marca.setAttribute("aria-label", texto);
      }
    });
    svg.classList.add(horizontal ? "grafico-h" : "grafico-v");
  }

  function iniciarDica() {
    var dica = document.createElement("div");
    dica.className = "dica";
    dica.setAttribute("role", "tooltip");
    document.body.appendChild(dica);

    function mostrar(alvo, x, y) {
      dica.textContent = alvo.getAttribute("data-dica");
      dica.classList.add("visivel");
      posicionar(x, y);
    }
    function posicionar(x, y) {
      var w = dica.offsetWidth;
      var esquerda = Math.min(Math.max(8, x - w / 2), window.innerWidth - w - 8);
      dica.style.transform = "translate(" + esquerda + "px," + (y - dica.offsetHeight - 14) + "px)";
    }
    function esconder() { dica.classList.remove("visivel"); }

    document.addEventListener("pointermove", function (e) {
      var alvo = e.target.closest && e.target.closest("[data-dica]");
      if (alvo) mostrar(alvo, e.clientX, e.clientY);
      else esconder();
    });
    document.addEventListener("focusin", function (e) {
      var alvo = e.target.closest && e.target.closest("[data-dica]");
      if (!alvo) return;
      var r = alvo.getBoundingClientRect();
      mostrar(alvo, r.left + r.width / 2, r.top);
    });
    document.addEventListener("focusout", esconder);
    window.addEventListener("scroll", esconder, { passive: true });
  }

  function areaSobLinha(svg, linha) {
    var pontos = linha.getAttribute("points").trim().split(/\s+/).map(function (p) { return p.split(",").map(Number); });
    if (pontos.length < 2) return null;
    var base = Math.max.apply(null, todos("line", svg).map(function (l) { return Number(l.getAttribute("y1")); }));
    var d = "M" + pontos[0][0] + "," + base;
    pontos.forEach(function (p) { d += " L" + p[0] + "," + p[1]; });
    d += " L" + pontos[pontos.length - 1][0] + "," + base + " Z";
    var area = document.createElementNS("http://www.w3.org/2000/svg", "path");
    area.setAttribute("d", d);
    area.setAttribute("class", "area");
    linha.parentNode.insertBefore(area, linha);
    return area;
  }

  function iniciarGraficos() {
    var graficos = todos(".conteudo .secao svg");
    if (!graficos.length) return;
    criarDefs();
    iniciarDica();

    graficos.forEach(function (svg) {
      var linha = svg.querySelector("polyline");
      var barras = todos("rect", svg);
      var horizontal = barras.length > 1 && barras.every(function (b) { return b.getAttribute("x") === barras[0].getAttribute("x"); });
      prepararDicas(svg, horizontal);
      var area = linha ? areaSobLinha(svg, linha) : null;

      if (!animar || !window.ScrollTrigger) return;

      var tl = gsap.timeline({
        paused: true,
        scrollTrigger: { trigger: svg, start: "top 85%", once: true }
      });

      if (linha) {
        var comprimento = linha.getTotalLength ? linha.getTotalLength() : 2000;
        gsap.set(linha, { strokeDasharray: comprimento, strokeDashoffset: comprimento });
        tl.to(linha, { strokeDashoffset: 0, duration: 1.6, ease: "power2.inOut" });
        if (area) tl.from(area, { opacity: 0, duration: 0.8 }, 0.6);
        tl.from(todos("circle", svg), {
          scale: 0, transformOrigin: "50% 50%", duration: 0.45, ease: "back.out(3)",
          stagger: 1.4 / Math.max(1, svg.querySelectorAll("circle").length)
        }, 0.1);
      } else if (barras.length) {
        tl.from(barras, {
          scaleX: horizontal ? 0 : 1,
          scaleY: horizontal ? 1 : 0,
          transformOrigin: horizontal ? "0% 50%" : "50% 100%",
          duration: 0.9,
          ease: "power3.out",
          stagger: 0.06
        });
        var valores = todos("text", svg).filter(function (t) {
          return horizontal ? t.getAttribute("text-anchor") !== "end" : t.previousElementSibling && t.previousElementSibling.tagName.toLowerCase() === "rect";
        });
        tl.from(valores, { opacity: 0, y: horizontal ? 0 : 8, x: horizontal ? -8 : 0, duration: 0.4, stagger: 0.06 }, 0.45);
      }
    });

    // gráficos do matplotlib chegam como imagem: só um surgimento suave
    if (animar && window.ScrollTrigger) {
      todos(".conteudo .secao img").forEach(function (img) {
        gsap.from(img, { opacity: 0, y: 24, duration: 0.8, ease: "power2.out",
          scrollTrigger: { trigger: img, start: "top 88%", once: true } });
      });
    }
  }

  /* ---------- Status dos pedidos viram selos coloridos ---------- */
  function iniciarStatus() {
    var classes = { "Pendente": "pendente", "Em preparo": "preparo", "Pronto": "pronto" };
    todos(".tabela-caixa td").forEach(function (td) {
      var texto = td.textContent.trim();
      if (!classes[texto] || td.children.length) return;
      td.textContent = "";
      var selo = document.createElement("span");
      selo.className = "status status-" + classes[texto];
      selo.textContent = texto;
      td.appendChild(selo);
    });
  }

  /* ---------- Respostas às ações do usuário ---------- */
  function granulado(origem) {
    if (!animar) return;
    var r = origem.getBoundingClientRect();
    var x0 = r.left + 26, y0 = r.top + r.height / 2;
    for (var i = 0; i < 34; i++) {
      var g = document.createElement("span");
      g.className = "granulado";
      g.style.background = CORES[i % CORES.length];
      g.style.left = x0 + "px";
      g.style.top = y0 + "px";
      document.body.appendChild(g);
      var angulo = gsap.utils.random(-Math.PI * 0.95, -Math.PI * 0.05);
      var forca = gsap.utils.random(70, 190);
      gsap.timeline({ onComplete: g.remove.bind(g) })
        .to(g, {
          x: Math.cos(angulo) * forca * 1.6,
          y: Math.sin(angulo) * forca,
          rotation: gsap.utils.random(-540, 540),
          duration: gsap.utils.random(0.6, 0.9),
          ease: "power3.out"
        })
        .to(g, { y: "+=" + gsap.utils.random(60, 120), opacity: 0, duration: 0.7, ease: "power1.in" }, ">-0.15");
    }
  }

  function iniciarMensagens() {
    var sucesso = document.querySelector(".sucesso");
    var alerta = document.querySelector(".alerta");
    if (!animar) return;
    if (sucesso) {
      gsap.from(sucesso, { y: -16, opacity: 0, scale: 0.96, duration: 0.6, ease: "back.out(2)", delay: 0.3,
        onStart: function () { granulado(sucesso); } });
    }
    if (alerta) {
      gsap.timeline({ delay: 0.3 })
        .from(alerta, { opacity: 0, y: -10, duration: 0.3 })
        .to(alerta, { keyframes: { x: [0, -9, 8, -6, 4, -2, 0] }, duration: 0.5, ease: "none" });
    }
  }

  function iniciarFormularios() {
    // mostra que o envio está acontecendo; respeita o confirm() das exclusões
    document.addEventListener("submit", function (e) {
      if (e.defaultPrevented) return;
      var botao = e.submitter || e.target.querySelector("button[type=submit], button:not([type])");
      if (!botao) return;
      botao.classList.add("enviando");
      botao.setAttribute("aria-busy", "true");
    });
    // ao voltar pelo histórico, a página pode reaparecer do cache com o botão "enviando"
    window.addEventListener("pageshow", function (e) {
      if (!e.persisted) return;
      todos(".enviando").forEach(function (b) { b.classList.remove("enviando"); b.removeAttribute("aria-busy"); });
    });
  }

  /* ---------- Início ---------- */
  iniciarMenu();
  iniciarTopo();
  iniciarStatus();
  iniciarFormularios();

  if (animar) {
    var tl = abertura();
    iniciarCartoes(tl);
  } else {
    var marca = document.querySelector(".marca");
    if (marca) marca.classList.add("acesa");
  }
  iniciarGraficos();
  iniciarMensagens();
})();
