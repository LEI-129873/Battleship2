package restserver;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameSessionTest {

    private GameSession session;

    @BeforeEach
    void setUp() {
        session = new GameSession("id-teste-123", "Pedro123", "http://localhost:9090");
    }

    @Test
    void getGameId() {
        assertEquals("id-teste-123", session.getGameId());
    }

    @Test
    void getPlayerName() {
        assertEquals("Pedro123", session.getPlayerName());
    }

    @Test
    void getCallbackUrl() {
        assertEquals("http://localhost:9090", session.getCallbackUrl());
    }

    @Test
    void getGame() {
        assertNotNull(session.getGame(), "O Jogo não ode ser null");
    }

    @Test
    @DisplayName("O jogo tem exatamente 3 SHOTS por turno")
    void getShotsPerTurn() {
        assertEquals(3, session.getShotsPerTurn());
    }

    @Test
    void isGameOver() {
        assertFalse(session.isGameOver(), "O jogo não pode começar terminado");
    }

    @Test
    void getWinner() {
        assertNull(session.getWinner(), "O jogo não pode começar com um vencedor");
    }

    @Test
    void markAiWins() {
        session.markAiWins();

        assertTrue(session.isGameOver(), "O jogo não pode ter vencedor em ter acabado");
        assertEquals("AI_WINS", session.getWinner());

    }

    @Test
    void markStudentWins() {
        session.markStudentWins();

        assertTrue(session.isGameOver(), "O jogo não pode ter vencedor em ter acabado");
        assertEquals("STUDENT_WINS", session.getWinner());
    }
}