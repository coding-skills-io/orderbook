package orderbook.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import orderbook.model.OrderBook;

import java.math.BigDecimal;
import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static orderbook.h2.H2Database.JDBC_URL;

public class GetOrderPathHandler implements PathHandler {

    private final ObjectMapper objectMapper;

    public GetOrderPathHandler(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void executeRequest(final ChannelHandlerContext ctx, final FullHttpRequest request) {
        List<OrderBook> orderBooks = new ArrayList<>();
        String query = "SELECT order_type, quantity, price, created_at FROM orders";

        try (Connection conn = DriverManager.getConnection(JDBC_URL, "sa", "");
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String type = rs.getString("order_type");
                BigDecimal quantity = rs.getBigDecimal("quantity");
                BigDecimal price = rs.getBigDecimal("price");
                Instant createdAt = rs.getTimestamp("created_at").toInstant();
                orderBooks.add(new OrderBook(type, quantity, price, createdAt));
            }

            var json = objectMapper.writeValueAsString(orderBooks);
            var response = new DefaultFullHttpResponse(
                    request.protocolVersion(),
                    HttpResponseStatus.OK,
                    Unpooled.copiedBuffer(json, CharsetUtil.UTF_8)
            );
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8");
            response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        } catch (SQLException | JsonProcessingException e) {
            throw new RuntimeException("Error retrieving order book data and parsing to json", e);
        }
    }
}
