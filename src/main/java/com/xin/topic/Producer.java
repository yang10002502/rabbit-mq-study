package com.xin.topic;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.xin.util.ConnectionUtil;

import static com.xin.util.ConnectionUtil.TOPIC_EXCHANGE_NAME;

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
        channel.exchangeDeclare(TOPIC_EXCHANGE_NAME, "topic");

        // 消息内容
        for (int i = 0; i < 50; i++) {
            String key = "queue1.1";
            channel.basicPublish(TOPIC_EXCHANGE_NAME, key, null, key.getBytes());
            key = "queue2.2";
            channel.basicPublish(TOPIC_EXCHANGE_NAME, key, null, key.getBytes());
        }

        //关闭通道和连接
        channel.close();
        connection.close();
    }

}
