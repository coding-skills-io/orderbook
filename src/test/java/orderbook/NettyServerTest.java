package orderbook;

import orderbook.server.NettyServer;
import org.junit.jupiter.api.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class NettyServerTest {

    private HttpClient httpClient;

    private NettyServer server;

    @BeforeAll
    void beforeAll() throws Exception {
        httpClient = HttpClient.newHttpClient();

        server = new NettyServer();
        server.start(8080);
    }

    @AfterAll()
    void afterAll() throws Exception {
        server.stop();
    }

    @Test
    @DisplayName("should return 200 - health check passes")
    void testServer() throws Exception {
        var request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/health"))
                .GET()
                .build();

        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode()).isEqualTo(200);
    }

    @Test
    @DisplayName("should return not found: 404")
    void testNotFound() throws Exception {
        var request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/anythingThatDoesNotExist"))
                .GET()
                .build();

        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode()).isEqualTo(404);
    }

}
