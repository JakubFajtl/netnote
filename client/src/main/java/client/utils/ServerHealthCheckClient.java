package client.utils;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.URL;

public class ServerHealthCheckClient implements HealthCheckClient {
    public boolean isServerAvailable(String serverUrl) {
        try (Socket socket = new Socket()) {
            URL url = URI.create(serverUrl).toURL();
            if (url.getPath() != null && !url.getPath().isEmpty() && !url.getPath().equals("/")) {
                throw new IllegalArgumentException();
            }

            if (url.getQuery() != null) {
                throw new IllegalArgumentException();
            }

            socket.connect(new InetSocketAddress(url.getHost(), url.getPort()), 50);
            // If we successfully connect, the server exists
            return true;
        }
        // If any exception occurs, the server is unreachable
        catch (Exception e) {
            return false;
        }
    }
}
