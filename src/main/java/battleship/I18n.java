package battleship;

import com.ibm.icu.text.MessageFormat;
import com.ibm.icu.util.ULocale;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Gestor de internacionalização do jogo recorrendo à biblioteca ICU4J.
 */
public class I18n {

    private static ULocale currentULocale = ULocale.forLanguageTag("pt-PT");
    private static ResourceBundle bundle = ResourceBundle.getBundle("messages", currentULocale.toLocale());

    private I18n() {
        // Classe utilitária: construtor privado
    }

    /**
     * Define o idioma ativo do jogo ("pt" para português, "en" para inglês).
     *
     * @param language Código do idioma ("pt" ou "en")
     */
    public static void setLanguage(String language) {
        if ("en".equalsIgnoreCase(language)) {
            currentULocale = ULocale.US;
        } else {
            currentULocale = ULocale.forLanguageTag("pt-PT");
        }
        bundle = ResourceBundle.getBundle("messages", currentULocale.toLocale());
    }

    /**
     * Devolve o idioma atual.
     */
    public static String getCurrentLanguage() {
        return currentULocale.getLanguage();
    }

    /**
     * Obtém e formata a mensagem correspondente à chave com os parâmetros indicados.
     *
     * @param key Chave no ficheiro .properties
     * @param args Argumentos a injetar na mensagem ({0}, {1}, ...)
     * @return Texto internacionalizado formatado
     */
    public static String get(String key, Object... args) {
        try {
            String pattern = bundle.getString(key);
            MessageFormat formatter = new MessageFormat(pattern, currentULocale);
            return formatter.format(args);
        } catch (Exception e) {
            return "!" + key + "!";
        }
    }
}