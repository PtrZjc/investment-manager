import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;
import java.util.ResourceBundle;

public class test {
    public static void main(String[] args) {
        Locale locale = LocaleContextHolder.getLocale();

        ResourceBundle messages = ResourceBundle.getBundle("messages", locale);

        System.out.println(messages.getString("product.capitalization"));

    }
}
