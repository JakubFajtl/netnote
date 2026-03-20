package client.utils;

public interface HealthCheckClient {
    boolean isServerAvailable(String serverUrl);
}
