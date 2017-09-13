package org.pan;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.pan.callback.PushClientCallback;
import org.pan.message.MqttDongluMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MQTTClient {

    private static Logger LOGGER = LoggerFactory.getLogger(MQTTClient.class);
    private static final String HOST = "tcp://120.192.167.84:1883";
    private static final String TOPIC = "onecard_topic";
    private MqttClient client;
    private MqttConnectOptions options;
    private MqttTopic topic;

    public MQTTClient() {
    }

    public void start() {
        try {
            client = new MqttClient(HOST, TOPIC, new MemoryPersistence());
            options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setConnectionTimeout(10);
            options.setKeepAliveInterval(20);
            client.setCallback(new PushClientCallback(this));
            client.connect(options);
            client.subscribe(new String[]{ "onecard_topic" }, new int[]{ 1 });
            topic = client.getTopic("onecard_topic");
            LOGGER.info("启动一卡通mqtt转发客户端成功");
        } catch (Exception e) {
            LOGGER.info("启动一卡通mqtt转发客户端失败", e);
        }
    }

    public void reConnect() {
        try {
            client.connect(options);
            LOGGER.info("重连服务器成功");
        } catch (MqttException e) {
            LOGGER.error("重连服务器失败", e);
        }
    }

    public void publish(MqttMessage message)
            throws MqttException {
        MqttDeliveryToken token = topic.publish(message);
        token.waitForCompletion();
    }

    public void publish(MqttDongluMessage mqttDongluMessage) {
        try {
            MqttMessage mqttMessage = new MqttMessage();
            mqttMessage.setQos(1);
            mqttMessage.setRetained(true);
            byte payload[] = JSONObject.toJSONBytes(mqttDongluMessage, new SerializerFeature[0]);
            mqttMessage.setPayload(payload);
            publish(mqttMessage);
        } catch (MqttException e) {
            LOGGER.error("客户端发送消息失败", e);
        }
    }

    public static void main(String args[])
            throws MqttException {
        MQTTClient client = new MQTTClient();
        client.start();
    }


}
