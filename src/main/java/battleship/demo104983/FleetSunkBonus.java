// 104983 - Gonçalo Sena Esteves
package battleship.demo104983;

/**
 * Bónus por afundar a frota inteira.
 */
public class FleetSunkBonus {

	/**
	 * Calcula o bónus por afundar todos os navios.
	 *
	 * @param totalShipSize soma dos tamanhos de todos os navios da frota
	 * @return o bónus obtido
	 */
	public int bonus(int totalShipSize) {
		return 2 * totalShipSize * ScoreRules.sinkBonusPerSize();
	}
}
