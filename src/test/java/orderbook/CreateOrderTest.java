package orderbook;

import com.fasterxml.jackson.databind.ObjectMapper;
import orderbook.h2.H2Database;
import orderbook.model.OrderResponse;
import orderbook.server.NettyServer;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static orderbook.TestFixtures.insertOrder;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CreateOrderTest {

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
    @DisplayName("should return filled sell order")
    void testSellFilledOrder() throws Exception {
        insertOrder("BUY", 10, new BigDecimal("100.00"), 123);

        // Build the SELL order request (market order) JSON.
        String orderJson = """
            {
              "type": "SELL",
              "quantity": 10,
              "accountId": 123
            }
            """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/orders"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(orderJson))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode()).isEqualTo(200);

        OrderResponse orderResponse = mapper.readValue(response.body(), OrderResponse.class);
        assertThat(orderResponse.status()).isEqualTo("FILLED");
        assertThat(orderResponse.initialQuantity()).isEqualByComparingTo(new BigDecimal("10"));
        assertThat(orderResponse.executedPrice()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(orderResponse.executedQuantity()).isEqualByComparingTo(new BigDecimal("10"));
        assertThat(orderResponse.accountId()).isEqualTo(123);
    }

    @Test
    @DisplayName("should return rejected sell order")
    void testSellRejectedOrder() throws Exception {
        // Do NOT insert any BUY order so that the SELL order will be rejected.
        String orderJson = """
            {
              "type": "SELL",
              "quantity": 10,
              "accountId": 123
            }
            """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/orders"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(orderJson))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode()).isEqualTo(200);

        OrderResponse orderResponse = mapper.readValue(response.body(), OrderResponse.class);

        assertThat(orderResponse.status()).isEqualTo("REJECTED");
        assertThat(orderResponse.initialQuantity()).isEqualByComparingTo(new BigDecimal("10"));
        assertThat(orderResponse.executedPrice()).isNull();
        assertThat(orderResponse.executedQuantity()).isNull();
        assertThat(orderResponse.accountId()).isEqualTo(123);
    }

    @Test
    @DisplayName("should return filled buy order")
    void testBuyFilledOrder() throws Exception {
        insertOrder("SELL", 10, new BigDecimal("90.00"), 123);

        String orderJson = """
            {
              "type": "BUY",
              "quantity": 10,
              "accountId": 234
            }
            """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/orders"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(orderJson))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode()).isEqualTo(200);

        OrderResponse orderResponse = mapper.readValue(response.body(), OrderResponse.class);
        assertThat(orderResponse.status()).isEqualTo("FILLED");
        assertThat(orderResponse.initialQuantity()).isEqualByComparingTo(new BigDecimal("10"));
        assertThat(orderResponse.executedPrice()).isEqualByComparingTo(new BigDecimal("90.00"));
        assertThat(orderResponse.executedQuantity()).isEqualByComparingTo(new BigDecimal("10"));
        assertThat(orderResponse.accountId()).isEqualTo(234);
    }

    @Test
    @DisplayName("should return rejected buy order")
    void testBuyRejectedOrder() throws Exception {
        String orderJson = """
            {
              "type": "BUY",
              "quantity": 10,
              "accountId": 234
            }
            """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/orders"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(orderJson))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode()).isEqualTo(200);

        OrderResponse orderResponse = mapper.readValue(response.body(), OrderResponse.class);
        // Expect the BUY order to be rejected since no matching SELL order is present.
        assertThat(orderResponse.status()).isEqualTo("REJECTED");
        assertThat(orderResponse.initialQuantity()).isEqualByComparingTo(new BigDecimal("10"));
        assertThat(orderResponse.executedPrice()).isNull();
        assertThat(orderResponse.executedQuantity()).isNull();
        assertThat(orderResponse.accountId()).isEqualTo(234);
    }

}
