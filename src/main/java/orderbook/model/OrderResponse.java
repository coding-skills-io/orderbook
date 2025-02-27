package orderbook.model;

import io.soabase.recordbuilder.core.RecordBuilder;

import java.math.BigDecimal;

@RecordBuilder
public record OrderResponse(String type, BigDecimal initialQuantity, BigDecimal executedPrice, BigDecimal executedQuantity, int accountId, String status) {
}
