package orderbook.model;

import java.math.BigDecimal;

public record OrderRequest(String type, BigDecimal quantity, int accountId) {
}
