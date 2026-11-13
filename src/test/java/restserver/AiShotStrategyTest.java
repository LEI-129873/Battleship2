package restserver;

import battleship.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes para a estratégia de disparos da IA")
class AiShotStrategyTest {

    private AiShotStrategy strategy;
    private Game game;

    @BeforeEach
    void setUp() {
        strategy = new AiShotStrategy();
        // Instanciamos um jogo real com uma frota vazia para servir de contexto
        game = new Game(new Fleet());
    }

    @Test
    @DisplayName("Fase HUNT: Deve devolver 3 tiros no padrão de xadrez")
    void testComputeShotsHuntPhase() {
        // Sem histórico de jogadas, a IA deve entrar em modo de caça
        List<IPosition> shots = strategy.computeShots(game);

        assertEquals(3, shots.size(), "A IA deve devolver exatamente 3 tiros");

        for (IPosition pos : shots) {
            assertTrue(pos.isInside(), "O tiro deve estar dentro do tabuleiro");
            // O padrão da IA para caçar é disparar apenas onde (linha + coluna) é par
            assertEquals(0, (pos.getRow() + pos.getColumn()) % 2,
                    "O tiro de caça deve seguir o padrão de xadrez (soma par)");
        }

        // Garantir que não há tiros repetidos na mesma rajada
        assertEquals(3, shots.stream().distinct().count(), "Os tiros não devem ser repetidos");
    }

    @Test
    @DisplayName("Fase TARGET: Deve mudar de estratégia ao acertar num navio")
    void testComputeShotsTargetPhase() {
        // 1. Simular que a IA acertou num navio em (5,5), mas não o afundou
        IPosition hitPosition = new Position(5, 5);
        IGame.ShotResult hitResult = new IGame.ShotResult(true, false, new Barge(Compass.NORTH, hitPosition), false);

        // Injetamos esta jogada passada no histórico do jogo
        Move pastMove = new Move(1, List.of(hitPosition), List.of(hitResult));
        game.getAlienMoves().add(pastMove);

        // 2. Pedir os próximos 3 tiros à IA
        List<IPosition> shots = strategy.computeShots(game);

        assertEquals(3, shots.size(), "Deve devolver 3 tiros");

        // 3. Verificar se a IA mudou para modo de perseguição (TARGET)
        boolean shotNearHit = false;
        for (IPosition pos : shots) {
            if (pos.isAdjacentTo(hitPosition)) {
                shotNearHit = true;
                break;
            }
        }
        assertTrue(shotNearHit, "A IA deve tentar afundar o navio disparando nas posições vizinhas ao acerto");
    }

    @Test
    @DisplayName("Fallback: Deve encontrar tiros válidos mesmo com o tabuleiro quase cheio")
    void testComputeShotsFallback() {
        // Enche o tabuleiro quase todo com tiros antigos (deixa apenas 3 posições livres)
        for (int r = 0; r < Game.BOARD_SIZE; r++) {
            for (int c = 0; c < Game.BOARD_SIZE; c++) {
                if ((r < 9) || (r == 9 && c < 7)) { // Bloqueia tudo exceto as últimas posições
                    IPosition pos = new Position(r, c);
                    // Tiro antigo na água
                    game.getAlienMoves().add(
                            new Move(r * 10 + c, List.of(pos), List.of(new IGame.ShotResult(true, false, null, false)))
                    );
                }
            }
        }

        // A IA vai tentar disparar, mas como as posições de xadrez estão cheias,
        // terá de usar o fallback para preencher os 3 tiros obrigatórios.
        List<IPosition> shots = strategy.computeShots(game);

        assertEquals(3, shots.size(), "A IA deve conseguir devolver 3 tiros mesmo com o tabuleiro cheio");

        // Confirma que ela não dispara em nenhuma das posições que já estavam no histórico
        for (IPosition pos : shots) {
            boolean jaFoiDisparado = pos.getRow() < 9 || (pos.getRow() == 9 && pos.getColumn() < 7);
            assertFalse(jaFoiDisparado, "A IA não deve repetir tiros antigos do histórico");
        }
    }
}
