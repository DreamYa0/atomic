package com.atomic.util;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

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
     * 模拟同步方式发送消息
     * @param topic
     * @param messages
     */
    public static void syncSend(String topic, Map<String, String> messages) {
        if (null != messages && messages.size() > 0) {
            for (Map.Entry<String, String> entry: messages.entrySet()) {
                // 消息实体
                ProducerRecord record = new ProducerRecord<>(topic, entry.getKey(), entry.getValue());
                // 发送消息
                producer.send(record);
            }

            producer.flush();
            producer.close();
        }
    }

    /**
     * 异步方式发送消息
     * @param topic
     * @param messages
     */
    public static void AsyncSend(String topic, Map<String, String> messages) {
        if (null != messages && messages.size() > 0) {
            for (Map.Entry<String, String> entry: messages.entrySet()) {
                // 消息实体
                ProducerRecord record = new ProducerRecord<>(topic, entry.getKey(), entry.getValue());
                // 发送消息
                producer.send(record, (recordMetadata, e) -> {
                    if (null != e) {
                        e.printStackTrace();
                    } else {
                        System.out.println(String.format("offset:%s,partition:%s",
                                recordMetadata.offset(),
                                recordMetadata.partition()));
                    }
                });
            }

            producer.close();
        }
    }

    public static void main(String[] args) {
        // 消息实体
        String prValue1 = "{\"data\":{\"additional_info\":null,\"additional_key\":\"316\",\"beginLat\":28.477271407232696,\"beginLng\":113.15509888596925,\"beginTime\":1611197547000,\"carnum\":\"堵AFM034\",\"changed\":false,\"code\":null,\"containsChina\":true,\"coordinate\":\"GCJ02\",\"createtime\":1611197547000,\"dbOperation\":1,\"distance\":0,\"driverName\":\"\",\"driverType\":-1,\"driverno\":\"\",\"endLat\":0.0,\"endLng\":0.0,\"endPrecision\":-1.0,\"endTime\":null,\"eventOrgcode\":null,\"eventid\":278727999962992448,\"gpsno\":\"41093091\",\"icno\":\"\",\"id\":278727999962992448,\"imei\":\"106064802005750\",\"intTruckId\":\"04CF8ABFC7F4BE70A81BD767BADAA628\",\"itemId\":6,\"latitude\":28.477271407232696,\"longitude\":113.15509888596925,\"macno\":\"106064802005750\",\"media\":0,\"next\":null,\"nextId\":null,\"oid\":\"99544e47c22aae555ec47a2e91\",\"oidOrgCode\":\"Unknown\",\"orgCode\":\"2001N00Y\",\"pointId\":316,\"pointName\":\"湖南省|长沙市\",\"pointType\":991,\"precision\":-1.0,\"privacy\":0,\"rangeAlarmSet\":null,\"rangeType\":2,\"rowKey\":null,\"save\":false,\"savedToDB\":false,\"seconds\":0,\"sysid\":\"38\",\"time\":1611192837000,\"triggerLat\":28.477271407232696,\"triggerLng\":113.15509888596925,\"triggerPrecision\":-1.0,\"triggerTime\":1611192837000,\"truckOrgcode\":null,\"trucknum\":\"3804CF8ABFC7F4BE70A81BD767BADAA628\",\"type\":1,\"updatetime\":1611197547000},\"platformType\":\"1\",\"type\":\"rangeEvent\"}";
        Map<String, String> message = new LinkedHashMap<>();
        message.put("278727999962992448", prValue1);
        KafkaClientUtils.syncSend("rangeEvent", message);

        // 消息实体
        Map<String, String> message2 = new LinkedHashMap<>();
        String prValue21 = "{\"imei\":\"122100168210560\",\"longitude\":\"112.55289\",\"latitude\":\"34.285325\",\"time\":\"1610233618000\",\"speed\":35,\"course\":150,\"totalMileage\":-1,\"locType\":\"0\",\"precision\":0.0,\"altitude\":0.0,\"source\":2}";
        String prValue22 = "{\"imei\":\"122100168210560\",\"longitude\":\"114.51087\",\"latitude\":\"33.59865666666666\",\"time\":\"1610244140000\",\"speed\":80,\"course\":87,\"totalMileage\":-1,\"locType\":\"0\",\"precision\":0.0,\"altitude\":0.0,\"source\":2}";
        String prValue23 = "{\"imei\":\"122100168210560\",\"longitude\":\"114.651925\",\"latitude\":\"33.576078333333335\",\"time\":\"1610244770000\",\"speed\":77,\"course\":110,\"totalMileage\":-1,\"locType\":\"0\",\"precision\":0.0,\"altitude\":0.0,\"source\":2}";
        message2.put("1221001682105601", prValue21);
        message2.put("1221001682105602", prValue22);
        message2.put("1221001682105603", prValue23);
        KafkaClientUtils.AsyncSend("topic_general_trace", message2);
    }
}
