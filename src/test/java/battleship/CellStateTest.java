package battleship;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for enum CellState.
 * Author: SENAS
 * Date: 2026-11-20 16:01
 * Cyclomatic Complexity:
 * - constructor CellState(char): 1
 * - getSymbol(): 1
 * - values() (implícito): 1
 * - valueOf(String) (implícito): 2 (nome válido / nome inválido → exceção)
 */
class CellStateTest {

	private CellState state;

	@BeforeEach
	void setUp() {
		state = CellState.WATER;
	}

	@AfterEach
	void tearDown() {
		state = null;
	}

	@Test
	@DisplayName("O construtor associa a cada estado um símbolo e todos os símbolos são distintos")
	void constructor() {
		long distinct = Arrays.stream(CellState.values()).map(CellState::getSymbol).distinct().count();
		assertAll(
				() -> assertNotNull(state, "Error: expected the WATER instance but got null"),
				() -> assertEquals(CellState.values().length, distinct,
						"Error: expected " + CellState.values().length + " distinct symbols but got " + distinct));
	}

	@Test
	@DisplayName("getSymbol devolve o símbolo definido para cada estado")
	void getSymbol() {
		assertAll(
				() -> assertEquals(' ', state.getSymbol(), "Error: expected ' ' for WATER but got '" + state.getSymbol() + "'"),
				() -> assertEquals('o', CellState.MISS.getSymbol(), "Error: expected 'o' for MISS but got '" + CellState.MISS.getSymbol() + "'"),
				() -> assertEquals('#', CellState.SHIP.getSymbol(), "Error: expected '#' for SHIP but got '" + CellState.SHIP.getSymbol() + "'"),
				() -> assertEquals('*', CellState.HIT.getSymbol(), "Error: expected '*' for HIT but got '" + CellState.HIT.getSymbol() + "'"),
				() -> assertEquals('X', CellState.SUNK.getSymbol(), "Error: expected 'X' for SUNK but got '" + CellState.SUNK.getSymbol() + "'"));
	}

	@Test
	@DisplayName("values devolve os 5 estados pela ordem de declaração")
	void values() {
		CellState[] expected = { CellState.WATER, CellState.MISS, CellState.SHIP, CellState.HIT, CellState.SUNK };
		assertArrayEquals(expected, CellState.values(),
				"Error: expected [WATER, MISS, SHIP, HIT, SUNK] but got " + Arrays.toString(CellState.values()));
	}

	@Test
	@DisplayName("valueOf com um nome válido devolve o estado correspondente")
	void valueOf1() {
		assertAll(
				() -> assertSame(state, CellState.valueOf("WATER"), "Error: expected WATER but got another state"),
				() -> assertSame(CellState.SUNK, CellState.valueOf("SUNK"), "Error: expected SUNK but got another state"));
	}

	@Test
	@DisplayName("valueOf com um nome inexistente lança IllegalArgumentException")
	void valueOf2() {
		assertThrows(IllegalArgumentException.class, () -> CellState.valueOf("XPTO"),
				"Error: expected IllegalArgumentException for an unknown name but nothing was thrown");
	}
}
