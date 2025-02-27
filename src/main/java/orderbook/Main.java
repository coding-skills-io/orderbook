package orderbook;

import orderbook.h2.H2Database;
import orderbook.server.NettyServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        var db = new H2Database();
        db.setup();
        db.insertDummyData();

        var server = new NettyServer();

        log.info("Starting server on port 8080...");
        server.start(8080);
    }
}