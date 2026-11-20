package battleship;

import java.util.List;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for BoardState.
 */
public class BoardStateTest {

	private IFleet fleet;
	private Game game;

	@BeforeEach
	void setUp() {
		fleet = TestFixtures.emptyFleet();
		fleet.addShip(new Caravel(Compass.EAST, new Position(0, 0)));  // A1, A2
		fleet.addShip(new Barge(Compass.NORTH, new Position(5, 5)));   // F6
		game = new Game(fleet);
	}

	@Test
	void emptyBoardsAreWater() {
		CellState[][] own = BoardState.ownBoard(TestFixtures.emptyFleet(), List.of());
		CellState[][] opponent = BoardState.opponentBoard(List.of());
		assertEquals(Game.BOARD_SIZE, own.length);
		assertEquals(Game.BOARD_SIZE, opponent[0].length);
		for (int r = 0; r < Game.BOARD_SIZE; r++)
			for (int c = 0; c < Game.BOARD_SIZE; c++) {
				assertEquals(CellState.WATER, own[r][c]);
				assertEquals(CellState.WATER, opponent[r][c]);
			}
	}

	@Test
	void ownBoardShowsShipsBeforeAnyShot() {
		CellState[][] own = BoardState.ownBoard(fleet, game.getAlienMoves());
		assertEquals(CellState.SHIP, own[0][0]);
		assertEquals(CellState.SHIP, own[0][1]);
		assertEquals(CellState.SHIP, own[5][5]);
		assertEquals(CellState.WATER, own[9][9]);
	}

	@Test
	void ownBoardShowsMissHitAndSunk() {
		game.fireShots(List.of(new Position(0, 0), new Position(5, 5), new Position(9, 9)));
		CellState[][] own = BoardState.ownBoard(fleet, game.getAlienMoves());

		assertEquals(CellState.HIT, own[0][0], "Caravela atingida mas a flutuar");
		assertEquals(CellState.SHIP, own[0][1], "Parte da caravela por atingir");
		assertEquals(CellState.SUNK, own[5][5], "Barca afundada");
		assertEquals(CellState.MISS, own[9][9], "Tiro na água");

		game.fireShots(List.of(new Position(0, 1), new Position(8, 8), new Position(-1, 0)));
		own = BoardState.ownBoard(fleet, game.getAlienMoves());
		assertEquals(CellState.SUNK, own[0][0]);
		assertEquals(CellState.SUNK, own[0][1]);
		assertEquals(CellState.MISS, own[8][8]);
	}

	@Test
	void opponentBoardHidesShipsAndShowsShotResults() {
		IShip caravel = new Caravel(Compass.EAST, new Position(3, 3)); // D4, D5
		game.recordMyMove(
				List.of(new Position(3, 3), new Position(7, 7), new Position(-1, 2)),
				List.of(new IGame.ShotResult(true, false, caravel, false),
						new IGame.ShotResult(true, false, null, false),
						new IGame.ShotResult(false, false, null, false)));

		CellState[][] opponent = BoardState.opponentBoard(game.getMyMoves());
		assertEquals(CellState.HIT, opponent[3][3]);
		assertEquals(CellState.WATER, opponent[3][4], "Navio do adversário não atingido fica oculto");
		assertEquals(CellState.MISS, opponent[7][7]);

		game.recordMyMove(
				List.of(new Position(3, 4), new Position(3, 3), new Position(0, 0)),
				List.of(new IGame.ShotResult(true, false, caravel, true),
						new IGame.ShotResult(true, true, null, false),
						new IGame.ShotResult(true, false, null, false)));

		opponent = BoardState.opponentBoard(game.getMyMoves());
		assertEquals(CellState.SUNK, opponent[3][3]);
		assertEquals(CellState.SUNK, opponent[3][4]);
		assertEquals(CellState.MISS, opponent[0][0]);
	}

	@Test
	void eachStateHasDistinctSymbol() {
		long distinct = java.util.Arrays.stream(CellState.values()).map(CellState::getSymbol).distinct().count();
		assertEquals(CellState.values().length, distinct);
	}

	@Test
	@DisplayName("ownBoard ignora tiros do adversário fora do tabuleiro e não altera células já atingidas")
	void ownBoardIgnoresOutsideAlienShots() {
		List<IMove> alienMoves = List.of(new Move(1,
				List.of(new Position(-1, 0), new Position(9, 9), new Position(9, 9)),
				List.of(new IGame.ShotResult(false, false, null, false),
						new IGame.ShotResult(true, false, null, false),
						new IGame.ShotResult(true, true, null, false))));
		CellState[][] own = BoardState.ownBoard(fleet, alienMoves);
		assertAll(
				() -> assertEquals(CellState.MISS, own[9][9], "Error: expected MISS at J10 but got " + own[9][9]),
				() -> assertEquals(CellState.SHIP, own[0][0], "Error: expected SHIP at A1 but got " + own[0][0]));
	}

	@Test
	@DisplayName("opponentBoard ignora tiros sem resultado, inválidos dentro do tabuleiro e não reescreve um afundado")
	void opponentBoardEdgeCases() {
		IShip caravel = new Caravel(Compass.EAST, new Position(3, 3)); // D4, D5
		List<IMove> myMoves = List.of(
				new Move(1, List.of(new Position(3, 3), new Position(2, 2)),
						List.of(new IGame.ShotResult(true, false, caravel, true))),
				new Move(2, List.of(new Position(6, 6), new Position(3, 4)),
						List.of(new IGame.ShotResult(false, false, null, false),
								new IGame.ShotResult(true, false, caravel, false))));
		CellState[][] opponent = BoardState.opponentBoard(myMoves);
		assertAll(
				() -> assertEquals(CellState.WATER, opponent[2][2], "Error: expected WATER for a shot without result but got " + opponent[2][2]),
				() -> assertEquals(CellState.WATER, opponent[6][6], "Error: expected WATER for an invalid shot but got " + opponent[6][6]),
				() -> assertEquals(CellState.SUNK, opponent[3][4], "Error: expected SUNK to be kept at D5 but got " + opponent[3][4]));
	}

	@Test
	@DisplayName("opponentBoard ignora posições de um navio afundado que caiam fora do tabuleiro")
	void opponentBoardSunkShipPartlyOutside() {
		IShip caravel = new Caravel(Compass.EAST, new Position(0, 9)); // A10 e uma posição fora
		List<IMove> myMoves = List.of(new Move(1, List.of(new Position(0, 9)),
				List.of(new IGame.ShotResult(true, false, caravel, true))));
		CellState[][] opponent = BoardState.opponentBoard(myMoves);
		assertEquals(CellState.SUNK, opponent[0][9], "Error: expected SUNK at A10 but got " + opponent[0][9]);
	}

	@Test
	@DisplayName("ownBoard e opponentBoard rejeitam argumentos nulos (AssertionError)")
	void nullArgumentsAreRejected() {
		assertAll(
				() -> assertThrows(AssertionError.class, () -> BoardState.ownBoard(null, List.of()),
						"Error: expected AssertionError for a null fleet"),
				() -> assertThrows(AssertionError.class, () -> BoardState.ownBoard(fleet, null),
						"Error: expected AssertionError for null alien moves"),
				() -> assertThrows(AssertionError.class, () -> BoardState.opponentBoard(null),
						"Error: expected AssertionError for null moves"));
	}
}
