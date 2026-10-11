package battleship;

import java.io.IOException;
import java.util.Scanner;

import ch.qos.logback.core.net.SyslogOutputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.apache.commons.lang3.time.StopWatch;

/**
 * The type Tasks.
 */
public class Tasks {
	/**
	 * The constant LOGGER.
	 */
	private static final Logger LOGGER = LogManager.getLogger();

	/**
	 * Comandos em Português
	 */
	private static final String AJUDA = "ajuda";
	private static final String GERAFROTA = "gerafrota";
	private static final String LEFROTA = "lefrota";
	private static final String DESISTIR = "desisto";
	private static final String RAJADA = "rajada";
	private static final String TIROS = "tiros";
	private static final String MAPA = "mapa";
	private static final String STATUS = "estado";
	private static final String SIMULA = "simula";
	private static final String PDF = "pdf";
	private static final String IDIOMA = "idioma";

	/**
	 * Comandos em Inglês (aliases)
	 */
	private static final String HELP = "help";
	private static final String GENFLEET = "genfleet";
	private static final String LOADFLEET = "loadfleet";
	private static final String QUIT = "quit";
	private static final String EXIT = "exit";
	private static final String FIRE = "fire";
	private static final String SHOTS = "shots";
	private static final String MAP = "map";
	private static final String STATUS_EN = "status";
	private static final String SIMULATE = "simulate";
	private static final String LANG = "lang";

	/**
	 * This task also tests the fighting element of a round of three shots
	 */
	public static void menu() {

		IFleet myFleet = null;
		IGame game = null;
		menuHelp();

		Scanner in = new Scanner(System.in);

		while (true) {
			System.out.print("> ");
			StopWatch watch = StopWatch.createStarted(); // começa a contar assim que o prompt aparece
			String command = in.next();

			if (command.equals(DESISTIR) || command.equalsIgnoreCase(QUIT) || command.equalsIgnoreCase(EXIT))
				break;

			switch (command.toLowerCase()) {
				case GERAFROTA:
				case GENFLEET:
					myFleet = Fleet.createRandom();
					game = new Game(myFleet);
					game.printMyBoard(false, true);
					break;
				case LEFROTA:
				case LOADFLEET:
					myFleet = buildFleet(in);
					game = new Game(myFleet);
					game.printMyBoard(false, true);
					break;
				case STATUS:
				case STATUS_EN:
					if (myFleet != null)
						myFleet.printStatus();
					break;
				case MAPA:
				case MAP:
					if (myFleet != null)
						game.printMyBoard(false, true);
					break;
				case RAJADA:
				case FIRE:
					if (game != null) {
						game.readEnemyFire(in);
						myFleet.printStatus();
						game.printMyBoard(true, false);

						if (game.getRemainingShips() == 0) {
							printElapsed(watch);
							game.over();
							System.exit(0);
						}
					}
					break;
				case SIMULA:
				case SIMULATE:
					if (game != null) {
						while (game.getRemainingShips() > 0) {
							game.randomEnemyFire();
							myFleet.printStatus();
							game.printMyBoard(true, false);
							try {
								Thread.sleep(3000);
							} catch (InterruptedException e) {
								Thread.currentThread().interrupt();
							}
						}
						printElapsed(watch);
						game.over();
						System.exit(0);
					}
					break;
				case TIROS:
				case SHOTS:
					if (game != null)
						game.printMyBoard(true, true);
					break;
				case PDF:
					if (game != null) {
						try {
							GamePDFExporter.generatePDF(game.getMyMoves(), game.getAlienMoves());
							System.out.println("Histórico exportado para historico-partida.pdf");
						} catch (IOException e) {
							System.err.println("Não foi possível gerar o PDF: " + e.getMessage());
						}
					}
					break;
				case AJUDA:
				case HELP:
					menuHelp();
					break;
				case IDIOMA:
				case LANG:
					if (in.hasNext()) {
						String novoIdioma = in.next();
						I18n.setLanguage(novoIdioma);
						System.out.println(I18n.get("game.language.changed"));
					} else {
						System.out.println(I18n.get("game.language.usage"));
					}
					break;
				default:
					System.out.println(I18n.get("game.unknown.command", command));
			}
			printElapsed(watch);
		}
		System.out.println(I18n.get("game.goodbye"));
	}

	private static void printElapsed(StopWatch watch) {
		watch.stop();
		double seconds = watch.getTime() / 1000.0;
		System.out.printf("Tempo de execução: %.2f segundos%n", seconds);
	}

	/**
	 * This function provides help information about the menu commands.
	 */
	public static void menuHelp() {
		System.out.println(I18n.get("help.title"));
		System.out.println(I18n.get("help.subtitle"));
		System.out.println(I18n.get("help.gerafrota"));
		System.out.println(I18n.get("help.lefrota"));
		System.out.println(I18n.get("help.status"));
		System.out.println(I18n.get("help.mapa"));
		System.out.println(I18n.get("help.rajada"));
		System.out.println(I18n.get("help.simula"));
		System.out.println(I18n.get("help.tiros"));
		System.out.println(I18n.get("help.lang"));
		System.out.println(I18n.get("help.pdf"));
		System.out.println(I18n.get("help.desistir"));
		System.out.println(I18n.get("help.footer"));
	}

	public static Fleet buildFleet(Scanner in) {
		assert in != null;

		Fleet fleet = new Fleet();
		int i = 0;
		while (i < Fleet.FLEET_SIZE) {
			IShip s = readShip(in);
			if (s != null) {
				boolean success = fleet.addShip(s);
				if (success)
					i++;
				else
					LOGGER.info("Falha na criacao de {} {} {}", s.getCategory(), s.getBearing(), s.getPosition());
			} else {
				LOGGER.info("Navio desconhecido!");
			}
		}
		LOGGER.info("{} navios adicionados com sucesso!", i);
		return fleet;
	}

	public static Ship readShip(Scanner in) {
		assert in != null;

		String shipKind = in.next();
		Position pos = readPosition(in);
		char c = in.next().charAt(0);
		Compass bearing = Compass.charToCompass(c);
		return Ship.buildShip(shipKind, bearing, pos);
	}

	public static Position readPosition(Scanner in) {
		assert in != null;

		int row = in.nextInt();
		int column = in.nextInt();
		return new Position(row, column);
	}

	public static IPosition readClassicPosition(@NotNull Scanner in) {
		if (!in.hasNext()) {
			throw new IllegalArgumentException("Nenhuma posição válida encontrada!");
		}

		String part1 = in.next();
		String part2 = null;

		if (in.hasNextInt()) {
			part2 = in.next();
		}

		String input = (part2 != null) ? part1 + part2 : part1;
		input = input.toUpperCase();

		if (input.matches("[A-Z]\\d+")) {
			char column = input.charAt(0);
			int row = Integer.parseInt(input.substring(1));
			return new Position(column, row);
		} else if (part2 != null && part1.matches("[A-Z]") && part2.matches("\\d+")) {
			char column = part1.charAt(0);
			int row = Integer.parseInt(part2);
			return new Position(column, row);
		} else {
			throw new IllegalArgumentException("Formato inválido. Use 'A3', 'A 3' ou similar.");
		}
	}
}