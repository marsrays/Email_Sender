package email.service.config;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.DefaultPropertySourceFactory;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.lang.Nullable;

public class YmlPropertySourceFactory extends DefaultPropertySourceFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(YmlPropertySourceFactory.class);

    @Override
    public PropertySource<?> createPropertySource(@Nullable String name, EncodedResource resource) throws IOException {
        LOGGER.debug("Load : {}", resource.getResource().getFilename());
        Properties propertiesFromYml = loadYmlIntoProperties(resource);
        String sourceName = name == null ? resource.getResource().getFilename() : name;
        if (null == sourceName) {
            sourceName = "";
        }
        return new PropertiesPropertySource(sourceName, propertiesFromYml);
    }

    private Properties loadYmlIntoProperties(EncodedResource resource) throws FileNotFoundException {
        try {
            YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
            factory.setResources(resource.getResource());
            factory.afterPropertiesSet();
            return factory.getObject();
        } catch (IllegalStateException e) {
            Throwable cause = e.getCause();
            if (cause instanceof FileNotFoundException) {
                throw (FileNotFoundException) new FileNotFoundException().initCause(e);
            }
            throw e;
        }
    }
}
