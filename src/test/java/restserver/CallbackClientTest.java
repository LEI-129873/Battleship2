package restserver;

import battleship.IGame;
import battleship.IPosition;
import battleship.Position;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes para o cliente HTTP de callback")
class CallbackClientTest {

    private CallbackClient client;
    private GameSession session;

    @BeforeEach
    void setUp() {
        client = new CallbackClient();
        session = new GameSession("id-teste", "Pedro", "http://localhost:9999");
    }

    @Test
    @DisplayName("Deve retornar false se o servidor do jogador estiver offline")
    void testSendShotsServerOffline() {
        List<IPosition> shots = List.of(new Position(0, 0),
                new Position(1, 1),
                new Position(2, 2));

        // Vai tentar ligar ao localhost:9999. Como não existe, deve apanhar a exceção e devolver false
        boolean result = client.sendShots(session, shots);

        assertFalse(result, "Deve retornar false quando a conexão HTTP falha");
    }

    @Test
    @DisplayName("Deve processar a resposta do servidor e atualizar o jogo em caso de sucesso")
    void testSendShotsSuccess() throws IOException {
        // 1. Criar um servidor HTTP falso na porta 0 (o SO escolhe uma porta livre)
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);

        // 2. Configurar o endpoint para devolver um JSON válido (m2b)
        server.createContext("/game/", exchange -> {
            String jsonResponse = "{\n" +
                    "  \"results\": [\n" +
                    "    {\"row\":\"A\", \"column\":1, \"outcome\":\"MISS\"},\n" +
                    "    {\"row\":\"B\", \"column\":2, \"outcome\":\"MISS\"},\n" +
                    "    {\"row\":\"C\", \"column\":3, \"outcome\":\"SUNK\"}\n" +
                    "  ],\n" +
                    "  \"shipsRemaining\": 0,\n" +
                    "  \"gameStatus\": \"GAME_OVER\",\n" +
                    "  \"winner\": \"AI_WINS\"\n" +
                    "}";
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, jsonResponse.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(jsonResponse.getBytes());
            }
        });
        server.start();

        // Atualizar a sessão para apontar para a porta do servidor que acabámos de criar
        int port = server.getAddress().getPort();
        session = new GameSession("id-teste", "Pedro", "http://localhost:" + port);

        List<IPosition> shots = List.of(new Position(0, 0), new Position(1, 1), new Position(2, 2));

        // 3. Enviar os tiros
        boolean result = client.sendShots(session, shots);

        // 4. Verificações
        assertTrue(result, "Deve retornar true quando o servidor responde com sucesso");

        // Verificar se o recordAiMove guardou a jogada no jogo da sessão
        IGame game = session.getGame();
        assertEquals(1, game.getAlienMoves().size(), "A jogada da IA deve ser registada no jogo");
        assertEquals(3, game.getAlienMoves().get(0).getShots().size(), "Devem estar registados 3 tiros");

        // Como o JSON dizia GAME_OVER, a sessão deve ter terminado com vitória da IA
        assertTrue(session.isGameOver(), "O jogo deve estar terminado após o GAME_OVER");
        assertEquals("AI_WINS", session.getWinner(), "A IA deve ser declarada vencedora");

        server.stop(0); // Parar o servidor para libertar a porta
    }
}
