package br.com.jtech.tasklist.config.infra.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

/**
 * Logs the active environment when the application finishes starting.
 */
@Configuration
public class ReadyEventListener {

    private static final Logger log = LoggerFactory.getLogger(ReadyEventListener.class);

    @Value("${application.environment:LOCAL}")
    private String environment;

    @EventListener(ApplicationReadyEvent.class)
    public void start(ApplicationReadyEvent event) {
        log.info(">>> Connector Ready");
        show();
    }

    private void show() {
        log.info("======================================================");
        log.info("== Execute mode:.....................'{}'", environment);
        log.info("======================================================");
    }
}