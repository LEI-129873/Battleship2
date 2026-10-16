package battleship;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;

/**
 * Janela com a representação gráfica dos dois tabuleiros:
 * o próprio (a minha frota + tiros do adversário) e o do adversário (os meus tiros).
 */
public class GameWindow {

	private static GameWindow instance;

	private final JFrame frame;
	private final BoardPanel ownBoard;
	private final BoardPanel opponentBoard;
	private final JLabel ownTitle;
	private final JLabel opponentTitle;
	private final JPanel legend;

	private GameWindow() {
		frame = new JFrame("Battleship");
		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

		ownBoard = new BoardPanel();
		opponentBoard = new BoardPanel();
		ownTitle = title();
		opponentTitle = title();

		JPanel boards = new JPanel(new GridLayout(1, 2, 32, 0));
		boards.add(boardWithTitle(ownTitle, ownBoard));
		boards.add(boardWithTitle(opponentTitle, opponentBoard));

		legend = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 4));

		JPanel content = new JPanel(new BorderLayout(0, 12));
		content.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
		content.add(boards, BorderLayout.CENTER);
		content.add(legend, BorderLayout.SOUTH);

		frame.setContentPane(content);
		updateTexts();
		frame.pack();
		frame.setLocationByPlatform(true);
	}

	/**
	 * Mostra (ou atualiza) a janela com o estado atual do jogo.
	 * Em ambientes sem ecrã (headless) não faz nada.
	 *
	 * @param game o jogo a representar
	 */
	public static void show(IGame game) {
		if (game == null || GraphicsEnvironment.isHeadless())
			return;

		CellState[][] own = BoardState.ownBoard(game.getMyFleet(), game.getAlienMoves());
		CellState[][] opponent = BoardState.opponentBoard(game.getMyMoves());

		SwingUtilities.invokeLater(() -> {
			if (instance == null)
				instance = new GameWindow();
			instance.updateTexts();
			instance.ownBoard.setCells(own);
			instance.opponentBoard.setCells(opponent);
			if (!instance.frame.isVisible())
				instance.frame.setVisible(true);
		});
	}

	/**
	 * Fecha a janela, se estiver aberta.
	 */
	public static void close() {
		SwingUtilities.invokeLater(() -> {
			if (instance != null) {
				instance.frame.dispose();
				instance = null;
			}
		});
	}

	private void updateTexts() {
		frame.setTitle(I18n.get("gui.title"));
		ownTitle.setText(I18n.get("gui.board.own"));
		opponentTitle.setText(I18n.get("gui.board.opponent"));

		legend.removeAll();
		legend.add(legendItem(CellState.WATER, "gui.legend.water"));
		legend.add(legendItem(CellState.MISS, "gui.legend.miss"));
		legend.add(legendItem(CellState.SHIP, "gui.legend.ship"));
		legend.add(legendItem(CellState.HIT, "gui.legend.hit"));
		legend.add(legendItem(CellState.SUNK, "gui.legend.sunk"));
		legend.revalidate();
		legend.repaint();
	}

	private static JLabel title() {
		JLabel label = new JLabel("", SwingConstants.CENTER);
		label.setFont(label.getFont().deriveFont(Font.BOLD, 16f));
		return label;
	}

	private static JPanel boardWithTitle(JLabel title, BoardPanel board) {
		JPanel panel = new JPanel(new BorderLayout(0, 6));
		panel.add(title, BorderLayout.NORTH);
		panel.add(board, BorderLayout.CENTER);
		return panel;
	}

	private static JLabel legendItem(CellState state, String key) {
		JLabel label = new JLabel(I18n.get(key), new CellIcon(state), SwingConstants.LEFT);
		label.setIconTextGap(6);
		return label;
	}

	/** Ícone da legenda com o mesmo desenho das células do tabuleiro. */
	private record CellIcon(CellState state) implements Icon {
		private static final int SIZE = 18;

		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
			BoardPanel.drawCell((Graphics2D) g, state, x, y, SIZE);
			g.setColor(java.awt.Color.DARK_GRAY);
			g.drawRect(x, y, SIZE - 1, SIZE - 1);
		}

		@Override
		public int getIconWidth() {
			return SIZE;
		}

		@Override
		public int getIconHeight() {
			return SIZE;
		}
	}
}
