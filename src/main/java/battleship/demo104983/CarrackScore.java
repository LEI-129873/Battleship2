// 104983 - Gonçalo Sena Esteves
package battleship.demo104983;

/**
 * Pontuação de um ataque a um navio do tipo Carrack (tamanho 3).
 */
public class CarrackScore {

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
			points += 3 * ScoreRules.sinkBonusPerSize();
		}
		return points;
	}
}
