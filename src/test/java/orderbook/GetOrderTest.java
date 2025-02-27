package orderbook;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import orderbook.h2.H2Database;
import orderbook.model.OrderBook;
import orderbook.server.NettyServer;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static java.util.Collections.emptyList;
import static orderbook.TestFixtures.insertOrder;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GetOrderTest {

    private HttpClient httpClient;

    private NettyServer server;

    private H2Database db;

    private ObjectMapper mapper;

    @BeforeAll
    void beforeAll() throws Exception {
        httpClient = HttpClient.newHttpClient();

        server = new NettyServer();
        server.start(8080);

        db = new H2Database();
        db.setup();

        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
    }

    @AfterAll()
    void afterAll() throws Exception {
        server.stop();
    }

    @AfterEach
    void afterEach() throws Exception {
        db.clearDb();
    }

    @Test
    @DisplayName("should return empty json")
    void noData() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/orders"))
                .header("Content-Type", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode()).isEqualTo(200);

        List<OrderBook> orderBooks = mapper.readValue(response.body(), new TypeReference<List<OrderBook>>() {});
        assertThat(orderBooks).isEqualTo(emptyList());
    }

    @Test
    @DisplayName("should return order book from db")
    void orderBookFromDb() throws Exception {
        insertOrder("BUY", 10, new BigDecimal("100.00"), 123);
        insertOrder("SELL", 20, new BigDecimal("200.00"), 456);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/orders"))
                .header("Content-Type", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode()).isEqualTo(200);

        List<OrderBook> orderBooks = mapper.readValue(response.body(), new TypeReference<List<OrderBook>>() {});
        assertThat(orderBooks.size()).isEqualTo(2);
    }

}
