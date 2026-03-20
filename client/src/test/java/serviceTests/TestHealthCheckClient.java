package serviceTests;

import client.utils.HealthCheckClient;

public class TestHealthCheckClient implements HealthCheckClient {
    @Override
    public boolean isServerAvailable(String serverUrl) {
        return true;
    }
}
