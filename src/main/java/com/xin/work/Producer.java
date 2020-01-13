package com.xin.work;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.xin.util.ConnectionUtil;

import static com.xin.util.ConnectionUtil.WORK_EXCHANGE_NAME;
import static com.xin.util.ConnectionUtil.WORK_QUEUE_NAME;

/**
 * @author jack
 * @date 2020/1/10 11:03
 */
public class Producer {

    public static void main(String[] argv) throws Exception {
        // 获取到连接以及mq通道
        Connection connection = ConnectionUtil.getConnection();
        Channel channel = connection.createChannel();

        // 声明队列
        channel.queueDeclare(WORK_QUEUE_NAME, false, false, false, null);
        channel.exchangeDeclare(WORK_EXCHANGE_NAME, "direct");

        for (int i = 0; i < 100; i++) {
            // 消息内容
            String message = "" + i;
            channel.basicPublish(WORK_EXCHANGE_NAME, WORK_QUEUE_NAME, null, message.getBytes());
            System.out.println(" [x] Sent '" + message + "'");

            Thread.sleep(10);
        }

        channel.close();
        connection.close();
    }

}
