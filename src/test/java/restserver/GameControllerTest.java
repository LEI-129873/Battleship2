package restserver;

import battleship.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameControllerTest {

    private GameController controller;
    private GameRegistry registry;
    private CallbackClient fakeCallback;
    private AiShotStrategy aiStrategy;

    @BeforeEach
    void setUp() {
        registry = new GameRegistry();
        aiStrategy = new AiShotStrategy();
        fakeCallback = new CallbackClient() {
            @Override
            public boolean sendShots(GameSession session, List<IPosition> aiShots) {
                return true; // Evita chamadas HTTP reais
            }
        };
        controller = new GameController(registry, fakeCallback, aiStrategy);
    }

    // ====================== TESTES AO REGISTER ======================

    @Test
    @DisplayName("Register rejeita playerName nulo")
    void registerRejectsNullPlayerName() {
        Dtos.RegistrationRequest req = new Dtos.RegistrationRequest();
        req.callbackUrl = "http://localhost";
        ResponseEntity<?> res = controller.register(req);
        assertEquals(400, res.getStatusCodeValue());
        assertTrue(res.getBody() instanceof Dtos.ErrorResponse);
        assertEquals("playerName is required", ((Dtos.ErrorResponse) res.getBody()).error);
    }

    @Test
    @DisplayName("Register rejeita playerName em branco")
    void registerRejectsBlankPlayerName() {
        Dtos.RegistrationRequest req = new Dtos.RegistrationRequest();
        req.playerName = "   ";
        req.callbackUrl = "http://localhost";
        ResponseEntity<?> res = controller.register(req);
        assertEquals(400, res.getStatusCodeValue());
        assertEquals("playerName is required", ((Dtos.ErrorResponse) res.getBody()).error);
    }

    @Test
    @DisplayName("Register rejeita callbackUrl nulo")
    void registerRejectsNullCallbackUrl() {
        Dtos.RegistrationRequest req = new Dtos.RegistrationRequest();
        req.playerName = "Alice";
        ResponseEntity<?> res = controller.register(req);
        assertEquals(400, res.getStatusCodeValue());
        assertEquals("callbackUrl is required", ((Dtos.ErrorResponse) res.getBody()).error);
    }

    @Test
    @DisplayName("Register rejeita callbackUrl em branco")
    void registerRejectsBlankCallbackUrl() {
        Dtos.RegistrationRequest req = new Dtos.RegistrationRequest();
        req.playerName = "Alice";
        req.callbackUrl = "   ";
        ResponseEntity<?> res = controller.register(req);
        assertEquals(400, res.getStatusCodeValue());
        assertEquals("callbackUrl is required", ((Dtos.ErrorResponse) res.getBody()).error);
    }

    @Test
    @DisplayName("Register aceita pedido válido")
    void registerAcceptsValidRequest() {
        Dtos.RegistrationRequest req = new Dtos.RegistrationRequest();
        req.playerName = "Alice";
        req.callbackUrl = "http://localhost:9090";
        ResponseEntity<?> res = controller.register(req);
        assertEquals(200, res.getStatusCodeValue());
        Dtos.RegistrationResponse body = (Dtos.RegistrationResponse) res.getBody();
        assertNotNull(body.gameId);
        assertEquals("REGISTERED", body.status);
        assertEquals(3, body.shotsPerTurn);
        assertTrue(body.message.contains("Alice"));
    }

    // ====================== TESTES AO RECEIVESHOTS ======================

    @Test
    @DisplayName("receiveShots devolve 404 se gameId não existir")
    void receiveShotsUnknownGameId() {
        Dtos.ShotRequest req = new Dtos.ShotRequest();
        req.shots = new ArrayList<>();
        ResponseEntity<?> res = controller.receiveShots("id-inexistente", req);
        assertEquals(404, res.getStatusCodeValue());
        assertTrue(((Dtos.ErrorResponse) res.getBody()).error.contains("Game not found"));
    }

    @Test
    @DisplayName("receiveShots devolve 410 se o jogo já acabou")
    void receiveShotsGameAlreadyOver() {
        String gameId = registarJogo("Alice");
        GameSession session = registry.getSession(gameId);
        session.markStudentWins();
        Dtos.ShotRequest req = new Dtos.ShotRequest();
        req.shots = new ArrayList<>();
        ResponseEntity<?> res = controller.receiveShots(gameId, req);
        assertEquals(410, res.getStatusCodeValue());
        assertTrue(((Dtos.ErrorResponse) res.getBody()).error.contains("already over"));
    }

    @Test
    @DisplayName("receiveShots devolve 400 se shots for null")
    void receiveShotsNullShots() {
        String gameId = registarJogo("Alice");
        Dtos.ShotRequest req = new Dtos.ShotRequest();
        ResponseEntity<?> res = controller.receiveShots(gameId, req);
        assertEquals(400, res.getStatusCodeValue());
    }

    @Test
    @DisplayName("receiveShots devolve 400 se o número de tiros for errado")
    void receiveShotsWrongNumberOfShots() {
        String gameId = registarJogo("Alice");
        Dtos.ShotRequest req = new Dtos.ShotRequest();
        req.shots = List.of(criarShot("A", 1), criarShot("B", 2));
        ResponseEntity<?> res = controller.receiveShots(gameId, req);
        assertEquals(400, res.getStatusCodeValue());
        assertTrue(((Dtos.ErrorResponse) res.getBody()).error.contains("Exactly 3 shots"));
    }

    @Test
    @DisplayName("receiveShots devolve 400 se uma posição for inválida (coluna fora do tabuleiro)")
    void receiveShotsInvalidColumn() {
        String gameId = registarJogo("Alice");
        Dtos.ShotRequest req = new Dtos.ShotRequest();
        req.shots = List.of(criarShot("A", 1), criarShot("B", 2), criarShot("C", 11));
        ResponseEntity<?> res = controller.receiveShots(gameId, req);
        assertEquals(400, res.getStatusCodeValue());
        assertTrue(((Dtos.ErrorResponse) res.getBody()).error.contains("Invalid position"));
    }

    @Test
    @DisplayName("receiveShots devolve 400 se row for nulo ou vazio")
    void receiveShotsNullRow() {
        String gameId = registarJogo("Alice");
        Dtos.ShotRequest req = new Dtos.ShotRequest();
        Dtos.ShotPosition sp = criarShot("A", 1);
        sp.row = null;
        req.shots = List.of(sp, criarShot("B", 2), criarShot("C", 3));
        ResponseEntity<?> res = controller.receiveShots(gameId, req);
        assertEquals(400, res.getStatusCodeValue());
    }

    @Test
    @DisplayName("receiveShots processa tiros válidos e devolve HIT, MISS e REPEATED")
    void receiveShotsProcessesValidShots() {
        String gameId = registarJogo("Alice");
        GameSession session = registry.getSession(gameId);
        IShip ship = session.getGame().getMyFleet().getShips().get(0);
        IPosition hitPos = ship.getPositions().get(0);
        IPosition missPos = encontrarAgua(session.getGame().getMyFleet());
        Dtos.ShotRequest req = new Dtos.ShotRequest();
        req.shots = List.of(
                criarShot(String.valueOf(hitPos.getClassicRow()), hitPos.getClassicColumn()),   // HIT
                criarShot(String.valueOf(missPos.getClassicRow()), missPos.getClassicColumn()), // MISS
                criarShot(String.valueOf(hitPos.getClassicRow()), hitPos.getClassicColumn())    // REPEATED (mesmo turno)
        );
        ResponseEntity<?> res = controller.receiveShots(gameId, req);
        assertEquals(200, res.getStatusCodeValue());
        Dtos.ShotResponse body = (Dtos.ShotResponse) res.getBody();
        assertEquals("HIT", body.results.get(0).outcome);
        assertEquals("MISS", body.results.get(1).outcome);
        assertEquals("REPEATED", body.results.get(2).outcome);
        assertEquals("ONGOING", body.gameStatus);
    }

    @Test
    @DisplayName("receiveShots devolve INVALID quando a row está fora do intervalo A-J")
    void receiveShotsInvalidRowOutOfBoard() {
        String gameId = registarJogo("Alice");
        Dtos.ShotRequest req = new Dtos.ShotRequest();
        req.shots = List.of(criarShot("Z", 1), criarShot("A", 1), criarShot("B", 2));
        ResponseEntity<?> res = controller.receiveShots(gameId, req);
        assertEquals(200, res.getStatusCodeValue());
        Dtos.ShotResponse body = (Dtos.ShotResponse) res.getBody();
        assertEquals("INVALID", body.results.get(0).outcome);
    }

    @Test
    @DisplayName("receiveShots marca SUNK quando um navio é afundado")
    void receiveShotsSunkShip() {
        String gameId = registarJogo("Alice");
        GameSession session = registry.getSession(gameId);
        // Achar uma barca (tamanho 1) para afundar
        IShip barge = session.getGame().getMyFleet().getShipsLike("Barca").get(0);
        IPosition bargePos = barge.getPositions().get(0);
        IPosition missPos1 = encontrarAgua(session.getGame().getMyFleet());
        IPosition missPos2 = encontrarAgua(session.getGame().getMyFleet());
        Dtos.ShotRequest req = new Dtos.ShotRequest();
        req.shots = List.of(
                criarShot(String.valueOf(bargePos.getClassicRow()), bargePos.getClassicColumn()),
                criarShot(String.valueOf(missPos1.getClassicRow()), missPos1.getClassicColumn()),
                criarShot(String.valueOf(missPos2.getClassicRow()), missPos2.getClassicColumn())
        );
        ResponseEntity<?> res = controller.receiveShots(gameId, req);
        assertEquals(200, res.getStatusCodeValue());
        Dtos.ShotResponse body = (Dtos.ShotResponse) res.getBody();
        assertEquals("SUNK", body.results.get(0).outcome);
    }

    @Test
    @DisplayName("receiveShots marca GAME_OVER quando todos os navios do AI são afundados")
    void receiveShotsStudentWins() {
        String gameId = registarJogo("Alice");
        GameSession session = registry.getSession(gameId);
        session.getGame().getMyFleet().getShips().forEach(IShip::sink);
        Dtos.ShotRequest req = new Dtos.ShotRequest();
        req.shots = List.of(criarShot("A", 1), criarShot("B", 2), criarShot("C", 3));
        ResponseEntity<?> res = controller.receiveShots(gameId, req);
        assertEquals(200, res.getStatusCodeValue());
        Dtos.ShotResponse body = (Dtos.ShotResponse) res.getBody();
        assertEquals("GAME_OVER", body.gameStatus);
        assertEquals("STUDENT_WINS", body.winner);
        assertTrue(session.isGameOver());
    }

    @Test
    @DisplayName("receiveShots não rebenta se callback do AI falhar")
    void receiveShotsCallbackFails() {
        // Cria um controller com callback falso que devolve false
        CallbackClient failingCallback = new CallbackClient() {
            @Override
            public boolean sendShots(GameSession session, List<IPosition> aiShots) {
                return false;
            }
        };
        GameController c = new GameController(registry, failingCallback, aiStrategy);
        String gameId = registarJogo("Alice");
        Dtos.ShotRequest req = new Dtos.ShotRequest();
        req.shots = List.of(criarShot("A", 1), criarShot("B", 2), criarShot("C", 3));
        ResponseEntity<?> res = c.receiveShots(gameId, req);
        // Deve continuar a devolver 200, só regista um warning no stderr
        assertEquals(200, res.getStatusCodeValue());
    }

    // ====================== MÉTODOS AUXILIARES ======================

    private String registarJogo(String nome) {
        Dtos.RegistrationRequest req = new Dtos.RegistrationRequest();
        req.playerName = nome;
        req.callbackUrl = "http://localhost:9090";
        Dtos.RegistrationResponse body = (Dtos.RegistrationResponse) controller.register(req).getBody();
        return body.gameId;
    }

    private Dtos.ShotPosition criarShot(String row, int col) {
        Dtos.ShotPosition sp = new Dtos.ShotPosition();
        sp.row = row;
        sp.column = col;
        return sp;
    }

    private IPosition encontrarAgua(IFleet fleet) {
        for (int r = 0; r < Game.BOARD_SIZE; r++) {
            for (int c = 0; c < Game.BOARD_SIZE; c++) {
                Position p = new Position(r, c);
                if (fleet.shipAt(p) == null) return p;
            }
        }
        throw new IllegalStateException("Não há água no tabuleiro?");
    }
    @Test
    @DisplayName("receiveShots devolve 400 se a coluna for inferior a 1")
    void receiveShotsColumnBelowRange() {
        String gameId = registarJogo("Alice");
        Dtos.ShotRequest req = new Dtos.ShotRequest();
        req.shots = List.of(criarShot("A", 0), criarShot("B", 2), criarShot("C", 3));
        ResponseEntity<?> res = controller.receiveShots(gameId, req);
        assertEquals(400, res.getStatusCodeValue());
        assertTrue(((Dtos.ErrorResponse) res.getBody()).error.contains("Invalid position"));
    }

    @Test
    @DisplayName("receiveShots devolve 400 se row for vazia")
    void receiveShotsEmptyRow() {
        String gameId = registarJogo("Alice");
        Dtos.ShotRequest req = new Dtos.ShotRequest();
        Dtos.ShotPosition sp = criarShot("A", 1);
        sp.row = "";
        req.shots = List.of(sp, criarShot("B", 2), criarShot("C", 3));
        ResponseEntity<?> res = controller.receiveShots(gameId, req);
        assertEquals(400, res.getStatusCodeValue());
    }
}