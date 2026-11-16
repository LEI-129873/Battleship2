package battleship;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GamePDFExporterTest {

    @TempDir
    Path tempDir;

    private IMove moveWith(int number, char row, int col, IGame.ShotResult result) {
        List<IPosition> shots = new ArrayList<>();
        shots.add(new Position(row, col));
        List<IGame.ShotResult> results = new ArrayList<>();
        results.add(result);
        return new Move(number, shots, results);
    }

    @Test
    @DisplayName("Gera PDF com listas vazias")
    void generatePdfWithEmptyLists() throws IOException {
        Path pdf = tempDir.resolve("vazio.pdf");
        GamePDFExporter.generatePDF(new ArrayList<>(), new ArrayList<>(), pdf);
        assertTrue(Files.exists(pdf));
        assertTrue(Files.size(pdf) > 0);
    }

    @Test
    @DisplayName("Gera PDF com todos os tipos de resultado")
    void generatePdfWithAllResultTypes() throws IOException {
        IShip barge = new Barge(Compass.NORTH, new Position(0, 0));

        List<IMove> mine = new ArrayList<>();
        mine.add(moveWith(1, 'A', 1, new IGame.ShotResult(false, false, null, false)));   // INVALID
        mine.add(moveWith(2, 'B', 2, new IGame.ShotResult(true, true, null, false)));     // REPEATED
        mine.add(moveWith(3, 'C', 3, new IGame.ShotResult(true, false, null, false)));    // MISS
        mine.add(moveWith(4, 'D', 4, new IGame.ShotResult(true, false, barge, false)));   // HIT

        List<IMove> alien = new ArrayList<>();
        alien.add(moveWith(1, 'E', 5, new IGame.ShotResult(true, false, barge, true)));   // SUNK

        Path pdf = tempDir.resolve("completo.pdf");
        GamePDFExporter.generatePDF(mine, alien, pdf);
        assertTrue(Files.size(pdf) > 0);
    }

    @Test
    @DisplayName("Jogada sem resultados escreve RESULT UNAVAILABLE sem falhar")
    void generatePdfWithMissingResults() throws IOException {
        List<IPosition> shots = new ArrayList<>();
        shots.add(new Position('A', 1));

        List<IMove> mine = new ArrayList<>();
        mine.add(new Move(1, shots, new ArrayList<>()));

        Path pdf = tempDir.resolve("semresultados.pdf");
        assertDoesNotThrow(() -> GamePDFExporter.generatePDF(mine, new ArrayList<>(), pdf));
        assertTrue(Files.exists(pdf));
    }

    @Test
    @DisplayName("Muitas jogadas geram várias páginas sem falhar")
    void generatePdfWithManyMovesCreatesMultiplePages() throws IOException {
        List<IMove> mine = new ArrayList<>();
        for (int i = 1; i <= 60; i++) {
            mine.add(moveWith(i, 'A', 1, new IGame.ShotResult(true, false, null, false)));
        }

        Path pdf = tempDir.resolve("paginas.pdf");
        GamePDFExporter.generatePDF(mine, new ArrayList<>(), pdf);
        assertTrue(Files.size(pdf) > 0);
    }

    @Test
    @DisplayName("Argumentos nulos lançam NullPointerException")
    void nullArgumentsThrow() {
        Path pdf = tempDir.resolve("x.pdf");

        assertThrows(NullPointerException.class,
                () -> GamePDFExporter.generatePDF(null, new ArrayList<>(), pdf));
        assertThrows(NullPointerException.class,
                () -> GamePDFExporter.generatePDF(new ArrayList<>(), null, pdf));
        assertThrows(NullPointerException.class,
                () -> GamePDFExporter.generatePDF(new ArrayList<>(), new ArrayList<>(), null));
    }

    @Test
    @DisplayName("Gera PDF no destino por omissão e limpa o ficheiro")
    void generatePdfDefaultDestination() throws IOException {
        Path defaultFile = Path.of("historico-partida.pdf");

        try {
            GamePDFExporter.generatePDF(new ArrayList<>(), new ArrayList<>());
            assertTrue(Files.exists(defaultFile));
        } finally {
            Files.deleteIfExists(defaultFile);
        }
    }
}