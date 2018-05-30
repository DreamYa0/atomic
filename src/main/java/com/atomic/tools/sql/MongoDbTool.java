package com.atomic.tools.sql;

import com.atomic.config.CenterConfig;
import com.atomic.config.GlobalConfig;
import com.google.common.collect.Lists;
import com.google.common.io.Closer;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;

import javax.annotation.concurrent.ThreadSafe;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.atomic.param.Constants.*;

/**
 * @author dreamyao
 * @version 1.0.0
 * @description
 * @Data 2017/11/30 下午10:34
 */
@ThreadSafe
public final class MongoDbTool implements IMongoDb {

    private static final MongoDbTool INSTANCE = new MongoDbTool();
    private final String host;
    private final Integer port;
    private final String user;
    private final String password;
    private volatile MongoDatabase mongoDatabase;
    private volatile Closer closer;


    private MongoDbTool() {
        //加载环境配置文件
        GlobalConfig.load();
        String profile = GlobalConfig.getProfile();
        Map<String, String> maps = CenterConfig.newInstance().readPropertyConfig(profile);
        String ip = maps.get(MONGODB_IP);
        String[] con = ip.split(":");
        host = con[0];
        port = Integer.valueOf(con[1]);
        user = maps.get(MONGODB_USER);
        password = maps.get(MONGODB_PASSWORD);

    }

    public static MongoDbTool getInstance() {
        return INSTANCE;
    }

    public MongoDbTool connect(String database) {
        ServerAddress address = new ServerAddress(host, port);
        List<MongoCredential> credentialsList = Lists.newArrayList();
        MongoCredential credential = MongoCredential.createCredential(user, database, password.toCharArray());
        credentialsList.add(credential);
        closer = Closer.create();
        MongoClient client = closer.register(new MongoClient(address, credentialsList));
        mongoDatabase = client.getDatabase(database);
        return this;
    }

    /**
     * 通过Id获取数据
     * @param tableName 表名
     * @param Id        文档Id
     * @return 是否成功
     */
    @Override
    public Document queryById(String tableName, Object Id) {
        MongoCollection<Document> tableCollection = mongoDatabase.getCollection(tableName);
        BasicDBObject query = new BasicDBObject("_id", Id);
        // DBObject接口和BasicDBObject对象：表示一个具体的记录，BasicDBObject实现了DBObject，是key-value的数据结构，用起来和HashMap是基本一致的。
        FindIterable<Document> iterable = tableCollection.find(query);
        MongoCursor<Document> cursor = iterable.iterator();
        close();
        while (cursor.hasNext()) {
            return cursor.next();
        }
        return null;
    }

    /**
     * 根据文档内容查询文档信息
     * @param tableName 表名
     * @param doc       文档
     * @return 文档信息
     */
    @Override
    public List<Document> queryByDoc(String tableName, BasicDBObject doc) {
        MongoCollection<Document> collection = mongoDatabase.getCollection(tableName);
        FindIterable<Document> iterable = collection.find(doc);
        List<Document> list = new ArrayList<>();
        MongoCursor<Document> cursor = iterable.iterator();
        while (cursor.hasNext()) {
            Document document = cursor.next();
            list.add(document);
        }
        close();
        return list;
    }

    /**
     * 查询表中所有文档信息
     * @param tableName 表名
     * @return 文档信息
     */
    @Override
    public List<Document> queryAll(String tableName) {
        MongoCollection<Document> collection = mongoDatabase.getCollection(tableName);
        FindIterable<Document> iterable = collection.find();
        List<Document> list = Lists.newArrayList();
        MongoCursor<Document> cursor = iterable.iterator();
        while (cursor.hasNext()) {
            Document document = cursor.next();
            list.add(document);
        }
        close();
        return list;
    }

    /**
     * 插入一条文档数据
     * @param tableName 表名
     * @param document  文档
     * @return 是否成功
     */
    @Override
    public boolean insertOne(String tableName, Document document) {
        MongoCollection<Document> collection = mongoDatabase.getCollection(tableName);
        collection.insertOne(document);
        long count = collection.count(document);
        close();
        if (count == 1) {
            return true;
        }
        return false;
    }

    /**
     * 插入多条文档数据
     * @param tableName 表名
     * @param documents 文档信息集合
     * @return 是否成功
     */
    @Override
    public boolean insertMany(String tableName, List<Document> documents) {
        MongoCollection<Document> collection = mongoDatabase.getCollection(tableName);
        long preCount = collection.count();
        collection.insertMany(documents);
        long nowCount = collection.count();
        close();
        if ((nowCount - preCount) == documents.size()) {
            return true;
        }
        return false;
    }

    /**
     * 删除多条文档数据
     * @param tableName 表名
     * @param document  文档
     * @return 是否成功
     */
    @Override
    public boolean deleteMany(String tableName, BasicDBObject document) {
        MongoCollection<Document> collection = mongoDatabase.getCollection(tableName);
        DeleteResult deleteManyResult = collection.deleteMany(document);
        long deletedCount = deleteManyResult.getDeletedCount();
        close();
        if (deletedCount > 0) {
            return true;
        }
        return false;
    }

    /**
     * 删除一条文档数据
     * @param tableName 表名
     * @param document  文档
     * @return 是否成功
     */
    @Override
    public boolean deleteOne(String tableName, BasicDBObject document) {
        MongoCollection<Document> collection = mongoDatabase.getCollection(tableName);
        DeleteResult deleteOneResult = collection.deleteOne(document);
        long deletedCount = deleteOneResult.getDeletedCount();
        close();
        if (deletedCount == 1) {
            return true;
        }
        return false;
    }

    /**
     * 更新多条文档数据
     * @param tableName 表名
     * @param oldDoc    旧文档
     * @param newDoc    新文档
     * @return 是否成功
     */
    @Override
    public boolean updateMany(String tableName, BasicDBObject oldDoc, BasicDBObject newDoc) {
        MongoCollection<Document> collection = mongoDatabase.getCollection(tableName);
        UpdateResult updateManyResult = collection.updateMany(oldDoc, new Document("$set", newDoc));
        long modifiedCount = updateManyResult.getModifiedCount();
        close();
        if (modifiedCount > 0) {
            return true;
        }
        return false;
    }

    /**
     * 更新一条文档数据
     * @param tableName 表名
     * @param oldDoc    旧文档
     * @param newDoc    新文档
     * @return 是否成功
     */
    @Override
    public boolean updateOne(String tableName, BasicDBObject oldDoc, BasicDBObject newDoc) {
        MongoCollection<Document> collection = mongoDatabase.getCollection(tableName);
        UpdateResult updateOneResult = collection.updateOne(oldDoc, new Document("$set", newDoc));
        long modifiedCount = updateOneResult.getModifiedCount();
        close();
        if (modifiedCount == 1) {
            return true;
        }
        return false;
    }

    private void close() {
        try {
            closer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
