import com.test.mybatis.pool.DatabasePool;
import org.junit.Test;

import java.sql.*;

/**
 * @author 老肥猪
 * @since 2019/3/7
 */
public class DemoTest {
    @Test
    public void testConnDatabase() throws InterruptedException, SQLException {
        Connection connection = DatabasePool.getDatabasePool().getConnection();
        PreparedStatement pre = connection.prepareCall(" \n" +
                "       select * from user where id = ?\n" +
                "   ");
        pre.setString(1,"1");
        ResultSet rs=pre.executeQuery();
        rs.next();
        System.out.println(rs.getObject(1));
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        System.out.println(columnCount);
        for (int i = 0; i < columnCount; i++) {
            String columnName = metaData.getColumnName(i+1);
            System.out.println(columnName);
        }
        System.out.println(rs);
        rs.close();
        pre.close();
        DatabasePool.getDatabasePool().recycle(connection);
    }
}
