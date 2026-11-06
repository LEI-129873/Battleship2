// 104983 - Gonçalo Sena Esteves
package battleship.demo104983;

/**
 * Penalização por tiros falhados.
 */
public class MissPenalty {

	/**
	 * Calcula a penalização: cada tiro falhado retira metade dos pontos de um acerto.
	 *
	 * @param misses número de tiros falhados
	 * @return a penalização (valor negativo)
	 */
	public int penalty(int misses) {
		return -misses * ScoreRules.hitPoints() / 2;
	}
}
