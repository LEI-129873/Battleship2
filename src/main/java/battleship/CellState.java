package battleship;

/**
 * Estado visual de uma célula de um tabuleiro.
 * Cada estado tem um símbolo próprio, para que seja distinguível mesmo sem cor.
 */
public enum CellState {
	/** Água por atingir (ou célula do adversário ainda desconhecida). */
	WATER(' '),
	/** Água atingida: tiro sem efeito. */
	MISS('o'),
	/** Navio intacto (apenas visível no tabuleiro próprio). */
	SHIP('#'),
	/** Navio atingido, mas ainda a flutuar. */
	HIT('*'),
	/** Navio afundado. */
	SUNK('X');

	private final char symbol;

	CellState(char symbol) {
		this.symbol = symbol;
	}

	/**
	 * @return o símbolo usado para representar este estado
	 */
	public char getSymbol() {
		return symbol;
	}
}
