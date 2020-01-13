package com.xin.work;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.QueueingConsumer;
import com.xin.util.ConnectionUtil;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import static com.xin.util.ConnectionUtil.WORK_QUEUE_NAME;

/**
 * @author jack
 * @date 2020/1/10 11:06
 * <p>
 * 使用公平分发，必须关闭自动应答，改为手动应答。
 */
public class Consumer {
    private String name;

    public Consumer(String name) {
        this.name = name;
    }

    public void receive() throws Exception {
        // 获取到连接以及mq通道
        Connection connection = ConnectionUtil.getConnection();
        Channel channel = connection.createChannel();

        // 声明队列
        channel.queueDeclare(WORK_QUEUE_NAME, false, false, false, null);

        // 同一时刻服务器只会发一条消息给消费者
        channel.basicQos(1);

        // 定义队列的消费者
        QueueingConsumer consumer = new QueueingConsumer(channel);
        // 监听队列，false表示手动返回完成状态，true表示自动
//        channel.basicConsume(WORK_QUEUE_NAME, true, consumer);
        channel.basicConsume(WORK_QUEUE_NAME, false, consumer);
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
                        // 返回确认状态，注释掉表示使用自动确认模式
                        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                    } catch (InterruptedException | IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        }).start();

    }

    public static void main(String[] argv) throws Exception {
        Consumer consumer1 = new Consumer("consumer1");
        Consumer consumer2 = new Consumer("consumer2");
        consumer1.receive();
        consumer2.receive();
        // 使用CountDownLatch来等待子线程执行完成
        CountDownLatch latch = new CountDownLatch(1);
        latch.await();
    }
}

/**
 * 测试结果：
 * 1、消费者1和消费者2获取到的消息内容是不同的，同一个消息只能被一个消费者获取。
 * 2、消费者1和消费者2获取到的消息的数量是相同的，一个是消费奇数号消息，一个是偶数。
 *
 * 其实，这样是不合理的，因为消费者1线程停顿的时间短。应该是消费者1要比消费者2获取到的消息多才对。
 * RabbitMQ 默认将消息顺序发送给下一个消费者，这样，每个消费者会得到相同数量的消息。即轮询（round-robin）分发消息。
 *
 * 怎样才能做到按照每个消费者的能力分配消息呢？联合使用 Qos 和 Acknowledge 就可以做到。
 * basicQos 方法设置了当前信道最大预获取（prefetch）消息数量为1。消息从队列异步推送给消费者，消费者的 ack 也是异步发送给队列，
 * 从队列的视角去看，总是会有一批消息已推送但尚未获得 ack 确认，Qos 的 prefetchCount 参数就是用来限制这批未确认消息数量的。
 * 设为1时，队列只有在收到消费者发回的上一条消息 ack 确认后，才会向该消费者发送下一条消息。prefetchCount 的默认值为0，
 * 即没有限制，队列会将所有消息尽快发给消费者。
 *
 * 2个概念
 * 轮询分发 ：使用任务队列的优点之一就是可以轻易的并行工作。如果我们积压了好多工作，我们可以通过增加工作者（消费者）来解决这一问题，
 *           使得系统的伸缩性更加容易。在默认情况下，RabbitMQ将逐个发送消息到在序列中的下一个消费者(而不考虑每个任务的时长等等，
 *           且是提前一次性分配，并非一个一个分配)。平均每个消费者获得相同数量的消息。这种方式分发消息机制称为Round-Robin（轮询）。
 * 公平分发 ：虽然上面的分配法方式也还行，但是有个问题就是：比如：现在有2个消费者，所有的奇数的消息都是繁忙的，而偶数则是轻松的。
 *           按照轮询的方式，奇数的任务交给了第一个消费者，所以一直在忙个不停。偶数的任务交给另一个消费者，则立即完成任务，然后闲得不行。
 *           而RabbitMQ则是不了解这些的。这是因为当消息进入队列，RabbitMQ就会分派消息。
 *           它不看消费者为应答的数目，只是盲目的将消息发给轮询指定的消费者。
 *
 * 为了解决这个问题，我们使用basicQos( prefetchCount = 1)方法，来限制RabbitMQ只发不超过1条的消息给同一个消费者。当消息处理完毕后，有了反馈，才会进行第二次发送。
 * 还有一点需要注意，使用公平分发，必须关闭自动应答，改为手动应答。
 */
