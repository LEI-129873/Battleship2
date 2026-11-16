package battleship;

import org.junit.jupiter.api.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 * Test class for Move.
 * Author: Martim Barros
 * Date: 2026-11-06
 */
@DisplayName("Tests for Move and processEnemyFire")
class MoveTest {

    private Move move;
    private IPosition pos1, pos2, pos3;

    @BeforeEach
    void setUp() {
        pos1 = new Position(0, 0);
        pos2 = new Position(1, 1);
        pos3 = new Position(2, 2);
    }

    @Test
    @DisplayName("Constructor and getters work correctly")
    void constructorAndGetters() {
        List<IPosition> shots = List.of(pos1, pos2);
        List<IGame.ShotResult> results = List.of(
                new IGame.ShotResult(true, false, null, false),
                new IGame.ShotResult(true, false, null, false)
        );

        move = new Move(1, shots, results);

        assertEquals(1, move.getNumber(), "Move number should be 1");
        assertEquals(2, move.getShots().size(), "Should have 2 shots");
        assertEquals(2, move.getShotResults().size(), "Should have 2 results");
    }

    @Test
    @DisplayName("toString returns a formatted string")
    void testToString() {
        move = new Move(1, List.of(pos1), List.of(new IGame.ShotResult(true, false, null, false)));
        String str = move.toString();
        assertTrue(str.contains("number=1"), "toString should contain the move number");
        assertTrue(str.contains("shots=1"), "toString should contain the number of shots");
    }

    @Test
    @DisplayName("processEnemyFire handles misses, repeats, and invalid shots")
    void processEnemyFireMissesRepeatsInvalid() {
        List<IPosition> shots = List.of(pos1, pos2, pos3);

        // 1 tiro inválido, 1 tiro repetido, 1 tiro na água
        List<IGame.ShotResult> results = List.of(
                new IGame.ShotResult(false, false, null, false), // Inválido
                new IGame.ShotResult(true, true, null, false),   // Repetido
                new IGame.ShotResult(true, false, null, false)   // Na água
        );

        move = new Move(1, shots, results);
        String json = move.processEnemyFire(false); // false = sem prints no terminal

        assertTrue(json.contains("\"validShots\" : 1"), "Should have 1 valid shot");
        assertTrue(json.contains("\"repeatedShots\" : 1"), "Should have 1 repeated shot");
        assertTrue(json.contains("\"missedShots\" : 1"), "Should have 1 missed shot");
        // CORREÇÃO: 3 tiros - 1 válido - 1 repetido = 1 fora
        assertTrue(json.contains("\"outsideShots\" : 1"), "Should have 1 outside shot");
    }

    @Test
    @DisplayName("processEnemyFire handles hits and sunk ships")
    void processEnemyFireHitsAndSunk() {
        IShip barca = new SingleCellShip(Compass.NORTH, new Position(5, 5));
        IShip caravela = new Caravel(Compass.NORTH, new Position(7, 7));

        List<IPosition> shots = List.of(pos1, pos2, pos3);

        // 1 acerto (não afundou), 1 afundou, 1 inválido
        List<IGame.ShotResult> results = List.of(
                new IGame.ShotResult(true, false, barca, false),      // Acertou na Barca
                new IGame.ShotResult(true, false, caravela, true),    // Afundou a Caravela
                new IGame.ShotResult(false, false, null, false)       // Tiro fora do tabuleiro
        );

        move = new Move(1, shots, results);
        String json = move.processEnemyFire(false);

        assertTrue(json.contains("\"validShots\" : 2"), "Should have 2 valid shots");
        assertTrue(json.contains("\"outsideShots\" : 1"), "Should have 1 outside shot");
        assertTrue(json.contains("Barca"), "Should contain Barca in hits");
        assertTrue(json.contains("Caravela"), "Should contain Caravela in sunk boats");
    }

    @Test
    @DisplayName("processEnemyFire with verbose=true prints to console")
    void processEnemyFireVerbose() {
        List<IPosition> shots = List.of(pos1, pos2, pos3);
        List<IGame.ShotResult> results = List.of(
                new IGame.ShotResult(true, true, null, false),
                new IGame.ShotResult(true, true, null, false),
                new IGame.ShotResult(true, true, null, false)
        );

        move = new Move(1, shots, results);
        String json = move.processEnemyFire(true);

        assertNotNull(json, "JSON should not be null even in verbose mode");
        assertTrue(json.contains("\"repeatedShots\" : 3"), "All 3 shots should be repeated");
    }

    // --- NOVOS TESTES PARA AUMENTAR O COVERAGE ---

    @Test
    @DisplayName("processEnemyFire formats string correctly when there are NO misses")
    void processEnemyFireNoMisses() {
        IShip barca = new SingleCellShip(Compass.NORTH, new Position(5, 5));
        IShip caravela = new Caravel(Compass.NORTH, new Position(7, 7));

        List<IPosition> shots = List.of(pos1, pos2, pos3);

        // 1 Barca afundada, 1 Caravela atingida (mas não afundada), 1 Barca atingida (repetida no tipo)
        // Sem tiros na água
        List<IGame.ShotResult> results = List.of(
                new IGame.ShotResult(true, false, barca, true),       // Afundou Barca
                new IGame.ShotResult(true, false, caravela, false),   // Atingiu Caravela
                new IGame.ShotResult(true, false, barca, false)       // Atingiu outra Barca
        );

        move = new Move(1, shots, results);
        String json = move.processEnemyFire(false);

        assertTrue(json.contains("\"missedShots\" : 0"), "Should have 0 missed shots");
        assertTrue(json.contains("\"validShots\" : 3"), "Should have 3 valid shots");
    }

    @Test
    @DisplayName("processEnemyFire handles multiple sunk ships of the same type")
    void processEnemyFireMultipleSameType() {
        IShip barca1 = new SingleCellShip(Compass.NORTH, new Position(1, 1));
        IShip barca2 = new SingleCellShip(Compass.NORTH, new Position(2, 2));

        List<IPosition> shots = List.of(pos1, pos2, pos3);

        // 2 Barcas afundadas
        List<IGame.ShotResult> results = List.of(
                new IGame.ShotResult(true, false, barca1, true),
                new IGame.ShotResult(true, false, barca2, true),
                new IGame.ShotResult(true, false, null, false) // Miss
        );

        move = new Move(1, shots, results);
        String json = move.processEnemyFire(false);

        assertTrue(json.contains("\"validShots\" : 3"), "Should have 3 valid shots");
        assertTrue(json.contains("\"missedShots\" : 1"), "Should have 1 missed shot");
        assertTrue(json.contains("Barca"), "Should handle multiple sunk Barges");
    }

    @Test
    @DisplayName("processEnemyFire with verbose=true covers string building paths")
    void processEnemyFireVerboseComplex() {
        IShip barca = new SingleCellShip(Compass.NORTH, new Position(5, 5));
        IShip caravela = new Caravel(Compass.NORTH, new Position(7, 7));

        List<IPosition> shots = List.of(pos1, pos2, pos3);

        // Mistura: 1 atingido, 1 afundado, 1 repetido, 1 inválido, 1 na água
        // Como só podemos passar 3 tiros, vamos simular os resultados diretamente
        List<IGame.ShotResult> results = List.of(
                new IGame.ShotResult(true, false, barca, false),      // Atingiu Barca
                new IGame.ShotResult(true, true, null, false),        // Repetido
                new IGame.ShotResult(false, false, null, false)       // Fora do tabuleiro
        );

        move = new Move(1, shots, results);
        String json = move.processEnemyFire(true); // Força o verbose!

        assertNotNull(json);
    }

    @Test
    @DisplayName("processEnemyFire verbose with only invalid shots")
    void processEnemyFireVerboseAllInvalid() {
        List<IPosition> shots = List.of(pos1, pos2, pos3);
        List<IGame.ShotResult> results = List.of(
                new IGame.ShotResult(false, false, null, false),
                new IGame.ShotResult(false, false, null, false),
                new IGame.ShotResult(false, false, null, false)
        );

        move = new Move(1, shots, results);
        String json = move.processEnemyFire(true);

        assertTrue(json.contains("\"outsideShots\" : 3"));
    }

    @Test
    @DisplayName("processEnemyFire verbose with sunk and hits but no misses")
    void processEnemyFireVerboseNoMisses() {
        IShip barca = new SingleCellShip(Compass.NORTH, new Position(5, 5));
        IShip caravela = new Caravel(Compass.NORTH, new Position(7, 7));
        IShip nau = new Carrack(Compass.NORTH, new Position(9, 9));

        List<IPosition> shots = List.of(pos1, pos2, pos3);
        List<IGame.ShotResult> results = List.of(
                new IGame.ShotResult(true, false, barca, true),      // Afundou Barca
                new IGame.ShotResult(true, false, caravela, false),  // Atingiu Caravela
                new IGame.ShotResult(true, false, nau, false)        // Atingiu Nau
        );

        move = new Move(1, shots, results);
        String json = move.processEnemyFire(true);

        assertTrue(json.contains("\"missedShots\" : 0"));
    }

    private String captureVerboseOutput(Move m) {
        PrintStream original = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buffer, true, StandardCharsets.UTF_8));
        try {
            m.processEnemyFire(true);
        } finally {
            System.setOut(original);
        }
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("Verbose: single repeated shot without valid shots uses singular forms")
    void verboseSingleRepeatedShot() {
        move = new Move(1, List.of(pos1, pos2, pos3), List.of(
                new IGame.ShotResult(true, true, null, false),
                new IGame.ShotResult(false, false, null, false),
                new IGame.ShotResult(false, false, null, false)
        ));

        String out = captureVerboseOutput(move);

        assertTrue(out.contains("1 tiro repetido"), "Should use singular for repeated shot");
        assertTrue(out.contains("2 tiros exteriores"), "Should use plural for outside shots");
    }

    @Test
    @DisplayName("Verbose: single miss uses singular and several misses use plural")
    void verboseMissesSingularAndPlural() {
        move = new Move(1, List.of(pos1), List.of(
                new IGame.ShotResult(true, false, null, false)
        ));
        assertTrue(captureVerboseOutput(move).contains("1 tiro na "), "Singular miss");

        move = new Move(2, List.of(pos1, pos2), List.of(
                new IGame.ShotResult(true, false, null, false),
                new IGame.ShotResult(true, false, null, false)
        ));
        assertTrue(captureVerboseOutput(move).contains("2 tiros na "), "Plural misses");
    }

    @Test
    @DisplayName("Verbose: single hit on a ship that did not sink uses singular")
    void verboseSingleHitNoMisses() {
        IShip barca = new SingleCellShip(Compass.NORTH, new Position(5, 5));
        move = new Move(1, List.of(pos1), List.of(
                new IGame.ShotResult(true, false, barca, false)
        ));

        String out = captureVerboseOutput(move);

        assertTrue(out.contains("1 tiro num(a) Barca"), "Should use singular for one hit");
        assertFalse(out.contains("+"), "Trailing '+' should have been removed");
    }

    @Test
    @DisplayName("Verbose: several hits on a ship that did not sink use plural")
    void verboseMultipleHitsSameShip() {
        IShip caravela = new Caravel(Compass.NORTH, new Position(7, 7));
        move = new Move(1, List.of(pos1, pos2), List.of(
                new IGame.ShotResult(true, false, caravela, false),
                new IGame.ShotResult(true, false, caravela, false)
        ));

        String out = captureVerboseOutput(move);

        assertTrue(out.contains("2 tiros num(a) Caravela"), "Should use plural for several hits");
    }

    @Test
    @DisplayName("Verbose: single sunk ship uses singular")
    void verboseSingleSunkShip() {
        IShip barca = new SingleCellShip(Compass.NORTH, new Position(5, 5));
        move = new Move(1, List.of(pos1), List.of(
                new IGame.ShotResult(true, false, barca, true)
        ));

        String out = captureVerboseOutput(move);

        assertTrue(out.contains("1 Barca ao fundo"), "Should use singular for one sunk ship");
        assertFalse(out.contains("Barcas"), "Should not use plural");
    }

    @Test
    @DisplayName("Verbose: several sunk ships of the same type use plural")
    void verboseMultipleSunkSameType() {
        IShip barca1 = new SingleCellShip(Compass.NORTH, new Position(1, 1));
        IShip barca2 = new SingleCellShip(Compass.NORTH, new Position(3, 3));
        move = new Move(1, List.of(pos1, pos2, pos3), List.of(
                new IGame.ShotResult(true, false, barca1, true),
                new IGame.ShotResult(true, false, barca2, true),
                new IGame.ShotResult(true, false, null, false)
        ));

        String out = captureVerboseOutput(move);

        assertTrue(out.contains("2 Barcas ao fundo"), "Should use plural for two sunk ships");
    }

    @Test
    @DisplayName("Verbose: valid shot plus several repeated shots uses plural for repeated")
    void verboseValidAndMultipleRepeated() {
        move = new Move(1, List.of(pos1, pos2, pos3), List.of(
                new IGame.ShotResult(true, false, null, false),
                new IGame.ShotResult(true, true, null, false),
                new IGame.ShotResult(true, true, null, false)
        ));

        String out = captureVerboseOutput(move);

        assertTrue(out.contains("2 tiros repetidos"), "Should use plural for repeated shots");
    }
}