package com.xin.topic;

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
    private String topicQueueName;

    public Consumer(String routeKey, String topicQueueName) {
        this.routeKey = routeKey;
        this.topicQueueName = topicQueueName;
    }

    public void receive() throws Exception {
        // 获取到连接以及mq通道
        Connection connection = ConnectionUtil.getConnection();
        // 从连接中创建通道
        Channel channel = connection.createChannel();
        // 声明队列
        channel.queueDeclare(topicQueueName, false, false, false, null);

        /**
         * 绑定队列到交换机上
         * @param topicQueueName the name of the queue
         * @param TOPIC_EXCHANGE_NAME the name of the exchange
         * @param name the routine key to use for the binding
         */
        channel.queueBind(topicQueueName, TOPIC_EXCHANGE_NAME, routeKey);
        //同一时候指挥发一条消息给消费者
        channel.basicQos(1);

        // 定义队列的消费者
        QueueingConsumer consumer = new QueueingConsumer(channel);

        // 监听队列,完成手动返回
        channel.basicConsume(topicQueueName, false, consumer);

        new Thread(new Runnable() {
            @Override
            public void run() {
                // 获取消息
                while (true) {
                    try {
                        QueueingConsumer.Delivery delivery = consumer.nextDelivery();
                        String message = new String(delivery.getBody());
                        System.out.println(topicQueueName + " Received '" + message + "'");
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
//        Consumer consumer1 = new Consumer("queue1.1", TOPIC_QUEUE_NAME_1);
//        Consumer consumer2 = new Consumer("queue2.2", TOPIC_QUEUE_NAME_2);
        Consumer consumer1 = new Consumer("queue1.*", TOPIC_QUEUE_NAME_1);
        Consumer consumer2 = new Consumer("*.2", TOPIC_QUEUE_NAME_2);
        consumer1.receive();
        consumer2.receive();
        // 使用CountDownLatch来等待子线程执行完成
        CountDownLatch latch = new CountDownLatch(1);
        latch.await();
    }
}
