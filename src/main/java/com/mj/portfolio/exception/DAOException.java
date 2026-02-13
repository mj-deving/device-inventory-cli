package com.mj.portfolio.exception;

/**
 * Unchecked wrapper for SQL/database errors in the DAO layer.
 *
 * <p>Using a RuntimeException here follows the Spring convention: database
 * failures are rarely recoverable by the caller, so forcing every call site
 * to catch a checked exception adds noise without real benefit.</p>
 */
public class DAOException extends RuntimeException {

    public DAOException(String message) {
        super(message);
    }

    public DAOException(String message, Throwable cause) {
        super(message, cause);
    }
}
