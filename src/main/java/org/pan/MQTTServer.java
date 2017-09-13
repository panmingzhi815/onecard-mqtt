package org.pan;

import com.alibaba.fastjson.JSONObject;
import com.sun.net.httpserver.HttpExchange;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.pan.callback.PushServerCallback;
import org.pan.message.MqttDongluMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MQTTServer {

    private static Logger LOGGER = LoggerFactory.getLogger(MQTTServer.class);
    public static final String HOST = "tcp://120.192.167.84:1883";
    public static final String TOPIC = "onecard_topic";
    private static final String CLIENT_ID = "onecard_server";
    private MqttClient client;
    private MqttTopic mqttTopic;
    private MqttConnectOptions options;

    public MQTTServer() throws MqttException {
        client = new MqttClient(HOST, CLIENT_ID, new MemoryPersistence());
        connect();
    }

    private void connect() {
        options = new MqttConnectOptions();
        options.setCleanSession(false);
        options.setConnectionTimeout(10);
        options.setKeepAliveInterval(20);
        try {
            client.setCallback(new PushServerCallback(this));
            client.connect(options);
            mqttTopic = client.getTopic(TOPIC);
            client.subscribe(new String[]{TOPIC}, new int[]{1});
        } catch (Exception e) {
            e.printStackTrace();
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
        MqttDeliveryToken token = mqttTopic.publish(message);
        token.waitForCompletion();
        LOGGER.info("发送消息结果：{} 消息主题：{} 消息内容：{}", token.isComplete(), mqttTopic.getName(), new String(message.getPayload()));
    }

    public void publish(MqttDongluMessage mqttDongluMessage, HttpExchange httpExchange) {
        try {
            long timeMillis = System.currentTimeMillis();
            mqttDongluMessage.setId(timeMillis);
            MqttMessage mqttMessage = new MqttMessage();
            mqttMessage.setQos(1);
            mqttMessage.setRetained(true);
            byte payload[] = JSONObject.toJSONBytes(mqttDongluMessage);
            mqttMessage.setPayload(payload);
            publish(mqttMessage);
            if (httpExchange != null) {
                PushServerCallback.cache.put(timeMillis, httpExchange);
            }
        } catch (MqttException e) {
            LOGGER.error("服务器发送消息失败", e);
        }
    }


}
