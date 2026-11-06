// 104983 - Gonçalo Sena Esteves
package battleship.demo104983;

/**
 * Bónus por acertos consecutivos.
 */
public class StreakBonus {

	/**
	 * Calcula o bónus: a partir de 3 acertos seguidos, cada acerto vale a dobrar.
	 *
	 * @param streak número de acertos consecutivos
	 * @return o bónus obtido
	 */
	public int bonus(int streak) {
		return streak >= 3 ? streak * ScoreRules.hitPoints() : 0;
	}
}
