package assessor.utils;

import java.io.*;
import java.util.logging.*;

public class AdvancedLogger {
    public static Logger getLogger(String className) {
        Logger logger = Logger.getLogger(className);

        // Avoid duplicate handlers
        if (logger.getHandlers().length > 0) {
            return logger;
        }

        try {
            // Ensure log directory exists
            File logDir = new File("logs");
            if (!logDir.exists()) {
                logDir.mkdirs();
            }

            // Set up a file handler
            FileHandler fileHandler = new FileHandler("logs/application.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            fileHandler.setLevel(Level.ALL);

            // Set up a console handler
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter(new SimpleFormatter());
            consoleHandler.setLevel(Level.ALL);

            // Add handlers
            logger.addHandler(fileHandler);
            logger.addHandler(consoleHandler);
            logger.setUseParentHandlers(false); // Disable default console handler
            logger.setLevel(Level.ALL);

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to initialize logger", e);
        }

        return logger;
    }
}