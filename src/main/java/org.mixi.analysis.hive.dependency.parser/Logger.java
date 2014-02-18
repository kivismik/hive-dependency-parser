package org.mixi.analysis.hive.dependency.parser;

/**
 * Created by hikaru.ojima on 2014/02/18.
 */
public class Logger {
    public void error(String message) {
        System.err.println(message);
    }

    public void error(String message, Exception e) {
        error(message);
        e.printStackTrace(System.err);
    }
}
