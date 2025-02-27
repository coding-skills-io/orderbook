package orderbook.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.*;
import orderbook.Main;
import orderbook.handler.*;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.Map;

public class NettyServer {

    private static final Logger log = LoggerFactory.getLogger(NettyServer.class);


    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
    }

    private static Map<RequestPath, PathHandler> PATH_HANDLERS = Map.of(
            new RequestPath("/health", HttpMethod.GET), new HealthPathHandler(),
            new RequestPath("/orders", HttpMethod.POST), new CreateOrderPathHandler(OBJECT_MAPPER),
            new RequestPath("/orders", HttpMethod.GET), new GetOrderPathHandler(OBJECT_MAPPER)
    );

    private static PathHandler DEFAULT_HANDLER = new NotFoundPathHandler();

    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;
    private Channel serverChannel;

    public NettyServer() {
        this.bossGroup = new NioEventLoopGroup(1);
        this.workerGroup = new NioEventLoopGroup();
    }

    public void start(final int port) throws Exception {
        var bootstrap = new ServerBootstrap();

        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        var pipeline = ch.pipeline();

                        pipeline.addLast(new HttpServerCodec());
                        pipeline.addLast(new HttpObjectAggregator(65536));
                        pipeline.addLast(new PathRoutingHandler());
                    }
                });

        serverChannel = bootstrap.bind(port).sync().channel();

        log.info("Server started on port {}", port);
    }

    public void stop() throws Exception {
        serverChannel.close().sync();
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

    static class PathRoutingHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
        @Override
        protected void channelRead0(final ChannelHandlerContext ctx, final FullHttpRequest request) {
            var handler = PATH_HANDLERS.entrySet()
                    .stream()
                    .filter(entry -> entry.getKey().equals(new RequestPath(request.uri(), request.method())))
                    .map(Map.Entry::getValue)
                    .findFirst()
                    .orElse(DEFAULT_HANDLER);

            handler.executeRequest(ctx, request);
        }
    }
}
