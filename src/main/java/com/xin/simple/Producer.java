package com.xin.simple;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.xin.util.ConnectionUtil;

import static com.xin.util.ConnectionUtil.SIMPLE_QUEUE_NAME;

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
        channel.queueDeclare(SIMPLE_QUEUE_NAME, false, false, false, null);
        // 绑定到默认交换机上,因此下面这条语句可注释
//        channel.exchangeDeclare(SIMPLE_QUEUE_NAME, "direct");

        // 消息内容
        String message = "Hello World!";
        channel.basicPublish("", SIMPLE_QUEUE_NAME, null, message.getBytes());
        System.out.println(" [x] Sent '" + message + "'");
        //关闭通道和连接
        channel.close();
        connection.close();
    }

}
/**
 * 默认交换机（default exchange）实际上是一个由消息代理预先声明好的没有名字（名字为空字符串）的直连交换机（direct exchange）
 * 它有一个特殊的属性使得它对于简单应用特别有用处：那就是每个新建队列（queue）都会自动绑定到默认交换机上，绑定的路由键（routing key）名称与队列名称相同
 *
 * 举个例子：当你声明了一个名为 “search-indexing-online” 的队列，AMQP 代理会自动将其绑定到默认交换机上，
 * 绑定（binding）的路由键名称也是为 “search-indexing-online”。
 * 因此，当携带着名为 “search-indexing-online” 的路由键的消息被发送到默认交换机的时候，
 * 此消息会被默认交换机路由至名为 “search-indexing-online” 的队列中。
 * 换句话说，默认交换机看起来貌似能够直接将消息投递给队列，尽管技术上并没有做相关的操作
 */
