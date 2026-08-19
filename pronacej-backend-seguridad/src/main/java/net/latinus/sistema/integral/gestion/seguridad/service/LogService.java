package net.latinus.sistema.integral.gestion.seguridad.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogService {

    private Integer maxSizeText = 5000;
    private Logger log;

    public <T> LogService(Class<T> classT) {
        log = LoggerFactory.getLogger(classT);
    }

    public void info(String message) {
        message = this.checkSize(message);
        log.info(message);

    }

    public void error(String message) {
        message = this.checkSize(message);
        log.error(message);

    }

    public void error(String message, Object var2, Object var3) {
        message = this.checkSize(message);
        log.error(message, var2, var3);

    }


    public void warn(String message) {
        message = this.checkSize(message);
        log.warn(message);
    }

    private String checkSize(String message) {
        if (message == null) {
            log.info("Message is null");
            return null;
        }
        if (message.length() > this.maxSizeText) {
            message = message.substring(0, this.maxSizeText - 1);
        }

        return message;
    }

}
