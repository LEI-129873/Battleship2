// 104983 - Gonçalo Sena Esteves
package battleship.demo104983;

/**
 * Pontuação de um ataque a um navio do tipo Barge (tamanho 1).
 */
public class BargeScore {

	/**
	 * Calcula a pontuação de um ataque.
	 *
	 * @param hits  número de tiros certeiros
	 * @param sunk  se o navio foi afundado
	 * @return a pontuação obtida
	 */
	public int score(int hits, boolean sunk) {
		int points = hits * ScoreRules.hitPoints();
		if (sunk) {
			points += 1 * ScoreRules.sinkBonusPerSize();
		}
		return points;
	}
}
