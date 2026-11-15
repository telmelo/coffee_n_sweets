package dto;

public class ClienteCafeSemLancheDTO {
    private int IDCliente;
    private String nome;
    private String email;

    public ClienteCafeSemLancheDTO(int IDCliente, String nome, String email) {
        this.IDCliente = IDCliente;
        this.nome = nome;
        this.email = email;
    }

    public int getIDCliente() {
        return IDCliente;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }
}
