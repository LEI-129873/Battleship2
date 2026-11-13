package battleship;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the nested record IGame.ShotResult.
 * Author: Martim Barros
 * Date: 2026-11-07
 */
@DisplayName("Tests for IGame.ShotResult record")
class IGameTest {

    private IShip dummyShip;

    @BeforeEach
    void setUp() {
        // Criamos um navio só para preencher o campo IShip do record
        dummyShip = new SingleCellShip(Compass.NORTH, new Position(0, 0));
    }

    @AfterEach
    void tearDown() {
        dummyShip = null;
    }

    @Test
    @DisplayName("Constructor stores all four values correctly")
    void constructorAndGetters() {
        IGame.ShotResult result = new IGame.ShotResult(true, false, dummyShip, false);

        assertAll("ShotResult components",
                () -> assertTrue(result.valid(), "valid should be true"),
                () -> assertFalse(result.repeated(), "repeated should be false"),
                () -> assertSame(dummyShip, result.ship(), "ship should be the same instance"),
                () -> assertFalse(result.sunk(), "sunk should be false")
        );
    }

    @Test
    @DisplayName("Constructor accepts null ship")
    void constructorWithNullShip() {
        IGame.ShotResult result = new IGame.ShotResult(true, false, null, false);
        assertNull(result.ship(), "ship should be null");
    }

    @Test
    @DisplayName("Two ShotResults with identical values are equal")
    void equalsSameValues() {
        IGame.ShotResult a = new IGame.ShotResult(true, false, dummyShip, false);
        IGame.ShotResult b = new IGame.ShotResult(true, false, dummyShip, false);

        assertEquals(a, b, "ShotResults with same values should be equal");
        assertEquals(a.hashCode(), b.hashCode(), "Equal objects must have equal hashCodes");
    }

    @Test
    @DisplayName("ShotResults differing in any field are not equal")
    void equalsDifferentValues() {
        IGame.ShotResult base = new IGame.ShotResult(true, false, dummyShip, false);

        assertNotEquals(base, new IGame.ShotResult(false, false, dummyShip, false));
        assertNotEquals(base, new IGame.ShotResult(true, true, dummyShip, false));
        assertNotEquals(base, new IGame.ShotResult(true, false, null, false));
        assertNotEquals(base, new IGame.ShotResult(true, false, dummyShip, true));
    }

    @Test
    @DisplayName("ShotResult is not equal to null or a different type")
    void equalsOtherTypes() {
        IGame.ShotResult result = new IGame.ShotResult(true, false, dummyShip, false);

        assertNotEquals(null, result);
        assertNotEquals("some string", result);
    }

    @Test
    @DisplayName("toString contains the record name and field values")
    void toStringContainsFields() {
        IGame.ShotResult result = new IGame.ShotResult(true, false, dummyShip, true);
        String s = result.toString();

        assertTrue(s.contains("ShotResult"), "toString should contain the record name");
        assertTrue(s.contains("valid=true"), "toString should contain valid=true");
        assertTrue(s.contains("repeated=false"), "toString should contain repeated=false");
        assertTrue(s.contains("sunk=true"), "toString should contain sunk=true");
    }
}