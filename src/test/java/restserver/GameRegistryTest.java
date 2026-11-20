package restserver;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for class GameRegistry.
 * Author: SENAS
 * Date: 2026-11-20 16:01
 * Cyclomatic Complexity:
 * - constructor GameRegistry(): 1
 * - createSession(String, String): 1
 * - getSession(String): 1
 * - removeSession(String): 1
 */
class GameRegistryTest {

	private static final String PLAYER = "Alice";
	private static final String CALLBACK = "http://localhost:9090";

	private GameRegistry registry;

	@BeforeEach
	void setUp() {
		registry = new GameRegistry();
	}

	@AfterEach
	void tearDown() {
		registry = null;
	}

	@Test
	@DisplayName("Um registo acabado de criar não tem sessões")
	void constructor() {
		assertAll(
				() -> assertNotNull(registry, "Error: expected a registry instance but got null"),
				() -> assertNull(registry.getSession("qualquer-id"),
						"Error: expected no session in a new registry but one was found"));
	}

	@Test
	@DisplayName("createSession cria uma sessão com os dados indicados, guarda-a e gera ids únicos")
	void createSession() {
		GameSession session = registry.createSession(PLAYER, CALLBACK);
		GameSession other = registry.createSession("Bob", "http://localhost:9191");
		assertAll(
				() -> assertNotNull(session, "Error: expected a new session but got null"),
				() -> assertNotNull(session.getGameId(), "Error: expected a generated gameId but got null"),
				() -> assertEquals(PLAYER, session.getPlayerName(),
						"Error: expected player " + PLAYER + " but got " + session.getPlayerName()),
				() -> assertEquals(CALLBACK, session.getCallbackUrl(),
						"Error: expected callback " + CALLBACK + " but got " + session.getCallbackUrl()),
				() -> assertSame(session, registry.getSession(session.getGameId()),
						"Error: expected the created session to be stored in the registry"),
				() -> assertNotEquals(session.getGameId(), other.getGameId(),
						"Error: expected different gameIds for two sessions but both were " + session.getGameId()));
	}

	@Test
	@DisplayName("getSession devolve a sessão guardada ou null quando o id não existe")
	void getSession() {
		GameSession session = registry.createSession(PLAYER, CALLBACK);
		assertAll(
				() -> assertSame(session, registry.getSession(session.getGameId()),
						"Error: expected the stored session for its gameId but got another object"),
				() -> assertNull(registry.getSession("id-inexistente"),
						"Error: expected null for an unknown gameId but got a session"));
	}

	@Test
	@DisplayName("removeSession retira a sessão indicada e ignora ids inexistentes")
	void removeSession() {
		GameSession session = registry.createSession(PLAYER, CALLBACK);
		GameSession kept = registry.createSession("Bob", "http://localhost:9191");
		registry.removeSession(session.getGameId());
		assertAll(
				() -> assertNull(registry.getSession(session.getGameId()),
						"Error: expected the removed session to be gone but it is still stored"),
				() -> assertSame(kept, registry.getSession(kept.getGameId()),
						"Error: expected the other session to stay in the registry but it was removed"),
				() -> assertDoesNotThrow(() -> registry.removeSession("id-inexistente"),
						"Error: expected no exception when removing an unknown gameId"));
	}
}
