package battleship;

/**
 * Objetos auxiliares partilhados pelos testes.
 */
public final class TestFixtures {

    private TestFixtures() {
    }

    public static IFleet emptyFleet() {
        return new Fleet();
    }

    public static Game newGame() {
        return new Game(new Fleet());
    }
}