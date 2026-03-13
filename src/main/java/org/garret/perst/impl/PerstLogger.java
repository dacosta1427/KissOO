package org.garret.perst.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PerstLogger {
    private final Logger logger;

    private PerstLogger(Logger logger) {
        this.logger = logger;
    }

    public static PerstLogger getLogger(Class<?> clazz) {
        return new PerstLogger(LoggerFactory.getLogger(clazz));
    }

    public static PerstLogger getLogger(String name) {
        return new PerstLogger(LoggerFactory.getLogger(name));
    }

    public void error(String message) {
        logger.error(message);
    }

    public void error(String message, Throwable t) {
        logger.error(message, t);
    }

    public void warn(String message) {
        logger.warn(message);
    }

    public void warn(String message, Throwable t) {
        logger.warn(message, t);
    }

    public void info(String message) {
        logger.info(message);
    }

    public void info(String message, Throwable t) {
        logger.info(message, t);
    }

    public void debug(String message) {
        logger.debug(message);
    }

    public void debug(String message, Throwable t) {
        logger.debug(message, t);
    }

    public void trace(String message) {
        logger.trace(message);
    }

    public void trace(String message, Throwable t) {
        logger.trace(message, t);
    }

    public boolean isErrorEnabled() {
        return logger.isErrorEnabled();
    }

    public boolean isWarnEnabled() {
        return logger.isWarnEnabled();
    }

    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    public boolean isTraceEnabled() {
        return logger.isTraceEnabled();
    }
}
