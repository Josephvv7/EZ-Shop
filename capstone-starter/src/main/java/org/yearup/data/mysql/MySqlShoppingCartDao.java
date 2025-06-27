package org.yearup.data.mysql;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.yearup.data.ShoppingCartDao;
import org.yearup.models.Product;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;

import javax.sql.DataSource;
import java.sql.*;

@Component
public class MySqlShoppingCartDao extends MySqlDaoBase implements ShoppingCartDao {

    @Autowired
    public MySqlShoppingCartDao(DataSource dataSource) {
                super(dataSource);
    }
    @Override
    public ShoppingCart getByUserId(int userId) {
        ShoppingCart cart = new ShoppingCart();

        String sql = "SELECT sc.quantity, p.* FROM shopping_cart sc " + "JOIN products p ON sc.product_id = p.product_id " + "WHERE sc.user_id = ?";

        try (Connection connection = getConnection()) {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ShoppingCartItem item = new ShoppingCartItem();
                Product product = MySqlProductDao.mapRow(rs);
                item.setProduct(product);
                item.setQuantity(rs.getInt("quantity"));
                cart.add(item);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return cart;
    }

    @Override
    public void addProduct(int userId, int productId) {
        String updateSql = "UPDATE shopping_cart SET quantity = quantity + 1 WHERE user_id = ? AND product_id = ?";
        String insertSql = "INSERT INTO shopping_cart (user_id, product_id, quantity) VALUES (?, ?, 1)";

        try (Connection connection = getConnection()) {
            PreparedStatement ps = connection.prepareStatement(updateSql);
            ps.setInt(1, userId);
            ps.setInt(2, productId);

            int rows = ps.executeUpdate();
            if (rows == 0) {
                ps = connection.prepareStatement(insertSql);
                ps.setInt(1, userId);
                ps.setInt(2, productId);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateQuantity(int userId, int productId, int quantity) {
    String sql = "UPDATE shopping_cart SET quantity = ? WHERE user_id = ? AND product_id = ?";
        try (Connection connection = getConnection()) {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, quantity);
            ps.setInt(2, userId);
            ps.setInt(3, productId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void clearCart(int userId) {
String sql = "DELETE FROM shopping_cart WHERE user_id = ?";

    try (Connection connection = getConnection()) {
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, userId);
        ps.executeUpdate();
            } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

