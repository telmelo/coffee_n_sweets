package main;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class Conexao {

    private static final Properties config = carregar();

    private static Properties carregar() {
        Properties p = new Properties();
        try (InputStream in = new FileInputStream("config.properties")) {
            p.load(new InputStreamReader(in, StandardCharsets.UTF_8));
        } catch (IOException e) {
            System.err.println("Não foi possível ler config.properties: " + e.getMessage());
            System.err.println("Copie config.properties.example para config.properties e ajuste os valores.");
        }
        return p;
    }

    public static Connection getConexao() throws SQLException {
        return DriverManager.getConnection(
                config.getProperty("DB_URL"),
                config.getProperty("DB_USER"),
                config.getProperty("DB_PASSWORD"));
    }
}