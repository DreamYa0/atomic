package com.atomic.tools.sql;


import com.mongodb.BasicDBObject;
import org.bson.Document;

import java.util.List;

/**
 * @author dreamyao
 * @version 1.0.0
 * @description
 * @Data 2018/05/30 10:48
 */
public interface IMongoDb {

    /**
     * 通过Id获取数据
     * @param tableName 表名
     * @param Id        文档Id
     * @return 是否成功
     */
    Document queryById(String tableName, Object Id);

    /**
     * 根据文档内容查询文档信息
     * @param tableName 表名
     * @param doc       文档
     * @return 文档信息
     */
    List<Document> queryByDoc(String tableName, BasicDBObject doc);

    /**
     * 查询表中所有文档信息
     * @param tableName 表名
     * @return 文档信息
     */
    List<Document> queryAll(String tableName);

    /**
     * 插入一条文档数据
     * @param tableName 表名
     * @param doc       文档
     * @return 是否成功
     */
    boolean insertOne(String tableName, Document doc);

    /**
     * 插入多条文档数据
     * @param tableName 表名
     * @param documents 文档信息集合
     * @return 是否成功
     */
    boolean insertMany(String tableName, List<Document> documents);

    /**
     * 删除多条文档数据
     * @param tableName 表名
     * @param doc       文档
     * @return 是否成功
     */
    boolean deleteMany(String tableName, BasicDBObject doc);

    /**
     * 删除一条文档数据
     * @param tableName 表名
     * @param document  文档
     * @return 是否成功
     */
    boolean deleteOne(String tableName, BasicDBObject document);

    /**
     * 更新多条文档数据
     * @param tableName 表名
     * @param oldDoc    旧文档
     * @param newDoc    新文档
     * @return 是否成功
     */
    boolean updateMany(String tableName, BasicDBObject oldDoc, BasicDBObject newDoc);

    /**
     * 更新一条文档数据
     * @param tableName 表名
     * @param oldDoc    旧文档
     * @param newDoc    新文档
     * @return 是否成功
     */
    boolean updateOne(String tableName, BasicDBObject oldDoc, BasicDBObject newDoc);
}
