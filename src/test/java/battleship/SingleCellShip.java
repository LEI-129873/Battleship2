package battleship;

/**
 * Navio de uma célula para os testes (equivalente a Barge, sem depender de Barge).
 */
class SingleCellShip extends Ship {

    SingleCellShip(Compass bearing, IPosition pos) {
        super("Barca", bearing, pos, 1);
        getPositions().add(new Position(pos.getRow(), pos.getColumn()));
    }
}