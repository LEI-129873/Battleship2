// 104983 - Gonçalo Sena Esteves
package battleship.demo104983;

/**
 * Regras de pontuação usadas por várias classes (demonstração do cheiro Shotgun Surgery).
 * Qualquer alteração a estas regras obriga a rever todas as classes que as usam.
 */
public final class ScoreRules {

	private ScoreRules() {
	}

	/**
	 * Pontos por cada tiro certeiro.
	 *
	 * @return os pontos de um tiro certeiro
	 */
	public static int hitPoints() {
		return 10;
	}

	/**
	 * Bónus por afundar um navio, multiplicado pelo tamanho do navio.
	 *
	 * @return o bónus por unidade de tamanho
	 */
	public static int sinkBonusPerSize() {
		return 5;
	}
}
