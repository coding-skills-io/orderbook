package orderbook;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static orderbook.h2.H2Database.JDBC_URL;

public class TestFixtures {

    static void insertOrder(String orderType, int quantity, BigDecimal price, int accountId) {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, "sa", "")) {
            String insertSQL = "INSERT INTO orders (order_type, quantity, price, account_id) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertSQL)) {
                ps.setString(1, orderType);
                ps.setInt(2, quantity);
                ps.setBigDecimal(3, price);
                ps.setInt(4, accountId);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error inserting order", e);
        }
    }

}
