package battleship;

import java.util.List;

/**
 * Calcula o estado de cada célula dos tabuleiros (próprio e do adversário),
 * independentemente da forma como são desenhados.
 */
public final class BoardState {

	private BoardState() {
		// Classe utilitária: construtor privado
	}

	/**
	 * Constrói o tabuleiro próprio: a minha frota e os tiros que o adversário fez sobre ela.
	 *
	 * @param fleet      a minha frota
	 * @param alienMoves as jogadas do adversário
	 * @return matriz [linha][coluna] com o estado de cada célula
	 */
	public static CellState[][] ownBoard(IFleet fleet, List<IMove> alienMoves) {
		assert fleet != null;
		assert alienMoves != null;

		CellState[][] board = emptyBoard();

		for (IShip ship : fleet.getShips())
			for (IPosition pos : ship.getPositions())
				set(board, pos, CellState.SHIP);

		for (IMove move : alienMoves)
			for (IPosition shot : move.getShots()) {
				if (!shot.isInside())
					continue;
				CellState current = board[shot.getRow()][shot.getColumn()];
				if (current == CellState.WATER)
					set(board, shot, CellState.MISS);
				else if (current == CellState.SHIP)
					set(board, shot, CellState.HIT);
			}

		for (IShip ship : fleet.getSunkShips())
			for (IPosition pos : ship.getPositions())
				set(board, pos, CellState.SUNK);

		return board;
	}

	/**
	 * Constrói o tabuleiro do adversário a partir dos meus tiros e dos respetivos resultados.
	 * Os navios do adversário só aparecem quando são atingidos.
	 *
	 * @param myMoves as minhas jogadas (com resultados)
	 * @return matriz [linha][coluna] com o estado de cada célula
	 */
	public static CellState[][] opponentBoard(List<IMove> myMoves) {
		assert myMoves != null;

		CellState[][] board = emptyBoard();

		for (IMove move : myMoves) {
			List<IPosition> shots = move.getShots();
			List<IGame.ShotResult> results = move.getShotResults();
			for (int i = 0; i < shots.size() && i < results.size(); i++) {
				IPosition shot = shots.get(i);
				IGame.ShotResult result = results.get(i);
				if (!shot.isInside() || !result.valid() || result.repeated())
					continue;

				if (result.ship() == null)
					set(board, shot, CellState.MISS);
				else if (result.sunk())
					for (IPosition pos : result.ship().getPositions())
						set(board, pos, CellState.SUNK);
				else if (board[shot.getRow()][shot.getColumn()] != CellState.SUNK)
					set(board, shot, CellState.HIT);
			}
		}
		return board;
	}

	private static CellState[][] emptyBoard() {
		CellState[][] board = new CellState[Game.BOARD_SIZE][Game.BOARD_SIZE];
		for (CellState[] row : board)
			java.util.Arrays.fill(row, CellState.WATER);
		return board;
	}

	private static void set(CellState[][] board, IPosition pos, CellState state) {
		if (pos.isInside())
			board[pos.getRow()][pos.getColumn()] = state;
	}
}
