package orderbook.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

public interface PathHandler {

    void executeRequest(ChannelHandlerContext ctx, FullHttpRequest request);

}
