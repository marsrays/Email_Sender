package email.service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.loader.ClasspathLoader;
import com.mitchellbosecke.pebble.template.PebbleTemplate;

@Configuration
public class TemplateConfig {

    @Bean
    public PebbleEngine pebbleEngine() {
        // 如果沒設定 class path loader 會吃 getTemplate 輸入的路徑 (以 /resources 為根目錄)
        ClasspathLoader classPathLoader = new ClasspathLoader();
        classPathLoader.setPrefix("html");
        classPathLoader.setSuffix(".html");

        return new PebbleEngine.Builder()
            .loader(classPathLoader)
            .build();
    }

    @Bean
    public PebbleTemplate recalculateTemplate(PebbleEngine pebbleEngine) {
        return pebbleEngine.getTemplate("recalculate-announce");
    }

    @Bean
    public PebbleTemplate activateTemplate(PebbleEngine pebbleEngine) {
        return pebbleEngine.getTemplate("activate-account");
    }
}
