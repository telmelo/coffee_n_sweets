package dto;

import java.time.LocalDate;

public record PedidoStatusDTO(int idPagamento, LocalDate data, String status, double valorTotal,
                              int itens, int itensAtribuidos) {
}
