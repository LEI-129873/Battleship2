package restserver;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for class Dtos (e respetivas classes aninhadas).
 * Author: SENAS
 * Date: 2026-11-20 16:01
 * Cyclomatic Complexity:
 * - constructor Dtos(): 1
 * - constructor RegistrationRequest(): 1
 * - constructor RegistrationResponse(): 1
 * - constructor ShotRequest(): 1
 * - constructor ShotPosition(): 1
 * - constructor ShotResponse(): 1
 * - constructor ShotResult(): 1
 * - constructor ErrorResponse(String): 1
 *
 * Como são DTOs de um protocolo JSON, cada teste confirma também a
 * serialização/desserialização com Jackson (o contrato descrito em Dtos).
 */
class DtosTest {

	private Dtos dtos;
	private ObjectMapper mapper;

	@BeforeEach
	void setUp() {
		dtos = new Dtos();
		mapper = new ObjectMapper();
	}

	@AfterEach
	void tearDown() {
		dtos = null;
		mapper = null;
	}

	private static Dtos.ShotPosition position(String row, int column) {
		Dtos.ShotPosition p = new Dtos.ShotPosition();
		p.row = row;
		p.column = column;
		return p;
	}

	private static Dtos.ShotResult result(String row, int column, String outcome, String shipType) {
		Dtos.ShotResult r = new Dtos.ShotResult();
		r.row = row;
		r.column = column;
		r.outcome = outcome;
		r.shipType = shipType;
		return r;
	}

	@Test
	@DisplayName("Dtos pode ser instanciada (contentor das classes do protocolo)")
	void constructor() {
		assertNotNull(dtos, "Error: expected a Dtos instance but got null");
	}

	@Test
	@DisplayName("RegistrationRequest (m0) começa vazio e converte de/para JSON")
	void registrationRequest() throws JsonProcessingException {
		Dtos.RegistrationRequest empty = new Dtos.RegistrationRequest();
		Dtos.RegistrationRequest req = mapper.readValue(
				"{\"playerName\":\"Alice\",\"callbackUrl\":\"http://student-host:9090\"}",
				Dtos.RegistrationRequest.class);
		JsonNode json = mapper.readTree(mapper.writeValueAsString(req));
		assertAll(
				() -> assertNull(empty.playerName, "Error: expected null playerName but got " + empty.playerName),
				() -> assertNull(empty.callbackUrl, "Error: expected null callbackUrl but got " + empty.callbackUrl),
				() -> assertEquals("Alice", req.playerName, "Error: expected playerName Alice but got " + req.playerName),
				() -> assertEquals("http://student-host:9090", req.callbackUrl,
						"Error: expected callbackUrl http://student-host:9090 but got " + req.callbackUrl),
				() -> assertEquals("Alice", json.get("playerName").asText(),
						"Error: expected JSON field playerName=Alice but got " + json.get("playerName")));
	}

	@Test
	@DisplayName("RegistrationResponse (m0) guarda os campos e converte de/para JSON")
	void registrationResponse() throws JsonProcessingException {
		Dtos.RegistrationResponse resp = new Dtos.RegistrationResponse();
		resp.gameId = "550e8400";
		resp.status = "REGISTERED";
		resp.shotsPerTurn = 3;
		resp.message = "Game ready. You shoot first.";
		Dtos.RegistrationResponse back = mapper.readValue(mapper.writeValueAsString(resp), Dtos.RegistrationResponse.class);
		assertAll(
				() -> assertEquals(0, new Dtos.RegistrationResponse().shotsPerTurn,
						"Error: expected default shotsPerTurn 0"),
				() -> assertEquals("550e8400", back.gameId, "Error: expected gameId 550e8400 but got " + back.gameId),
				() -> assertEquals("REGISTERED", back.status, "Error: expected status REGISTERED but got " + back.status),
				() -> assertEquals(3, back.shotsPerTurn, "Error: expected shotsPerTurn 3 but got " + back.shotsPerTurn),
				() -> assertEquals(resp.message, back.message, "Error: expected message '" + resp.message + "' but got " + back.message));
	}

	@Test
	@DisplayName("ShotRequest (m1a/m2a) transporta a lista de tiros e converte de/para JSON")
	void shotRequest() throws JsonProcessingException {
		Dtos.ShotRequest req = new Dtos.ShotRequest();
		req.shots = List.of(position("B", 3), position("G", 7), position("D", 10));
		Dtos.ShotRequest back = mapper.readValue(mapper.writeValueAsString(req), Dtos.ShotRequest.class);
		assertAll(
				() -> assertNull(new Dtos.ShotRequest().shots, "Error: expected null shots in a new request"),
				() -> assertEquals(3, back.shots.size(), "Error: expected 3 shots but got " + back.shots.size()),
				() -> assertEquals("D", back.shots.get(2).row, "Error: expected row D but got " + back.shots.get(2).row),
				() -> assertEquals(10, back.shots.get(2).column, "Error: expected column 10 but got " + back.shots.get(2).column));
	}

	@Test
	@DisplayName("ShotPosition guarda a posição em notação clássica (linha letra, coluna número)")
	void shotPosition() throws JsonProcessingException {
		Dtos.ShotPosition pos = mapper.readValue("{\"row\":\"J\",\"column\":10}", Dtos.ShotPosition.class);
		JsonNode json = mapper.readTree(mapper.writeValueAsString(position("A", 1)));
		assertAll(
				() -> assertEquals("J", pos.row, "Error: expected row J but got " + pos.row),
				() -> assertEquals(10, pos.column, "Error: expected column 10 but got " + pos.column),
				() -> assertEquals("A", json.get("row").asText(), "Error: expected JSON row A but got " + json.get("row")),
				() -> assertEquals(1, json.get("column").asInt(), "Error: expected JSON column 1 but got " + json.get("column")));
	}

	@Test
	@DisplayName("ShotResponse (m1b/m2b) guarda resultados, navios restantes, estado e vencedor")
	void shotResponse() throws JsonProcessingException {
		Dtos.ShotResponse resp = new Dtos.ShotResponse();
		resp.results = List.of(result("B", 3, "MISS", null), result("G", 7, "HIT", "caravela"));
		resp.shipsRemaining = 8;
		resp.gameStatus = "ONGOING";
		JsonNode json = mapper.readTree(mapper.writeValueAsString(resp));
		Dtos.ShotResponse back = mapper.treeToValue(json, Dtos.ShotResponse.class);
		assertAll(
				() -> assertTrue(json.has("winner") && json.get("winner").isNull(),
						"Error: expected JSON winner=null while ONGOING but got " + json.get("winner")),
				() -> assertEquals(2, back.results.size(), "Error: expected 2 results but got " + back.results.size()),
				() -> assertEquals(8, back.shipsRemaining, "Error: expected 8 ships remaining but got " + back.shipsRemaining),
				() -> assertEquals("ONGOING", back.gameStatus, "Error: expected ONGOING but got " + back.gameStatus),
				() -> assertNull(back.winner, "Error: expected null winner but got " + back.winner));
	}

	@Test
	@DisplayName("ShotResult guarda o resultado de um tiro, com shipType só em HIT/SUNK")
	void shotResult() throws JsonProcessingException {
		Dtos.ShotResult sunk = mapper.readValue(
				"{\"row\":\"D\",\"column\":10,\"outcome\":\"SUNK\",\"shipType\":\"galeao\"}", Dtos.ShotResult.class);
		Dtos.ShotResult miss = mapper.readValue(
				mapper.writeValueAsString(result("B", 3, "MISS", null)), Dtos.ShotResult.class);
		assertAll(
				() -> assertEquals("D", sunk.row, "Error: expected row D but got " + sunk.row),
				() -> assertEquals(10, sunk.column, "Error: expected column 10 but got " + sunk.column),
				() -> assertEquals("SUNK", sunk.outcome, "Error: expected outcome SUNK but got " + sunk.outcome),
				() -> assertEquals("galeao", sunk.shipType, "Error: expected shipType galeao but got " + sunk.shipType),
				() -> assertEquals("MISS", miss.outcome, "Error: expected outcome MISS but got " + miss.outcome),
				() -> assertNull(miss.shipType, "Error: expected null shipType for MISS but got " + miss.shipType));
	}

	@Test
	@DisplayName("ErrorResponse guarda a mensagem recebida no construtor e serializa-a no campo error")
	void errorResponse() throws JsonProcessingException {
		Dtos.ErrorResponse err = new Dtos.ErrorResponse("Game not found: abc-123");
		JsonNode json = mapper.readTree(mapper.writeValueAsString(err));
		assertAll(
				() -> assertEquals("Game not found: abc-123", err.error,
						"Error: expected the constructor message but got " + err.error),
				() -> assertEquals("Game not found: abc-123", json.get("error").asText(),
						"Error: expected JSON error field with the message but got " + json.get("error")),
				() -> assertNull(new Dtos.ErrorResponse(null).error, "Error: expected null error when built with null"));
	}
}
