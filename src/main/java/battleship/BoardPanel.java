package battleship;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Painel Swing que desenha um tabuleiro como uma grelha gráfica,
 * com as coordenadas visíveis (letras nas linhas A-J, números nas colunas 1-10,
 * tal como se introduzem as posições, p.ex. "A3").
 */
public class BoardPanel extends JPanel {

	static final int CELL_SIZE = 36;
	private static final int MARGIN = 28;

	static final Color WATER_COLOR = new Color(0x4F, 0x9D, 0xDE);
	static final Color MISS_COLOR = new Color(0xB8, 0xD8, 0xF0);
	static final Color SHIP_COLOR = new Color(0x6B, 0x6B, 0x6B);
	static final Color HIT_COLOR = new Color(0xE8, 0x7A, 0x1E);
	static final Color SUNK_COLOR = new Color(0x9E, 0x1B, 0x1B);
	private static final Color GRID_COLOR = new Color(0x1E, 0x3A, 0x5F);

	private CellState[][] cells;

	public BoardPanel() {
		this.cells = new CellState[Game.BOARD_SIZE][Game.BOARD_SIZE];
		for (CellState[] row : cells)
			java.util.Arrays.fill(row, CellState.WATER);
		int side = MARGIN + Game.BOARD_SIZE * CELL_SIZE + 1;
		setPreferredSize(new Dimension(side, side));
		setOpaque(false);
	}

	/**
	 * Atualiza o estado das células e redesenha o tabuleiro.
	 *
	 * @param cells matriz [linha][coluna] com o estado de cada célula
	 */
	public void setCells(CellState[][] cells) {
		assert cells != null;
		this.cells = cells;
		repaint();
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		drawCoordinates(g2);

		for (int r = 0; r < Game.BOARD_SIZE; r++)
			for (int c = 0; c < Game.BOARD_SIZE; c++)
				drawCell(g2, cells[r][c], MARGIN + c * CELL_SIZE, MARGIN + r * CELL_SIZE, CELL_SIZE);

		g2.setColor(GRID_COLOR);
		for (int i = 0; i <= Game.BOARD_SIZE; i++) {
			int p = MARGIN + i * CELL_SIZE;
			g2.drawLine(MARGIN, p, MARGIN + Game.BOARD_SIZE * CELL_SIZE, p);
			g2.drawLine(p, MARGIN, p, MARGIN + Game.BOARD_SIZE * CELL_SIZE);
		}
		g2.dispose();
	}

	private void drawCoordinates(Graphics2D g2) {
		g2.setColor(getForeground());
		g2.setFont(getFont().deriveFont(Font.BOLD, 13f));
		FontMetrics fm = g2.getFontMetrics();
		for (int i = 0; i < Game.BOARD_SIZE; i++) {
			String colLabel = String.valueOf(i + 1);
			int x = MARGIN + i * CELL_SIZE + (CELL_SIZE - fm.stringWidth(colLabel)) / 2;
			g2.drawString(colLabel, x, MARGIN - 8);

			String rowLabel = String.valueOf((char) ('A' + i));
			int y = MARGIN + i * CELL_SIZE + (CELL_SIZE + fm.getAscent() - fm.getDescent()) / 2;
			g2.drawString(rowLabel, (MARGIN - fm.stringWidth(rowLabel)) / 2, y);
		}
	}

	/**
	 * Desenha uma célula num dado estado. Usado também pela legenda.
	 */
	static void drawCell(Graphics2D g2, CellState state, int x, int y, int size) {
		Graphics2D g = (Graphics2D) g2.create();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		int pad = Math.max(3, size / 5);
		switch (state) {
			case WATER -> {
				g.setColor(WATER_COLOR);
				g.fillRect(x, y, size, size);
			}
			case MISS -> {
				g.setColor(MISS_COLOR);
				g.fillRect(x, y, size, size);
				g.setColor(GRID_COLOR);
				int d = size / 3;
				g.fillOval(x + (size - d) / 2, y + (size - d) / 2, d, d);
			}
			case SHIP -> {
				g.setColor(WATER_COLOR);
				g.fillRect(x, y, size, size);
				g.setColor(SHIP_COLOR);
				g.fillRoundRect(x + 2, y + 2, size - 4, size - 4, size / 3, size / 3);
			}
			case HIT -> {
				g.setColor(HIT_COLOR);
				g.fillRect(x, y, size, size);
				g.setColor(Color.WHITE);
				g.setStroke(new BasicStroke(Math.max(2f, size / 12f)));
				int cx = x + size / 2, cy = y + size / 2, rad = size / 2 - pad;
				g.drawLine(cx - rad, cy, cx + rad, cy);
				g.drawLine(cx, cy - rad, cx, cy + rad);
				g.drawOval(cx - rad / 2, cy - rad / 2, rad, rad);
			}
			case SUNK -> {
				g.setColor(SUNK_COLOR);
				g.fillRect(x, y, size, size);
				g.setColor(Color.WHITE);
				g.setStroke(new BasicStroke(Math.max(2f, size / 10f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
				g.drawLine(x + pad, y + pad, x + size - pad, y + size - pad);
				g.drawLine(x + size - pad, y + pad, x + pad, y + size - pad);
			}
		}
		g.dispose();
	}
}
