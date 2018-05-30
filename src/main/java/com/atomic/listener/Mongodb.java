package com.atomic.listener;

import com.alibaba.fastjson.JSONArray;
import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.Optional;

/**
 * @author dreamyao
 * @version 1.0.0
 * @title MongoDB操作工具类
 * @Data 2017/8/15 13:57
 */
final class Mongodb {

    private String database;
    private String tableName;
    private String host;
    private Integer port;
    private MongoCollection<Document> tableCollection;

    private Mongodb(Builder builder) {
        database = builder.database;
        tableName = builder.tableName;
        host = builder.host;
        port = builder.port;
    }

    Mongodb connectMongodb() {
        if (database == null) {
            database = "extent";
        }
        if (tableName == null) {
            tableName = "report";
        }
        if (host == null) {
            host = "192.168.142.4";
        }
        if (port == null) {
            port = 27017;
        }
        MongoClient mongoClient = new MongoClient(host, port);
        MongoDatabase db = mongoClient.getDatabase(database);
        tableCollection = db.getCollection(tableName);
        return this;
    }

    /**
     * 获取分页数据
     * @param document  查询条件
     * @param pageIndex 当前页
     * @param pageSize  一页条数
     * @return Json数据
     */
    public String getDataByDocument(Bson document, Integer pageIndex, Integer pageSize) {
        FindIterable<Document> iterable = tableCollection.find(document);
        Document page = new Document();
        page.append("endTime", -1);
        iterable.sort(page);
        if (pageIndex > 1) {
            iterable.skip(pageSize * (pageIndex - 1));
        }
        FindIterable<Document> docres = iterable.limit(pageSize);
        final JSONArray array = new JSONArray();
        docres.forEach((Block<Document>) document1 -> array.add(document1.toJson()));
        return array.toString();
    }

    /**
     * 查询数据是否存在
     * @param key   名称
     * @param value 值
     * @return boolean
     */
    public boolean contain(String key, String value) {
        Document filter = new Document(key, value);
        long num = tableCollection.count(filter);
        if (num == 0) {
            return false;
        }
        return true;
    }

    /**
     * 根据Id获取数据
     * @param id 主键Id
     * @return Json数据
     */
    public String getDataById(String id) {
        Document filter = new Document("_id", new ObjectId(id));
        FindIterable<Document> res = tableCollection.find(filter);
        Optional<Document> document = Optional.ofNullable(res.first());
        Document doc = document.orElse(null);
        if (doc == null) {
            return null;
        }
        return doc.toJson();
    }

    /**
     * 根据key、value获取数据
     * @param key   字段名称
     * @param value 字段值
     * @return 表数据Json信息
     */
    public String getDataByExample(String key, String value) {
        Document filter = new Document(key, value);
        FindIterable<Document> res = tableCollection.find(filter);
        Optional<Document> document = Optional.ofNullable(res.first());
        Document doc = document.orElse(null);
        if (doc == null) {
            return null;
        }
        return doc.toJson();
    }

    /**
     * 根据key value 获取主键Id
     * @param key   字段名称
     * @param value 字段值
     * @return 主键Id
     */
    public String getIdByExample(String key, String value) {
        Document filter = new Document(key, value);
        FindIterable<Document> res = tableCollection.find(filter);
        Optional<Document> document = Optional.ofNullable(res.first());
        Document doc = document.orElse(null);
        if (doc != null) {
            ObjectId id = (ObjectId) doc.get("_id");
            return id.toHexString();
        }
        return "";
    }

    public static class Builder {

        private String database = null;
        private String tableName = null;
        private String host = null;
        private Integer port = null;

        public Builder setDatabase(String val) {
            database = val;
            return this;
        }

        public Builder setTableName(String val) {
            tableName = val;
            return this;
        }

        public Builder setHost(String val) {
            host = val;
            return this;
        }

        public Builder setPort(Integer val) {
            port = val;
            return this;
        }

        public Mongodb build() {
            return new Mongodb(this);
        }
    }
}
