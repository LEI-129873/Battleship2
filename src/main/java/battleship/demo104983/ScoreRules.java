// 104983 - Gonçalo Sena Esteves
package battleship.demo104983;

/**
 * Regras de pontuação do jogo, reunidas numa só classe.
 * Correção do cheiro Shotgun Surgery com Move Method + Inline Class: o cálculo que estava
 * espalhado por 8 classes passou para aqui, por isso uma mudança de regra altera só esta classe.
 */
public final class ScoreRules {

	private static final int HIT_POINTS = 10;
	private static final int SINK_BONUS_PER_SIZE = 5;
	private static final int STREAK_MIN = 3;

	private ScoreRules() {
	}

	/**
	 * Pontuação de um ataque a um navio (antes GalleonScore, FrigateScore, CarrackScore,
	 * CaravelScore e BargeScore).
	 *
	 * @param shipSize tamanho do navio
	 * @param hits     número de tiros certeiros
	 * @param sunk     se o navio foi afundado
	 * @return a pontuação obtida
	 */
	public static int shipScore(int shipSize, int hits, boolean sunk) {
		int points = hits * HIT_POINTS;
		if (sunk) {
			points += shipSize * SINK_BONUS_PER_SIZE;
		}
		return points;
	}

	/**
	 * Penalização por tiros falhados (antes MissPenalty).
	 *
	 * @param misses número de tiros falhados
	 * @return a penalização (valor negativo)
	 */
	public static int missPenalty(int misses) {
		return -misses * HIT_POINTS / 2;
	}

	/**
	 * Bónus por acertos consecutivos (antes StreakBonus).
	 *
	 * @param streak número de acertos consecutivos
	 * @return o bónus obtido
	 */
	public static int streakBonus(int streak) {
		return streak >= STREAK_MIN ? streak * HIT_POINTS : 0;
	}

	/**
	 * Bónus por afundar a frota inteira (antes FleetSunkBonus).
	 *
	 * @param totalShipSize soma dos tamanhos de todos os navios da frota
	 * @return o bónus obtido
	 */
	public static int fleetSunkBonus(int totalShipSize) {
		return 2 * totalShipSize * SINK_BONUS_PER_SIZE;
	}
}
