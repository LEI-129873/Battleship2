package battleship;

public record Jogada(int x, int y, int ronda, String jogador, String navio, boolean acertou, boolean afundou,
                     int pontos, long tempo) {
}