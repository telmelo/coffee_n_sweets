import json
import os
import sys
from collections import defaultdict
from datetime import date

import matplotlib

matplotlib.use("Agg")
import matplotlib.dates as mdates
import matplotlib.pyplot as plt

COR_PRINCIPAL = "#e0457b"


def moeda(valor):
    """Formata como moeda brasileira: R$ 1.234,56"""
    texto = f"R$ {valor:,.2f}"
    return texto.replace(",", "X").replace(".", ",").replace("X", ".")


def salvar(figura, pasta, nome_arquivo):
    figura.tight_layout()
    figura.savefig(os.path.join(pasta, nome_arquivo), dpi=110)
    plt.close(figura)


def grafico_vazio(pasta, nome_arquivo, titulo):
    figura, eixo = plt.subplots(figsize=(6, 3))
    eixo.text(0.5, 0.5, "Sem dados para exibir", ha="center", va="center", fontsize=13, color="#666")
    eixo.set_title(titulo)
    eixo.axis("off")
    salvar(figura, pasta, nome_arquivo)


def grafico_boxplot_por_forma(dados, pasta):
    titulo = "Distribuição do faturamento diário por forma de pagamento"
    if not dados:
        return grafico_vazio(pasta, "faturamento_boxplot.png", titulo)

    por_forma = defaultdict(list)
    for linha in dados:
        por_forma[linha["forma"]].append(linha["valor"])

    formas = sorted(por_forma)
    valores = [por_forma[forma] for forma in formas]

    figura, eixo = plt.subplots(figsize=(7, 4.5))
    caixas = eixo.boxplot(
        valores,
        patch_artist=True,
        showmeans=True,
        medianprops={"color": COR_PRINCIPAL, "linewidth": 2},
        meanprops={"marker": "D", "markerfacecolor": "white", "markeredgecolor": "black"},
    )
    for caixa in caixas["boxes"]:
        caixa.set_facecolor("#ffd6e7")

    eixo.set_xticks(range(1, len(formas) + 1))
    eixo.set_xticklabels(formas)
    eixo.set_title(titulo)
    eixo.set_ylabel("Faturamento diário (R$)")
    eixo.grid(axis="y", alpha=0.3)
    salvar(figura, pasta, "faturamento_boxplot.png")


def grafico_faturamento_diario(dados, pasta):
    titulo = "Faturamento diário"
    if not dados:
        return grafico_vazio(pasta, "faturamento_diario.png", titulo)

    totais = defaultdict(float)
    for linha in dados:
        totais[date.fromisoformat(linha["data"])] += linha["valor"]

    dias = sorted(totais)
    valores = [totais[dia] for dia in dias]

    figura, eixo = plt.subplots(figsize=(8, 4))
    eixo.plot(dias, valores, marker="o", color=COR_PRINCIPAL)
    eixo.xaxis.set_major_formatter(mdates.DateFormatter("%d/%m"))
    eixo.set_title(titulo)
    eixo.set_ylabel("Faturamento (R$)")
    eixo.grid(alpha=0.3)
    figura.autofmt_xdate()
    salvar(figura, pasta, "faturamento_diario.png")


def grafico_barras_horizontais(itens, rotulo_nome, rotulo_valor, titulo, rotulo_eixo, nome_arquivo, pasta):
    if not itens:
        return grafico_vazio(pasta, nome_arquivo, titulo)

    nomes = [item[rotulo_nome] for item in itens]
    valores = [item[rotulo_valor] for item in itens]

    figura, eixo = plt.subplots(figsize=(8, max(3, 0.45 * len(itens) + 1.5)))
    barras = eixo.barh(nomes, valores, color=COR_PRINCIPAL)
    eixo.bar_label(barras, padding=3)
    eixo.invert_yaxis()  # o maior fica no topo
    eixo.set_title(titulo)
    eixo.set_xlabel(rotulo_eixo)
    eixo.margins(x=0.1)
    salvar(figura, pasta, nome_arquivo)


def main():
    sys.stdin.reconfigure(encoding="utf-8")
    sys.stdout.reconfigure(encoding="utf-8")
    sys.stderr.reconfigure(encoding="utf-8")

    if len(sys.argv) < 2:
        print("Uso: python graficos.py <pasta_de_saida>", file=sys.stderr)
        sys.exit(1)

    pasta = sys.argv[1]
    os.makedirs(pasta, exist_ok=True)
    dados = json.load(sys.stdin)

    grafico_boxplot_por_forma(dados.get("faturamento", []), pasta)
    grafico_faturamento_diario(dados.get("faturamento", []), pasta)
    grafico_barras_horizontais(
        dados.get("top_produtos", []), "nome", "total",
        "Os 10 produtos mais vendidos", "Unidades vendidas", "top_produtos.png", pasta)
    grafico_barras_horizontais(
        dados.get("baristas", []), "nome", "itens",
        "Baristas acima da média de produtividade", "Itens preparados", "baristas.png", pasta)

    print("Gráficos gerados em:", os.path.abspath(pasta))


if __name__ == "__main__":
    main()