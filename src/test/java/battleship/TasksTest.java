package battleship;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.InputMismatchException;
import java.util.NoSuchElementException;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class TasksTest {

	// ====================== readClassicPosition ======================

	@Test
	@DisplayName("readClassicPosition aceita formato junto (A3)")
	void readClassicPositionJoined() {
		IPosition p = Tasks.readClassicPosition(new Scanner("A3"));
		assertEquals('A', p.getClassicRow());
		assertEquals(3, p.getClassicColumn());
	}

	@Test
	@DisplayName("readClassicPosition aceita minúsculas (a3)")
	void readClassicPositionLowercase() {
		IPosition p = Tasks.readClassicPosition(new Scanner("a3"));
		assertEquals('A', p.getClassicRow());
		assertEquals(3, p.getClassicColumn());
	}

	@Test
	@DisplayName("readClassicPosition aceita formato separado (A 3)")
	void readClassicPositionSeparated() {
		IPosition p = Tasks.readClassicPosition(new Scanner("A 3"));
		assertEquals('A', p.getClassicRow());
		assertEquals(3, p.getClassicColumn());
	}

	@Test
	@DisplayName("readClassicPosition aceita o canto do tabuleiro (J10)")
	void readClassicPositionCorner() {
		IPosition p = Tasks.readClassicPosition(new Scanner("J10"));
		assertEquals('J', p.getClassicRow());
		assertEquals(10, p.getClassicColumn());
	}

	@Test
	@DisplayName("readClassicPosition lança exceção sem input")
	void readClassicPositionEmptyInput() {
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> Tasks.readClassicPosition(new Scanner("")));
		assertTrue(ex.getMessage().contains("Nenhuma posição"));
	}

	@Test
	@DisplayName("readClassicPosition lança exceção com formato inválido (13)")
	void readClassicPositionInvalidFormat() {
		assertThrows(IllegalArgumentException.class,
				() -> Tasks.readClassicPosition(new Scanner("13")));
	}

	@Test
	@DisplayName("readClassicPosition lança exceção com letra dupla (AB 3)")
	void readClassicPositionDoubleLetter() {
		assertThrows(IllegalArgumentException.class,
				() -> Tasks.readClassicPosition(new Scanner("AB 3")));
	}

	@Test
	@DisplayName("readClassicPosition lança exceção com número negativo (A -3)")
	void readClassicPositionNegativeNumber() {
		assertThrows(IllegalArgumentException.class,
				() -> Tasks.readClassicPosition(new Scanner("A -3")));
	}

	// ====================== readPosition ======================

	@Test
	@DisplayName("readPosition lê linha e coluna numéricas")
	void readPositionReadsRowAndColumn() {
		Position p = Tasks.readPosition(new Scanner("4 7"));
		assertEquals(4, p.getRow());
		assertEquals(7, p.getColumn());
	}

	@Test
	@DisplayName("readPosition aceita o canto (9 9)")
	void readPositionCorner() {
		Position p = Tasks.readPosition(new Scanner("9 9"));
		assertEquals(9, p.getRow());
		assertEquals(9, p.getColumn());
	}

	@Test
	@DisplayName("readPosition lança InputMismatchException com texto")
	void readPositionInvalidToken() {
		assertThrows(InputMismatchException.class,
				() -> Tasks.readPosition(new Scanner("x y")));
	}

	@Test
	@DisplayName("readPosition lança NoSuchElementException sem input")
	void readPositionEmptyInput() {
		assertThrows(NoSuchElementException.class,
				() -> Tasks.readPosition(new Scanner("")));
	}

	// ====================== readShip ======================

	@Test
	@DisplayName("readShip constrói uma Barca")
	void readShipBuildsBarge() {
		Ship s = Tasks.readShip(new Scanner("barca 0 0 n"));
		assertNotNull(s);
		assertEquals("Barca", s.getCategory());
		assertEquals(1, s.getSize());
		assertEquals(Compass.NORTH, s.getBearing());
	}

	@Test
	@DisplayName("readShip constrói uma Caravela a sul")
	void readShipBuildsCaravel() {
		Ship s = Tasks.readShip(new Scanner("caravela 2 0 s"));
		assertNotNull(s);
		assertEquals("Caravela", s.getCategory());
		assertEquals(2, s.getSize());
		assertEquals(Compass.SOUTH, s.getBearing());
	}

	@Test
	@DisplayName("readShip constrói uma Nau")
	void readShipBuildsCarrack() {
		Ship s = Tasks.readShip(new Scanner("nau 0 5 e"));
		assertNotNull(s);
		assertEquals("Nau", s.getCategory());
		assertEquals(3, s.getSize());
	}

	@Test
	@DisplayName("readShip constrói uma Fragata")
	void readShipBuildsFrigate() {
		Ship s = Tasks.readShip(new Scanner("fragata 0 0 e"));
		assertNotNull(s);
		assertEquals("Fragata", s.getCategory());
		assertEquals(4, s.getSize());
	}

	@Test
	@DisplayName("readShip constrói um Galeão")
	void readShipBuildsGalleon() {
		Ship s = Tasks.readShip(new Scanner("galeao 5 5 n"));
		assertNotNull(s);
		assertEquals("Galeao", s.getCategory());
		assertEquals(5, s.getSize());
	}

	@Test
	@DisplayName("readShip devolve null para tipo desconhecido")
	void readShipUnknownKind() {
		assertNull(Tasks.readShip(new Scanner("submarino 0 0 n")));
	}

	@Test
	@DisplayName("readShip com orientação inválida viola o assert de buildShip")
	void readShipInvalidBearing() {
		assertThrows(AssertionError.class,
				() -> Tasks.readShip(new Scanner("barca 0 0 x")));
	}

	// ====================== buildFleet ======================

	@Test
	@DisplayName("buildFleet completa a frota, ignorando navio desconhecido e navio encostado")
	void buildFleetSkipsInvalidShipsAndCompletes() {
		// fragata primeiro; a barca (1,0) toca-lhe -> addShip falha;
		// "submarino" -> readShip devolve null; depois os 10 navios válidos.
		String input = """
                fragata 0 0 e
                barca 1 0 n
                submarino 1 9 n
                nau 0 5 e
                nau 9 0 e
                caravela 9 4 e
                caravela 9 7 e
                caravela 2 0 s
                galeao 5 5 n
                barca 2 9 n
                barca 4 9 n
                barca 6 9 n
                barca 7 0 n
                """;
		Fleet fleet = Tasks.buildFleet(new Scanner(input));
		assertNotNull(fleet);
		assertEquals(Fleet.FLEET_SIZE, fleet.getShips().size());
		assertEquals(4, fleet.getShipsLike("Barca").size());
		assertEquals(3, fleet.getShipsLike("Caravela").size());
		assertEquals(2, fleet.getShipsLike("Nau").size());
		assertEquals(1, fleet.getShipsLike("Fragata").size());
		assertEquals(1, fleet.getShipsLike("Galeao").size());
	}

	// ====================== menuHelp ======================

	@Test
	@DisplayName("menuHelp imprime a ajuda sem lançar exceção")
	void menuHelpPrintsHelp() {
		PrintStream original = System.out;
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		try {
			System.setOut(new PrintStream(buffer));
			assertDoesNotThrow(Tasks::menuHelp);
		} finally {
			System.setOut(original);
		}
		assertFalse(buffer.toString().isEmpty());
	}
}