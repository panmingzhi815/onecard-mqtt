package org.pan.callback;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.pan.MQTTClient;
import org.pan.message.MessageDirection;
import org.pan.message.MqttDongluMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PushClientCallback
        implements MqttCallback {
    private final String host = "http://127.0.0.1:9091";
    private static Logger LOGGER = LoggerFactory.getLogger((Class) PushClientCallback.class);
    private final RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(2000).setConnectTimeout(1000).build();
    private MQTTClient mqttClient;
    private final ExecutorService executorService;

    public PushClientCallback(MQTTClient mqttClient) {
        this.mqttClient = mqttClient;
        this.executorService = Executors.newCachedThreadPool();
    }

    public void connectionLost(Throwable cause) {
        LOGGER.info("\u6b63\u5728\u91cd\u8fde\u670d\u52a1\u5668");
        this.mqttClient.reConnect();
    }

    public void deliveryComplete(IMqttDeliveryToken token) {
    }

    public void messageArrived(String topic, MqttMessage message) throws Exception {
        MqttDongluMessage msg = JSONObject.parseObject(message.getPayload(), MqttDongluMessage.class, (Feature[]) new Feature[0]);
        if (msg.getDirection() != MessageDirection.toClient) {
            return;
        }
        String payLoad = new String(message.getPayload());
        LOGGER.info("接收消息主题 : {},接收消息Qos : {},接收消息内容 : {}", topic, message.getQos(), payLoad);
        this.executorService.execute(() -> {
                    try {
                        this.acceptHttpRequest(msg);
                        LOGGER.error("mqtt 消息 转本地http请求成功");
                    } catch (IOException e) {
                        LOGGER.error("mqtt 消息 转本地http请求失败", e);
                    }
                }
        );
    }

    private void acceptHttpRequest(MqttDongluMessage msg) throws IOException {
        msg.setDirection(MessageDirection.toServer);
        HttpGet httpGet = new HttpGet("http://127.0.0.1:9091" + msg.getData());
        httpGet.setConfig(this.requestConfig);
        CloseableHttpClient aDefault = HttpClients.createDefault();
        try {
            CloseableHttpResponse httpResponse = aDefault.execute(httpGet);
            Throwable throwable = null;
            try {
                HttpEntity entity = httpResponse.getEntity();
                String toString = EntityUtils.toString(entity, "utf-8");
                msg.setData(toString);
                EntityUtils.consume(entity);
            } catch (Throwable entity) {
                throwable = entity;
                throw entity;
            } finally {
                if (httpResponse != null) {
                    if (throwable != null) {
                        try {
                            httpResponse.close();
                        } catch (Throwable entity) {
                            throwable.addSuppressed(entity);
                        }
                    } else {
                        httpResponse.close();
                    }
                }
            }
        } catch (Exception e) {
            msg.setData("");
            LOGGER.error("发送请求失败:{}", msg.getData(), e);
        } finally {
            this.mqttClient.publish(msg);
        }
    }
}
