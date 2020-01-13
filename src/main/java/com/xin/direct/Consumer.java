package com.xin.direct;

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
    private String routeKey;
    private String queueName;

    public Consumer(String routeKey, String queueName) {
        this.routeKey = routeKey;
        this.queueName = queueName;
        System.out.println("routKey : " + routeKey + "\nqueueName : " + queueName);
    }

    public void receive() throws Exception {
        // 获取到连接以及mq通道
        Connection connection = ConnectionUtil.getConnection();
        // 从连接中创建通道
        Channel channel = connection.createChannel();
        // 声明队列
        channel.queueDeclare(queueName, false, false, false, null);

        //绑定队列到交换机
        /**
         * @param queueName the name of the queue
         * @param ROUTE_EXCHANGE_NAME the name of the exchange
         * @param routeKey the routine key to use for the binding
         */
        channel.queueBind(queueName, ROUTE_EXCHANGE_NAME, routeKey);
        //同一时候指挥发一条消息给消费者
        channel.basicQos(1);

        // 定义队列的消费者
        QueueingConsumer consumer = new QueueingConsumer(channel);

        // 监听队列,完成手动返回
        channel.basicConsume(queueName, false, consumer);

        new Thread(new Runnable() {
            @Override
            public void run() {
                // 获取消息
                while (true) {
                    try {
                        QueueingConsumer.Delivery delivery = consumer.nextDelivery();
                        String message = new String(delivery.getBody());
                        System.out.println(queueName + " Received '" + message + "'");
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
        Consumer consumer1 = new Consumer("delete", ROUTE_QUEUE_NAME_DELETE);
        Consumer consumer2 = new Consumer("insert", ROUTE_QUEUE_NAME_INSERT);
        consumer1.receive();
        consumer2.receive();
        // 使用CountDownLatch来等待子线程执行完成
        CountDownLatch latch = new CountDownLatch(1);
        latch.await();
    }
}
