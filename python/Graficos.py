"""Gera só o boxplot do faturamento diário por forma de pagamento (matplotlib).

Os demais gráficos do dashboard são desenhados no navegador com Chart.js.
Uso: python graficos.py <pasta_de_saida>   (o JSON chega pela entrada padrão)
"""
import json
import os
import sys
from collections import defaultdict

import matplotlib

matplotlib.use("Agg")
import matplotlib.pyplot as plt

COR_PRINCIPAL = "#e0457b"
COR_CAIXA = "#ffd6e7"
NOME_ARQUIVO = "faturamento_boxplot.png"


def salvar(figura, pasta):
    figura.tight_layout()
    # fundo transparente: o cartão do dashboard já tem fundo próprio
    figura.savefig(os.path.join(pasta, NOME_ARQUIVO), dpi=110, transparent=True, bbox_inches="tight")
    plt.close(figura)


def grafico_vazio(pasta):
    figura, eixo = plt.subplots(figsize=(6, 3))
    eixo.text(0.5, 0.5, "Sem dados para exibir", ha="center", va="center", fontsize=13, color="#666")
    eixo.axis("off")
    salvar(figura, pasta)


def grafico_boxplot_por_forma(dados, pasta):
    if not dados:
        return grafico_vazio(pasta)

    por_forma = defaultdict(list)
    for linha in dados:
        por_forma[linha["forma"]].append(linha["valor"])

    formas = sorted(por_forma)
    valores = [por_forma[forma] for forma in formas]

    figura, eixo = plt.subplots(figsize=(8, 4.5))
    caixas = eixo.boxplot(
        valores,
        patch_artist=True,
        showmeans=True,
        medianprops={"color": COR_PRINCIPAL, "linewidth": 2},
        meanprops={"marker": "D", "markerfacecolor": "white", "markeredgecolor": "black"},
    )
    for caixa in caixas["boxes"]:
        caixa.set_facecolor(COR_CAIXA)

    eixo.set_xticks(range(1, len(formas) + 1))
    eixo.set_xticklabels([f"{forma}\n(n = {len(por_forma[forma])} dias)" for forma in formas])
    eixo.set_ylabel("Faturamento diário (R$)")
    eixo.grid(axis="y", alpha=0.3)
    # legenda acima da área dos dados, para nunca cobrir um valor atípico
    eixo.legend([caixas["medians"][0], caixas["means"][0]], ["Mediana", "Média"],
                loc="lower right", bbox_to_anchor=(1.0, 1.01), ncol=2, frameon=False)
    salvar(figura, pasta)


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
    print("Boxplot gerado em:", os.path.abspath(os.path.join(pasta, NOME_ARQUIVO)))


if __name__ == "__main__":
    main()