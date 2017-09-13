package org.pan;

import com.sun.net.httpserver.HttpServer;
import org.pan.callback.PushServerCallback;
import org.pan.message.MessageDirection;
import org.pan.message.MqttDongluMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

public class HTTPOnecard {
    private static Logger LOGGER = LoggerFactory.getLogger((Class) HTTPOnecard.class);
    private int PORT = 9091;

    public void startHttp() {
        try {
            ServerSocket serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress(this.PORT));
            serverSocket.close();
        } catch (IOException e) {
            LOGGER.info("检查本地端口{}己被", this.PORT);
            return;
        }
        try {
            MQTTServer mqttServer = new MQTTServer();
            HttpServer httpServer = HttpServer.create(new InetSocketAddress(this.PORT), 0);
            httpServer.createContext("/", httpExchange -> {
                        try {
                            MqttDongluMessage mqttDongluMessage = new MqttDongluMessage();
                            mqttDongluMessage.setDirection(MessageDirection.toClient);
                            mqttDongluMessage.setData(httpExchange.getRequestURI().toString());
                            mqttServer.publish(mqttDongluMessage, httpExchange);
                            while (PushServerCallback.cache.getIfPresent(mqttDongluMessage.getId()) != null) {
                                try {
                                    Thread.sleep(200);
                                } catch (InterruptedException e) {
                                    httpExchange.close();
                                }
                            }
                        } finally {
                            httpExchange.close();
                        }
                    }
            );
            httpServer.start();
        } catch (Exception e) {
            LOGGER.error("启动web服务器失败", e);
        }
    }

    public static void main(String[] args) {
        try {
            HTTPOnecard httpOnecard = new HTTPOnecard();
            httpOnecard.startHttp();
            LOGGER.info("启动一卡通mqtt转发服务器成功");
        } catch (Exception e) {
            LOGGER.error("启动一卡通mqtt转发服务器失败", e);
        }
    }
}
