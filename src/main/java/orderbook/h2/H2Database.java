package orderbook.h2;

import java.math.BigDecimal;
import java.sql.*;

public class H2Database {

    public static final String JDBC_URL = "jdbc:h2:mem:orderbook;DB_CLOSE_DELAY=-1";

    public void setup() {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, "sa", "")) {

            // Create the order book table
            try (var stmt = conn.createStatement()) {
                var createTableSql = """
                        CREATE TABLE IF NOT EXISTS ORDERS (
                        id INT PRIMARY KEY AUTO_INCREMENT,
                        order_type VARCHAR(4) NOT NULL,
                        quantity INT NOT NULL,
                        price DECIMAL(10,2) NOT NULL,
                        account_id INT NOT NULL,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                        )
                        """;
                stmt.execute(createTableSql);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void insertDummyData() throws Exception {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, "sa", "")) {
            var insertSQL = "INSERT INTO orders (order_type, quantity, price, account_id) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertSQL)) {
                ps.setString(1, "BUY");
                ps.setInt(2, 130);
                ps.setBigDecimal(3, new BigDecimal("56.00"));
                ps.setInt(4, 1238);
                ps.executeUpdate();

                ps.setString(1, "BUY");
                ps.setInt(2, 100);
                ps.setBigDecimal(3, new BigDecimal("50.00"));
                ps.setInt(4, 1233);
                ps.executeUpdate();

                ps.setString(1, "BUY");
                ps.setInt(2, 200);
                ps.setBigDecimal(3, new BigDecimal("55.00"));
                ps.setInt(4, 1234);
                ps.executeUpdate();

                ps.setString(1, "BUY");
                ps.setInt(2, 150);
                ps.setBigDecimal(3, new BigDecimal("53.00"));
                ps.setInt(4, 1235);
                ps.executeUpdate();

                ps.setString(1, "BUY");
                ps.setInt(2, 120);
                ps.setBigDecimal(3, new BigDecimal("54.00"));
                ps.setInt(4, 1236);
                ps.executeUpdate();

                ps.setString(1, "BUY");
                ps.setInt(2, 110);
                ps.setBigDecimal(3, new BigDecimal("52.00"));
                ps.setInt(4, 1237);
                ps.executeUpdate();

                ps.setString(1, "BUY");
                ps.setInt(2, 130);
                ps.setBigDecimal(3, new BigDecimal("56.00"));
                ps.setInt(4, 1238);
                ps.executeUpdate();

                ps.setString(1, "SELL");
                ps.setInt(2, 130);
                ps.setBigDecimal(3, new BigDecimal("47.00"));
                ps.setInt(4, 5683);
                ps.executeUpdate();

                ps.setString(1, "SELL");
                ps.setInt(2, 90);
                ps.setBigDecimal(3, new BigDecimal("45.00"));
                ps.setInt(4, 5678);
                ps.executeUpdate();

                ps.setString(1, "SELL");
                ps.setInt(2, 80);
                ps.setBigDecimal(3, new BigDecimal("44.50"));
                ps.setInt(4, 5679);
                ps.executeUpdate();

                ps.setString(1, "SELL");
                ps.setInt(2, 100);
                ps.setBigDecimal(3, new BigDecimal("46.00"));
                ps.setInt(4, 5680);
                ps.executeUpdate();

                ps.setString(1, "SELL");
                ps.setInt(2, 110);
                ps.setBigDecimal(3, new BigDecimal("43.00"));
                ps.setInt(4, 5681);
                ps.executeUpdate();

                ps.setString(1, "SELL");
                ps.setInt(2, 120);
                ps.setBigDecimal(3, new BigDecimal("42.00"));
                ps.setInt(4, 5682);
                ps.executeUpdate();

                ps.setString(1, "SELL");
                ps.setInt(2, 130);
                ps.setBigDecimal(3, new BigDecimal("47.00"));
                ps.setInt(4, 5683);
                ps.executeUpdate();
            }
        }
    }

    public void clearDb() throws Exception {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, "sa", "")) {
            var deleteSql = "DELETE FROM ORDERS";

            try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
                ps.executeUpdate();
            }
        }
    }
}
