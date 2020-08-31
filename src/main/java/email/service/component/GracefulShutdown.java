package email.service.component;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rapidoid.u.U;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

/**
 * 优雅关闭 Spring Boot
 * curl -X POST http://localhost:8080/actuator/shutdown
 * nginx 必需allow ip指定，不能被外部關閉
 */
@Component
public class GracefulShutdown implements ApplicationListener<ContextClosedEvent>, ApplicationContextAware {
    private final static Logger LOGGER = LogManager.getLogger(GracefulShutdown.class);

    @Autowired
    private GracefulShutdownWrapper gracefulShutdownWrapper;

    private ApplicationContext context;

    @Override
    public void onApplicationEvent(ContextClosedEvent contextClosedEvent) {

        LOGGER.info("Server graceful shutdown now");

        if (U.isEmpty(gracefulShutdownWrapper.getGracefulShutdownHandler())) {
            return;
        }

        gracefulShutdownWrapper.getGracefulShutdownHandler().shutdown();

        gracefulShutdownWrapper.getGracefulShutdownHandler().addShutdownListener(shutdownSuccessful -> {
            LOGGER.info("graceful shutdown listener get result: {}", shutdownSuccessful);
            if (shutdownSuccessful) {

                // Get the webapp's ClassLoader
                ClassLoader cl = Thread.currentThread().getContextClassLoader();
                // Loop through all drivers
                Enumeration<Driver> drivers = DriverManager.getDrivers();
                while (drivers.hasMoreElements()) {
                    Driver driver = drivers.nextElement();
                    if (Thread.currentThread().getContextClassLoader() == cl) {
                        // This driver was registered by the webapp's ClassLoader, so deregister it:
                        try {
                            LOGGER.info("Deregistering JDBC driver {}", driver);
                            DriverManager.deregisterDriver(driver);
                        } catch (SQLException ex) {
                            LOGGER.error("Error deregistering JDBC driver {}", driver, ex);
                        }
                    } else {
                        // driver was not registered by the webapp's ClassLoader and may be in use elsewhere
                        LOGGER.trace("Not deregistering JDBC driver {} as it does not belong to this webapp's ClassLoader", driver);
                    }
                }
            }
        });

        try {
            gracefulShutdownWrapper.getGracefulShutdownHandler().awaitShutdown();
            LOGGER.info("Server graceful shutdown successed");

        } catch (InterruptedException e) {
            LOGGER.error("Server graceful shutdown failure", e);
        }

        LOGGER.info("ApplicationContext closed now");

        try {
            ((ConfigurableApplicationContext) context).close();
            LOGGER.info("ApplicationContext closed successed");
        } catch (BeansException e) {
            LOGGER.error("ApplicationContext closed failure", e);
        }

    }

    @Override
    public void setApplicationContext(ApplicationContext ctx) {
        this.context = ctx;

    }
}
