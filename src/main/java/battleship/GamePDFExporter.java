package battleship;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.IOException;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class GamePDFExporter {
    private static final float MARGIN = 50;
    private static final float LINE_HEIGHT = 16;

    private GamePDFExporter() {}

    public static void generatePDF(List<IMove> myPlays, List<IMove> alienMoves) throws IOException {
        generatePDF(myPlays, alienMoves, Path.of("historico-partida.pdf"));
    }

    public static void generatePDF(List<IMove> myPlays, List<IMove> alienMoves, Path destination)
            throws IOException {
        Objects.requireNonNull(myPlays, "myPlays cannot be null");
        Objects.requireNonNull(alienMoves, "alienMoves cannot be null");
        Objects.requireNonNull(destination, "destination cannot be null");

        List<String> lines = new ArrayList<>();
        lines.add("BATTLESHIP - GAME HISTORY");
        lines.add("");
        appendMoves(lines, "PLAYER MOVES", myPlays);
        lines.add("");
        appendMoves(lines, "OPPONENT MOVES", alienMoves);

        try (PDDocument document = new PDDocument()) {
            PDType1Font font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            int linesPerPage = (int) ((PDRectangle.A4.getHeight() - 2 * MARGIN) / LINE_HEIGHT);

            for (int start = 0; start < lines.size(); start += linesPerPage) {
                PDPage page = new PDPage(PDRectangle.A4);
                document.addPage(page);

                try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                    content.beginText();
                    content.setFont(font, 11);
                    content.newLineAtOffset(MARGIN, page.getMediaBox().getHeight() - MARGIN);

                    int end = Math.min(start + linesPerPage, lines.size());
                    for (int i = start; i < end; i++) {
                        content.showText(toPdfText(lines.get(i)));
                        content.newLineAtOffset(0, -LINE_HEIGHT);
                    }
                    content.endText();
                }
            }

            document.save(destination.toFile());
        }
    }

    // List com todas as informações que estão no PDF
    private static void appendMoves(List<String> lines, String heading, List<IMove> moves) {
        lines.add(heading);
        if (moves.isEmpty()) {
            lines.add("No moves recorded.");
            return;
        }

        for (IMove move : moves) {
            lines.add("Move " + move.getNumber() + ":");
            List<IPosition> shots = move.getShots();
            List<IGame.ShotResult> results = move.getShotResults();

            for (int i = 0; i < shots.size(); i++) {
                IPosition shot = shots.get(i);
                String position = shot.getClassicRow() + String.valueOf(shot.getClassicColumn());
                String result = i < results.size() ? describeResult(results.get(i)) : "RESULT UNAVAILABLE";
                lines.add("  " + position + " - " + result);
            }
        }
    }

    // Categorizar o tipo de jogada
    private static String describeResult(IGame.ShotResult result) {
        if (!result.valid()) {
            return "INVALID";
        }
        if (result.repeated()) {
            return "REPEATED";
        }
        if (result.ship() == null) {
            return "MISS";
        }
        String ship = " (" + result.ship().getCategory() + ")";
        return result.sunk() ? "SUNK" + ship : "HIT" + ship;
    }

    private static String toPdfText(String text) {
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return normalized.replaceAll("[^\\x20-\\x7E]", "?");
    }
}
