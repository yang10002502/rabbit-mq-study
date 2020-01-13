package com.xin.direct;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.xin.util.ConnectionUtil;

import static com.xin.util.ConnectionUtil.ROUTE_EXCHANGE_NAME;

/**
 * @author jack
 * @date 2020/1/10 11:03
 */
public class Producer {

    public static void main(String[] argv) throws Exception {
        // 获取到连接以及mq通道
        Connection connection = ConnectionUtil.getConnection();
        // 从连接中创建通道
        Channel channel = connection.createChannel();

        // 声明（创建）队列
        channel.exchangeDeclare(ROUTE_EXCHANGE_NAME, "direct");

        // 消息内容
        String message = "delete";
        channel.basicPublish(ROUTE_EXCHANGE_NAME, "delete", null, message.getBytes());
        System.out.println(" [x] Sent '" + message + "'");
        message = "insert";
        channel.basicPublish(ROUTE_EXCHANGE_NAME, "insert", null, message.getBytes());
        System.out.println(" [x] Sent '" + message + "'");
        //关闭通道和连接
        channel.close();
        connection.close();
    }

}
