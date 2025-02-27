package orderbook.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import orderbook.model.OrderRequest;
import orderbook.model.OrderResponseBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.*;

import static orderbook.h2.H2Database.JDBC_URL;

public class CreateOrderPathHandler implements PathHandler {

    private static final Logger log = LoggerFactory.getLogger(CreateOrderPathHandler.class);


    private final ObjectMapper objectMapper;

    public CreateOrderPathHandler(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void executeRequest(final ChannelHandlerContext ctx, final FullHttpRequest request) {
        OrderRequest orderRequest;
        try {
            // Parse the incoming JSON into an OrderRequest record
            orderRequest = objectMapper.readValue(
                    request.content().toString(CharsetUtil.UTF_8),
                    OrderRequest.class
            );

            log.info("Received order request: {}", orderRequest);
        } catch (Exception e) {
            sendJsonResponse(ctx, request, HttpResponseStatus.BAD_REQUEST,
                    "{\"error\":\"Invalid JSON format\"}");
            return;
        }

        String query;
        if ("BUY".equalsIgnoreCase(orderRequest.type())) {
            query = "SELECT * FROM orders WHERE order_type = 'SELL' AND quantity = ? ORDER BY price ASC, created_at ASC LIMIT 1";
        } else if ("SELL".equalsIgnoreCase(orderRequest.type())) {
            query = "SELECT * FROM orders WHERE order_type = 'BUY' AND quantity = ? ORDER BY price DESC, created_at ASC LIMIT 1";
        } else {
            sendJsonResponse(ctx, request, HttpResponseStatus.BAD_REQUEST,
                    "{\"error\":\"Invalid order type\"}");
            return;
        }

        try (Connection conn = DriverManager.getConnection(JDBC_URL, "sa", "")) {
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setInt(1, orderRequest.quantity().intValue());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        // Matching order found.
                        int matchedOrderId = rs.getInt("id");
                        BigDecimal matchedPrice = rs.getBigDecimal("price");

                        // Simulate execution by deleting the matched order from the order book.
                        String deleteQuery = "DELETE FROM orders WHERE id = ?";
                        try (PreparedStatement deletePs = conn.prepareStatement(deleteQuery)) {
                            deletePs.setInt(1, matchedOrderId);
                            deletePs.executeUpdate();
                        }

                        var orderResponse = OrderResponseBuilder.builder()
                                .type("exe_report")
                                .initialQuantity(orderRequest.quantity())
                                .executedPrice(matchedPrice)
                                .executedQuantity(orderRequest.quantity())
                                .accountId(orderRequest.accountId())
                                .status("FILLED")
                                .build();

                        var jsonResponse = objectMapper.writeValueAsString(orderResponse);
                        sendJsonResponse(ctx, request, HttpResponseStatus.OK, jsonResponse);
                    } else {
                        var orderResponse = OrderResponseBuilder.builder()
                                .type("exe_report")
                                .initialQuantity(orderRequest.quantity())
                                .accountId(orderRequest.accountId())
                                .status("REJECTED")
                                .build();
                        var jsonResponse = objectMapper.writeValueAsString(orderResponse);
                        sendJsonResponse(ctx, request, HttpResponseStatus.OK, jsonResponse);
                    }
                }
            }
        } catch (SQLException | JsonProcessingException e) {
            sendJsonResponse(ctx, request, HttpResponseStatus.INTERNAL_SERVER_ERROR,
                    "{\"error\":\"Database error: " + e.getMessage() + "\"}");
        }
    }

    private void sendJsonResponse(ChannelHandlerContext ctx, FullHttpRequest request, HttpResponseStatus status, String jsonBody) {
        var response = new DefaultFullHttpResponse(
                request.protocolVersion(),
                status,
                Unpooled.copiedBuffer(jsonBody, CharsetUtil.UTF_8)
        );
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8");
        response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
}
