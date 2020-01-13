package com.xin.fanout;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.QueueingConsumer;
import com.xin.util.ConnectionUtil;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import static com.xin.util.ConnectionUtil.*;

/**
 * @author jack
 * @date 2020/1/10 11:06
 */
public class Consumer {
    private String name;

    public Consumer(String name) {
        this.name = name;
    }

    public void receive() throws Exception {
        // 获取到连接以及mq通道
        Connection connection = ConnectionUtil.getConnection();
        // 从连接中创建通道
        Channel channel = connection.createChannel();
        // 声明队列
        channel.queueDeclare(name, false, false, false, null);

        //绑定队列到交换机
        channel.queueBind(name, FANOUT_EXCHANGE_NAME, "");
        //同一时候指挥发一条消息给消费者
        channel.basicQos(1);

        // 定义队列的消费者
        QueueingConsumer consumer = new QueueingConsumer(channel);

        // 监听队列,完成手动返回
        channel.basicConsume(name, false, consumer);

        new Thread(new Runnable() {
            @Override
            public void run() {
                // 获取消息
                while (true) {
                    try {
                        QueueingConsumer.Delivery delivery = consumer.nextDelivery();
                        String message = new String(delivery.getBody());
                        System.out.println(name + " Received '" + message + "'");
                        Thread.sleep(10);
                        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();


    }

    public static void main(String[] argv) throws Exception {
        Consumer consumer1 = new Consumer(FANOUT_QUEUE_NAME1);
        consumer1.receive();
        Consumer consumer2 = new Consumer(FANOUT_QUEUE_NAME2);
        consumer2.receive();
        // 使用CountDownLatch来等待子线程执行完成
        CountDownLatch latch = new CountDownLatch(1);
        latch.await();
    }
}
