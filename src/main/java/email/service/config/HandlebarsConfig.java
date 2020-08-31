package email.service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;

@Configuration
public class HandlebarsConfig {

    @Bean
    public Handlebars handlebars() {
        // 如果沒設定 class path loader 會預設抓取 /resources 下的 *.hbs 檔案
        // 有多一層就必須額外再加路徑： compile("/handlebars/recalculate-announce")
        // 而設定後就可以直接用檔名抓取
        TemplateLoader loader = new ClassPathTemplateLoader("/handlebars", ".hbs");
        return new Handlebars(loader);
    };
}
