package orderbook.handler;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;

public class NotFoundPathHandler implements PathHandler {

    @Override
    public void executeRequest(final ChannelHandlerContext ctx, final FullHttpRequest request) {
        var response = new DefaultFullHttpResponse(
                request.protocolVersion(),
                HttpResponseStatus.NOT_FOUND,
                Unpooled.wrappedBuffer("Not Found".getBytes())
        );
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8");
        response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
}
