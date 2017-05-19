package liquibase.logging.core;

import liquibase.configuration.GlobalConfiguration;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.exception.InternalException;
import liquibase.logging.LogLevel;
import liquibase.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.util.Date;

public class DefaultLogger extends AbstractLogger {

    private String name = "liquibase";
    private PrintStream stderr = System.err;
    private PrintStream stdout = System.out;

    public DefaultLogger() {
    }

    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public LogLevel getLogLevel() {
        LogLevel logLevel = super.getLogLevel();

        if (logLevel == null) {
            return toLogLevel(LiquibaseConfiguration.getInstance().getConfiguration(DefaultLoggerConfiguration.class).getLogLevel());
        } else {
            return logLevel;
        }
    }

    @Override
    public void setLogLevel(String logLevel, String logFile) {
        setLogLevel(logLevel);
        if (logFile != null) {
            File log = new File(logFile);
            try {
                if (!log.exists()) {
                    if (!log.createNewFile()) {
                        throw new RuntimeException("Could not create logFile "+log.getAbsolutePath());
                    }
                }
                stderr = new PrintStream(log, LiquibaseConfiguration.getInstance().getConfiguration(GlobalConfiguration.class).getOutputEncoding());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void closeLogFile() {
        if (stderr.equals(System.err) || stderr.equals(System.out)) {
            return;
        }
        stderr.flush();
        stderr.close();
        stderr = System.err;
    }

    @Override
    public void severe(String message)  {
        if (getLogLevel().compareTo(LogLevel.SEVERE) <=0) {
            print(LogLevel.SEVERE, message);
        }
    }

    protected void print(LogLevel logLevel, String message) throws InternalException {
        if (StringUtils.trimToNull(message) == null) {
            return;
        }

        String outputString = String.format("[%s] %s",
                logLevel,
                DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new Date()) + ": " + name + ": " + buildMessage(message)
        );
        switch (logLevel) {
            case DEBUG:
                stdout.println(outputString);
                break;
            case INFO:
                stdout.println(outputString);
                break;
            case SEVERE:
                stderr.println(outputString);
                break;
            case WARNING:
                stderr.println(outputString);
                break;
            default:
                throw new InternalException("Encountered an unknown log level: " + logLevel.toString());
        }
    }

    @Override
    public void severe(String message, Throwable e) {
        if (getLogLevel().compareTo(LogLevel.SEVERE) <=0) {
            print(LogLevel.SEVERE, message);
            e.printStackTrace(stderr);
        }
    }

    @Override
    public void warning(String message) {
        if (getLogLevel().compareTo(LogLevel.WARNING) <=0) {
            print(LogLevel.WARNING, message);
        }
    }

    @Override
    public void warning(String message, Throwable e) {
        if (getLogLevel().compareTo(LogLevel.WARNING) <=0) {
            print(LogLevel.WARNING, message);
            e.printStackTrace(stderr);
        }
    }

    @Override
    public void info(String message) {
        if (getLogLevel().compareTo(LogLevel.INFO) <=0) {
            print(LogLevel.INFO, message);
        }
    }

    @Override
    public void info(String message, Throwable e) {
        if (getLogLevel().compareTo(LogLevel.INFO) <=0) {
            print(LogLevel.INFO, message);
            e.printStackTrace(stderr);
        }
    }

    @Override
    public void debug(String message) {
        if (getLogLevel().compareTo(LogLevel.DEBUG) <=0) {
            print(LogLevel.DEBUG, message);
        }
    }

    @Override
    public void debug(String message, Throwable e) {
        if (getLogLevel().compareTo(LogLevel.DEBUG) <=0) {
            print(LogLevel.DEBUG, message);
            e.printStackTrace(stderr);
        }

    }
}
