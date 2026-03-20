package client.exceptions;

// This exception is for problems that happen in the config file
public class ConfigException extends RuntimeException {
    public ConfigException(String message) {
        super(message);
    }
}
