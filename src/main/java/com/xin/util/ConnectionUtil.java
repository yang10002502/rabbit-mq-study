package com.xin.util;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * @author jack
 * @date 2020/1/10 10:53
 */
public class ConnectionUtil {
    public static String SIMPLE_QUEUE_NAME = "test_queue_simple";
    public static String WORK_QUEUE_NAME = "test_queue_work";
    public static String WORK_EXCHANGE_NAME = "test_exchange_work";
    public static String FANOUT_EXCHANGE_NAME = "test_exchange_fanout";
    public static String FANOUT_QUEUE_NAME1 = "test_queue_fanout1";
    public static String FANOUT_QUEUE_NAME2 = "test_queue_fanout2";
    public static String ROUTE_QUEUE_NAME_DELETE = "test_queue_route_delete";
    public static String ROUTE_QUEUE_NAME_INSERT = "test_queue_route_insert";
    public static String ROUTE_EXCHANGE_NAME = "test_exchange_route";
    public static String TOPIC_EXCHANGE_NAME = "test_exchange_topic";
    public static String TOPIC_QUEUE_NAME_1 = "test_queue_topic_1";
    public static String TOPIC_QUEUE_NAME_2 = "test_queue_topic_2";

    public static Connection getConnection() throws Exception {
        //定义连接工厂
        ConnectionFactory factory = new ConnectionFactory();
        //设置服务地址
        factory.setHost("localhost");
        //端口
        factory.setPort(5672);
        //设置账号信息，用户名、密码、vhost
        factory.setVirtualHost("/developer");
        factory.setUsername("admin");
        factory.setPassword("admin123");
        // 通过工程获取连接
        Connection connection = factory.newConnection();
        return connection;
    }
}
