package com.test.mybatis.config;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 老肥猪
 * @since 2019/3/7
 */
public class Configuration {
    private static ClassLoader loader = ClassLoader.getSystemClassLoader();

    /**
     * 资源读取以及构建
     *
     * @param resources
     * @return
     */
    public static Connection build(String resources) {
        try {
            InputStream inputStream = loader.getResourceAsStream(resources);
            SAXReader reader = new SAXReader();
            Document document = reader.read(inputStream);
            Element root = document.getRootElement();
            return evalDataSource(root);
        } catch (DocumentException e) {
            throw new RuntimeException("xml read error");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("not found driver");
        }
    }

    /**
     * 获取数据库的连接
     *  <database>
     *   <property name="driverClassName">com.mysql.jdbc.Driver</property>
     *   <property name="url">jdbc:mysql://localhost:3306/test?useUnicode=true&amp;characterEncoding=utf8&amp;tinyInt1isBit=false&amp;useSSL=false</property>
     *   <property name="username">root</property>
     *   <property name="password">88105156</property>
     *  </database>
     * @param node xml的节点
     * @return
     * @throws ClassNotFoundException
     */
    private static Connection evalDataSource(Element node) throws ClassNotFoundException {
        if (!node.getName().equals("database")) {
            throw new RuntimeException("root shuold be database");
        }
        String driver = null;
        String url = null;
        String username = null;
        String password = null;
        for (Object item : node.elements("property")) {
            Element element = (Element) item;
            String value = getValue(element);
            String name = element.attributeValue("name");
            if (name == null || value == null) {
                throw new RuntimeException("the database's value or database's name is not null");
            }
            switch (name) {
                case "url":
                    url = value;
                    break;
                case "username":
                    username = value;
                    break;
                case "driverClassName":
                    driver = value;
                    break;
                case "password":
                    password = value;
                    break;
                default:
                    throw new RuntimeException("not found name is " + name);
            }
        }
        Class.forName(driver);
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    /**
     * 获得值
     *
     * @param element
     * @return
     */
    private static String getValue(Element element) {
        /**
         * 中间是否有值，有的话去找中间的，没有的话去找属性为value的值
         */
        return element.hasContent() ? element.getText() : element.attributeValue("value");
    }

    /**
     * 读取mapper的xml
     *  <mapper  namespace="com.test.mybatis.mapper.UserMapper">
     *     <select id="getUserById" resultType ="com.test.mybatis.bean.User">
     *         select * from user where id = ? (element.getText())
     *     </select>
     *  </mapper>
     * @param path 位置
     * @return
     */
    public static MapperBean readMapper(String path) {
        MapperBean mapperBean = new MapperBean();
        /**
         * 获得读取流
         */
        InputStream stream = loader.getResourceAsStream(path);
        SAXReader reader = new SAXReader();
        Document document = null;
        try {
            /**
             * 读取xml文档
             */
            document = reader.read(stream);
        } catch (DocumentException e) {
            throw new RuntimeException("read xml error");
        }
        /**
         * 获得xml文档根元素
         */
        Element root = document.getRootElement();
        if (root == null) {
            throw new RuntimeException("not found root element");
        }
        if (!root.getName().equals("mapper")) {
            throw new RuntimeException("root element should be mapper");
        }
        String namespace = root.attributeValue("namespace");
        if (namespace == null || "".equals(namespace)) {
            throw new RuntimeException("root element should be mapper");
        }
        /**
         * 设置扫描的类
         */
        mapperBean.setNamespace(namespace);
        List<Function> list = new ArrayList<>();
        root.elements().forEach(o -> {
            Function function = new Function();
            Element element = (Element) o;
            //设置类别 update || select || delete
            function.setSqlType(element.getName().trim());
            String funcName = element.attributeValue("id");
            if (funcName == null || "".equals(funcName)) {
                throw new RuntimeException("id is not be null");
            }
            function.setFuncName(funcName.trim());
            String resultType = element.attributeValue("resultType");
            if (resultType == null || "".equals(resultType)) {
                throw new RuntimeException("resultType is not be null");
            }
            resultType = resultType.trim();
            if (!element.hasContent()) {
                throw new RuntimeException("element's mid is not be null");
            }
            /**
             * 获得中间的sql
             */
            String sql = element.getText();
            function.setSql(sql);
            Object instance = null;
            try {
                instance = Class.forName(resultType).newInstance();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
            function.setResultType(instance);
            list.add(function);
        });
        mapperBean.setList(list);
        return mapperBean;
    }
}
