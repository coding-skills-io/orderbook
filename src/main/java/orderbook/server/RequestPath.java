package orderbook.server;

import io.netty.handler.codec.http.HttpMethod;

public record RequestPath(String path, HttpMethod method) {
}
