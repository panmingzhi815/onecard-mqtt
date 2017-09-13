package org.pan.callback;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.sun.net.httpserver.HttpExchange;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.pan.MQTTServer;
import org.pan.message.MessageDirection;
import org.pan.message.MqttDongluMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

public class PushServerCallback implements MqttCallback {


    private static Logger LOGGER = LoggerFactory.getLogger(PushServerCallback.class);
    public static final Cache cache = CacheBuilder.newBuilder().expireAfterWrite(10L, TimeUnit.SECONDS).maximumSize(1000L).build();
    private final MQTTServer mqttServer;

    public PushServerCallback(MQTTServer mqttServer) {
        this.mqttServer = mqttServer;
    }

    public void connectionLost(Throwable cause) {
        LOGGER.info("正在重连服务器");
        mqttServer.reConnect();
    }

    public void deliveryComplete(IMqttDeliveryToken imqttdeliverytoken) {
    }

    public void messageArrived(String topic, MqttMessage message)
            throws Exception {
        MqttDongluMessage msg = JSONObject.parseObject(message.getPayload(), MqttDongluMessage.class, new Feature[0]);
        if (msg.getDirection() != MessageDirection.toServer) {
            return;
        }

        try {
            LOGGER.info("接收消息主题 : {},接收消息 Qos : {}, 接收消息内容 : {}", topic, message.getQos(), new String(message.getPayload()));
            HttpExchange ifPresent = (HttpExchange) cache.getIfPresent(msg.getId());
            if (ifPresent != null) {
                byte bytes[] = msg.getData().getBytes();
                ifPresent.sendResponseHeaders(bytes.length <= 0 ? 500 : 200, bytes.length);
                OutputStream responseBody = ifPresent.getResponseBody();
                responseBody.write(bytes);
                responseBody.flush();
                cache.invalidate(msg.getId());
            }
        } catch (IOException e) {
            LOGGER.error("服务器接收消息失败", e);
        }
    }


}
