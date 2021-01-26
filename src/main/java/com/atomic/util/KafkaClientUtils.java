package com.atomic.util;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.*;
import java.util.concurrent.TimeUnit;

public final class KafkaClientUtils {
    private static final String NONCERTIFYPPY = "node1.test.gmq.chinawayltd.com:9092," +
            "node2.test.gmq.chinawayltd.com:9092," +
            "node3.test.gmq.chinawayltd.com:9092";

    private static KafkaProducer<String, String> producer;

    /**
     * 初始化生产者
     */
    static {
        Properties props = initProperties();
        producer = new KafkaProducer<>(props);
    }

    /**
     * 初始化Kafka配置
     * @return
     */
    private static Properties initProperties() {
        Properties props = new Properties();
        props.put("bootstrap.servers", NONCERTIFYPPY);
        props.put("acks", "all");
        props.put("key.serializer", StringSerializer.class.getName());
        props.put("value.serializer", StringSerializer.class.getName());

        return props;
    }

    /**
     * 模拟同步方式发送消息到Kafka
     * @param recordList Kafka消息列表
     * @param interval 发送消息间隔
     */
    public static void syncSend(List<ProducerRecord> recordList, int interval) {
        if (null != recordList && recordList.size() > 0) {
            for (ProducerRecord record: recordList) {
                // 发送消息
                producer.send(record);
                // 发送消息间隔
                if (interval > 0) {
                    try {
                        TimeUnit.SECONDS.sleep(interval);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            producer.flush();
            producer.close();
        }
    }

    /**
     * 提取Kafka消息ProducerRecord(有K, 有V)
     * @param topic 指定Kafka topic
     * @param messages 待提取消息对象
     * @return
     */
    public static List<ProducerRecord> convertPRList(String topic, Map<String, Object> messages) {
        // 检查produce topic
        if (null == topic || topic.length() <= 0) {
            return null;
        }
        // 检查待提取消息内容
        if (null == messages || messages.size() <= 0) {
            return null;
        }
        // 提取produce消息内容
        List<ProducerRecord> recordList = new ArrayList();
        for (Map.Entry<String, Object> entry: messages.entrySet()) {
            recordList.add(new ProducerRecord(topic, entry.getKey(), entry.getValue()));
        }

        return recordList;
    }

    /**
     * 提取Kafka消息ProducerRecord(无K, 有V)
     * @param topic 指定Kafka topic
     * @param messageList 待提取消息内容
     * @return
     */
    public static List<ProducerRecord> convertPRList(String topic, List<String> messageList) {
        // 检查produce topic
        if (null == topic || topic.length() <= 0) {
            return null;
        }
        // 检查待提取消息内容
        if (null == messageList || messageList.size() <= 0) {
            return null;
        }
        // 提取produce消息内容
        List<ProducerRecord> recordList = new ArrayList();
        for (String message: messageList) {
            recordList.add(new ProducerRecord(topic, message));
        }

        return recordList;
    }

    /**
     * 异步方式发送消息到Kafka
     * @param recordList Kafka消息列表
     * @param interval 发送消息间隔
     */
    public static void AsyncSend(List<ProducerRecord> recordList, int interval) {
        if (null != recordList && recordList.size() > 0) {
            for (ProducerRecord record: recordList) {
                // 发送消息
                producer.send(record, new Callback() {
                    @Override
                    public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                        if (null != e) {
                            e.printStackTrace();
                        } else {
                            System.out.println(String.format("offset: %s, partition: %s",
                                    recordMetadata.offset(),
                                    recordMetadata.partition()));
                        }
                    }
                });
                // 发送消息间隔
                if (interval > 0) {
                    try {
                        TimeUnit.SECONDS.sleep(interval);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            producer.close();
        }
    }
}
