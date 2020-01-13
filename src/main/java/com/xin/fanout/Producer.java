package com.xin.fanout;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.xin.util.ConnectionUtil;

import static com.xin.util.ConnectionUtil.FANOUT_EXCHANGE_NAME;

/**
 * @author jack
 * @date 2020/1/10 11:03
 * @Des 扇形交换机实现广播
 */
public class Producer {

    public static void main(String[] argv) throws Exception {
        // 获取到连接以及mq通道
        Connection connection = ConnectionUtil.getConnection();
        // 从连接中创建通道
        Channel channel = connection.createChannel();

        // 绑定交换机
        channel.exchangeDeclare(FANOUT_EXCHANGE_NAME, "fanout");

        // 消息内容
        String message = "Hello World!";
        // 将消息发送至交换机
        channel.basicPublish(FANOUT_EXCHANGE_NAME, "", null, message.getBytes());
        System.out.println(" [x] Sent '" + message + "'");
        //关闭通道和连接
        channel.close();
        connection.close();
    }

}

/**
 *  扇型交换机（funout exchange）将消息路由给绑定到它身上的所有队列，而不理会绑定的路由键。
 *  如果 N 个队列绑定到某个扇型交换机上，当有消息发送给此扇型交换机时，
 *  交换机会将消息的拷贝分别发送给这所有的 N 个队列。
 *  扇型用来交换机处理消息的广播路由（broadcast routing）。
 *
 *  因为扇型交换机投递消息的拷贝到所有绑定到它的队列，所以他的应用案例都极其相似：
 *  1、大规模多用户在线（MMO）游戏可以使用它来处理排行榜更新等全局事件
 *  2、体育新闻网站可以用它来近乎实时地将比分更新分发给移动客户端
 *  3、分发系统使用它来广播各种状态和配置更新
 *  4、在群聊的时候，它被用来分发消息给参与群聊的用户
 */
