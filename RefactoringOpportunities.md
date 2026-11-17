# Refactoring Opportunities

| Local | Cheiro no código | Refabricação | Aluno |
|---|---|---|---|
| Move::processEnemyFire (l. 99–100, cálculo de tiros exteriores explicado por comentário) | Comments | Extract Method (`countOutsideShots`) | 129873 |
| Move::processEnemyFire (l. 105, `validShots == 0 && repeatedShots > 0`) | Complex Conditional | Introduce Variable (`onlyRepeatedShots`) | 129873 |
| Move::processEnemyFire (l. 117 e 126, literal `" + "`) | Magic String | Introduce Constant (`SHOT_SEPARATOR`) | 129873 |
| Move::processEnemyFire (l. 158, variável `response`) | Uncommunicative Name | Rename (`jsonFields`) | 129873 |
| Move::processEnemyFire (l. 115, `boatName` usado uma vez) | Redundant Temporary Variable | Inline Variable | 129873 |
| AiShotStrategy::computeShots (l. 45–55, fase TARGET) | Long Method | Extract Method (`addNeighboursOfHits`) | 129825 |
| AiShotStrategy::getHitButNotSunkPositions (l. 101–103, `move.getShotResults()` repetido) | Duplicated Expression | Introduce Variable (`results`) | 129825 |
| AiShotStrategy::getHitButNotSunkPositions (parâmetro `alreadyShot` não usado) | Unused Parameter | Change Signature (remover `alreadyShot`) | 129825 |
| CallbackClient::sendShots (l. 91, variável `m2b`) | Uncommunicative Name | Rename (`studentResponse`) | 129825 |
| CallbackClient::sendShots (l. 72, `Duration.ofSeconds(10)`) | Magic Number | Introduce Constant (`REQUEST_TIMEOUT`) | 129825 |
| GameController::evaluateShots (l. 171–183, cadeia if/else do outcome) | Long Method / Switch-like Conditional | Extract Method (`fillOutcome`) | 129862 |
| GameController::receiveShots (l. 111, condição de posição inválida) | Complex Conditional | Introduce Variable (`invalidPosition`) | 129862 |
| GameController::receiveShots (l. 111, literal `10`) | Magic Number | Introduce Constant (`MAX_COLUMN`) | 129862 |
| GameController::receiveShots (l. 120, variável `m1bResponse`) | Uncommunicative Name | Rename (`turnResponse`) | 129862 |
| GameController::evaluateShots (parâmetro `session` não usado) | Unused Parameter | Change Signature (remover `session`) | 129862 |
| BoardState::ownBoard (l. 28–30 e 43–45, ciclos iguais) | Duplicated Code | Extract Method (`markShips`) | 104983 |
| BoardState::opponentBoard (l. 68, `!shot.isInside() \|\| !result.valid() \|\| result.repeated()`) | Complex Conditional | Introduce Variable (`ignoredShot`) | 104983 |
| BoardState::set (nome genérico) | Uncommunicative Name | Rename (`markCell`) | 104983 |
| GameSession::markAiWins / markStudentWins (`"AI_WINS"`, `"STUDENT_WINS"`) | Magic String | Introduce Constant (`AI_WINS`, `STUDENT_WINS`) | 104983 |
| GameSession (campo `shotsPerTurn`, sempre igual a `Game.NUMBER_SHOTS`) | Speculative Generality / Redundant Field | Inline Field | 104983 |