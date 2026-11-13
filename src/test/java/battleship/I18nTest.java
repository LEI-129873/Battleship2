package battleship;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for I18n.
 * Author: Martim Barros
 * Date: 2026-11-07
 */
@DisplayName("Tests for I18n (Internationalization)")
class I18nTest {

    @BeforeEach
    void setUp() {
        // Garantir que começamos sempre em português antes de cada teste
        I18n.setLanguage("pt");
    }

    @AfterEach
    void tearDown() {
        // Voltar ao estado inicial
        I18n.setLanguage("pt");
    }

    @Test
    @DisplayName("Default language should be Portuguese")
    void testDefaultLanguage() {
        assertEquals("pt", I18n.getCurrentLanguage(), "Default language should be 'pt'");
    }

    @Test
    @DisplayName("Change language to English")
    void testSetLanguageEnglish() {
        I18n.setLanguage("en");
        assertEquals("en", I18n.getCurrentLanguage(), "Language should be 'en' after changing");
    }

    @Test
    @DisplayName("Change language to Portuguese")
    void testSetLanguagePortuguese() {
        I18n.setLanguage("en"); // muda para inglês primeiro
        I18n.setLanguage("pt"); // volta a mudar para português
        assertEquals("pt", I18n.getCurrentLanguage(), "Language should be 'pt' after changing back");
    }

    @Test
    @DisplayName("Get a valid message key with arguments")
    void testGetValidKeyWithArgs() {
        // "game.unknown.command" existe no messages.properties e recebe 1 argumento
        // Se não tiveres a certeza do texto exato, verificamos apenas que NÃO devolve a chave de erro "!key!"
        String message = I18n.get("game.unknown.command", "teste");

        assertNotNull(message, "Message should not be null");
        assertFalse(message.startsWith("!"), "Should return a valid message, not an error flag");
        assertTrue(message.contains("teste"), "Message should contain the passed argument");
    }

    @Test
    @DisplayName("Get an invalid key returns the error format")
    void testGetInvalidKey() {
        // Se a chave não existir, o método retorna "!chave!"
        String message = I18n.get("chave.que.nao.existe");

        assertEquals("!chave.que.nao.existe!", message, "Should return the error format for missing keys");
    }
}