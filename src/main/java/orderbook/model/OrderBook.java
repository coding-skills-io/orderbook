package orderbook.model;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderBook(String type, BigDecimal quantity, BigDecimal price, Instant createdAt) {
}
