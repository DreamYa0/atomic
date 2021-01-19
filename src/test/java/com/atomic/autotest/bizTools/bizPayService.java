package com.atomic.autotest.bizTools;

import cn.com.g7.api.bo.*;
import cn.hutool.db.Db;
import cn.hutool.db.DbUtil;
import cn.hutool.db.Entity;
import cn.hutool.db.Session;
import cn.hutool.db.handler.EntityListHandler;
import cn.hutool.db.sql.SqlExecutor;
import com.atomic.util.DataSourceUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.SerializationUtils;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

import static com.atomic.autotest.bizTools.fileContentUtils.getFileContent;
import static com.atomic.autotest.bizTools.randomBigType.getRandomBigDecimal;

public class bizPayService {
    // 三级机构资金账户对应accountKey
    public static final String FUNDSACOUNTKEYLEVEL3 = "200MQT0Z02-tl&200MQT0Z02#1117430115603836928-tl";
    public static final String FUNDSACCOUNTKEYLEVEL4 = "200MQT0Z0201-tl&200MQT0Z02#1117430115603836928-tl";
    public static final String FUNDSACCOUNTKEYLEVEL5 = "200MQT0Z020101-tl&200MQT0Z02#1117430115603836928-tl";
    public static final String FUNDSACCOUNTKEYLEVEL6 = "200MQT0Z02010101-tl&200MQT0Z02#1117430115603836928-tl";
    public static final String CREDITACCOUNTKEY = "CREDIT-1117430115603836928-tl";
    public static final String REBATEACCOUNTKEY = "rebate-1117430115603836928-tl";
    public static final String FUNDSEXTERNALSTAFFACCOUTKEY = "200MQT0Z0202-tl&200MQT0Z02#1117430115603836928-tl";
    public static final String ORGCODELEVEL3 = "200MQT0Z02";
    public static final String ORGCODELEVEL4 = "200MQT0Z0201";
    public static final String ORGCODELEVEL5 = "200MQT0Z020101";
    public static final String FUNDSEXTERNALSTAFFORGCODE = "200MQT0Z0202";
    public static final String BASECODE = "tl";
    public static final String CREDITOPERATION = "三级机构信用账户扣服务费";
    public static final String REBATEOPERATION = "三级机构返利账户扣服务费";
    public static final String FUNDSEXTERNALSTAFFOPERATION = "三级机构外调员资金账户只扣服务费";
    public static final String CREDITRETURNOPERATION = "退已扣除服务费到指定三级机构授信账户";
    public static final String REBATERETURNOPERATION = "退已扣除服务费到指定三级机构返利账户";
    public static final String FUNDSEXTERNALSTAFFRETURNOPERATION = "退已扣除服务费到指定三级机构外调员资金账户";

    /* 获取扣费入参-请求对象
     */
    public static AccountTakeOffReq getAccountTakeOffReq() {
        String jsonStr = getFileContent("payService/accountTakeOffReq.json");
        AccountTakeOffReq takeOffReq = new Gson().fromJson(jsonStr, AccountTakeOffReq.class);

        return takeOffReq;
    }

    /* 获取扣费入参-请求对象
     */
    public static AccountTakeOffReq getAccountTakeOffReq(String jsonStr) {
        AccountTakeOffReq takeOffReq = new GsonBuilder().create().fromJson(jsonStr, AccountTakeOffReq.class);

        return takeOffReq;
    }

    /* 获取扣费入参-JsonObject
     */
    public static JsonObject getAccountTakeOffReqToJson() {
        AccountTakeOffReq takeOffReq = getAccountTakeOffReq();
        JsonObject jsonObject = new Gson().toJsonTree(takeOffReq).getAsJsonObject();

        return jsonObject;
    }

    /* 获取扣费入参-扣费账户业务对象Bo
     */
    public static AccountTakeOffBo getAccountTakeOffBo(String dataName) {
        String accountTakeOffBoStr = getFileContent(dataName);
        AccountTakeOffBo takeOffBo = new Gson().fromJson(accountTakeOffBoStr, AccountTakeOffBo.class);

        return takeOffBo;
    }

    /* 获取扣费入参-扣费账户(资金账户)业务对象List
     */
    public static List<AccountTakeOffBo> getTakeOffListAccountFunds(int accountSize, int waybillSize) {
        if (accountSize < 1) accountSize = 1;
        if (waybillSize < 1) waybillSize = 1;

        String fundsDataName = "payService/accountTakeOffFunds.json";
        AccountTakeOffBo accountTakeOffBo = getAccountTakeOffBo(fundsDataName);
        List<AccountTakeOffBo> takeOffList = new ArrayList<>();

        // 设置资金账户AccountKey, OrgCode, BaseCode
        for (int i = 0; i < accountSize; i++) {
            AccountTakeOffBo copyAccountTakeOffBo = (AccountTakeOffBo) SerializationUtils.clone(accountTakeOffBo);
            if (i % 3 == 0) {
                copyAccountTakeOffBo.setAccountKey(FUNDSACOUNTKEYLEVEL3);
                copyAccountTakeOffBo.setOrgCode(ORGCODELEVEL3);
                copyAccountTakeOffBo.setBaseCode(BASECODE);
            } else if (i % 3 == 1) {
                copyAccountTakeOffBo.setAccountKey(FUNDSACCOUNTKEYLEVEL4);
                copyAccountTakeOffBo.setOrgCode(ORGCODELEVEL3);
                copyAccountTakeOffBo.setBaseCode(BASECODE);
            } else if (i % 3 == 2) {
                copyAccountTakeOffBo.setAccountKey(FUNDSACCOUNTKEYLEVEL5);
                copyAccountTakeOffBo.setOrgCode(ORGCODELEVEL3);
                copyAccountTakeOffBo.setBaseCode(BASECODE);
            }

            List<WaybillInfoBo> waybillInfoBoList = getWaybillInfoList(waybillSize);
            // 设置账户扣服务费总金额
            copyAccountTakeOffBo.setTotalAmount(getWaybillTotalAmount(waybillInfoBoList));
            // 设置账户运单List
            copyAccountTakeOffBo.setWaybillInfoList(waybillInfoBoList);
            takeOffList.add(copyAccountTakeOffBo);
        }

        return takeOffList;
    }

    /* 获取扣费入参-扣费账户(授信账户)业务对象List
     */
    public static List<AccountTakeOffBo> getTakeOffListAccountCredit(int accountSize, int waybillSize) {
        if (accountSize < 1) accountSize = 1;
        if (waybillSize < 1) waybillSize = 1;

        String fundsDataName = "payService/accountTakeOffFunds.json";
        AccountTakeOffBo accountTakeOffBo = getAccountTakeOffBo(fundsDataName);
        List<AccountTakeOffBo> takeOffList = new ArrayList<>();

        // 设置授信账户AccountKey, OrgCode, BaseCode
        for (int i = 0; i < accountSize; i++) {
            AccountTakeOffBo copyAccountTakeOffBo = (AccountTakeOffBo) SerializationUtils.clone(accountTakeOffBo);
            copyAccountTakeOffBo.setAccountKey(CREDITACCOUNTKEY);
            copyAccountTakeOffBo.setOrgCode(ORGCODELEVEL3);
            copyAccountTakeOffBo.setBaseCode(BASECODE);
            copyAccountTakeOffBo.setOperation(CREDITOPERATION);

            List<WaybillInfoBo> waybillInfoBoList = getWaybillInfoList(waybillSize);
            // 设置账户扣服务费总金额
            copyAccountTakeOffBo.setTotalAmount(getWaybillTotalAmount(waybillInfoBoList));
            // 设置账户运单List
            copyAccountTakeOffBo.setWaybillInfoList(waybillInfoBoList);
            takeOffList.add(copyAccountTakeOffBo);
        }

        return takeOffList;
    }

    /* 获取扣费入参-扣费账户(返利账户)业务对象List
     */
    public static List<AccountTakeOffBo> getTakeOffListAccountRebate(int accountSize, int waybillSize) {
        if (accountSize < 1) accountSize = 1;
        if (waybillSize < 1) waybillSize = 1;

        String fundsDataName = "payService/accountTakeOffFunds.json";
        AccountTakeOffBo accountTakeOffBo = getAccountTakeOffBo(fundsDataName);
        List<AccountTakeOffBo> takeOffList = new ArrayList<>();

        // 设置返利账户AccountKey, OrgCode, BaseCode
        for (int i = 0; i < accountSize; i++) {
            AccountTakeOffBo copyAccountTakeOffBo = (AccountTakeOffBo) SerializationUtils.clone(accountTakeOffBo);
            copyAccountTakeOffBo.setAccountKey(REBATEACCOUNTKEY);
            copyAccountTakeOffBo.setOrgCode(ORGCODELEVEL3);
            copyAccountTakeOffBo.setBaseCode(BASECODE);
            copyAccountTakeOffBo.setOperation(REBATEOPERATION);

            List<WaybillInfoBo> waybillInfoBoList = getWaybillInfoList(waybillSize);
            // 设置账户扣服务费总金额
            copyAccountTakeOffBo.setTotalAmount(getWaybillTotalAmount(waybillInfoBoList));
            // 设置账户运单List
            copyAccountTakeOffBo.setWaybillInfoList(waybillInfoBoList);
            takeOffList.add(copyAccountTakeOffBo);
        }

        return takeOffList;
    }

    /* 获取扣费入参-扣费账户(外调员资金账户)业务对象List
     */
    public static List<AccountTakeOffBo> getTakeOffListAccountExternalStaff(int accountSize, int waybillSize) {
        if (accountSize < 1) accountSize = 1;
        if (waybillSize < 1) waybillSize = 1;

        String fundsDataName = "payService/accountTakeOffFunds.json";
        AccountTakeOffBo accountTakeOffBo = getAccountTakeOffBo(fundsDataName);
        List<AccountTakeOffBo> takeOffList = new ArrayList<>();

        // 设置返利账户AccountKey, OrgCode, BaseCode
        for (int i = 0; i < accountSize; i++) {
            AccountTakeOffBo copyAccountTakeOffBo = (AccountTakeOffBo) SerializationUtils.clone(accountTakeOffBo);
            copyAccountTakeOffBo.setAccountKey(FUNDSEXTERNALSTAFFACCOUTKEY);
            copyAccountTakeOffBo.setOrgCode(ORGCODELEVEL3);
            copyAccountTakeOffBo.setBaseCode(BASECODE);
            copyAccountTakeOffBo.setOperation(FUNDSEXTERNALSTAFFOPERATION);

            List<WaybillInfoBo> waybillInfoBoList = getWaybillInfoList(waybillSize);
            // 设置账户扣服务费总金额
            copyAccountTakeOffBo.setTotalAmount(getWaybillTotalAmount(waybillInfoBoList));
            // 设置账户运单List
            copyAccountTakeOffBo.setWaybillInfoList(waybillInfoBoList);
            takeOffList.add(copyAccountTakeOffBo);
        }

        return takeOffList;
    }

    /* 获取扣费入参-扣费账户-JsonArray
     */
    public static JsonArray getAccoutTakeOffList(List<AccountTakeOffBo> takeOffBoList) {
        JsonArray jsonArray = new Gson().toJsonTree(takeOffBoList,
                new TypeToken<List<AccountTakeOffBo>>() {}.getType()).getAsJsonArray();

        return jsonArray;
    }

    /* 获取扣费入参-扣费账户运单对象Bo
     */
    public static WaybillInfoBo getWaybillInfoBo(String dataName) {
        String waybillInfoBoStr = getFileContent(dataName);
        WaybillInfoBo waybillInfoBo = new Gson().fromJson(waybillInfoBoStr, WaybillInfoBo.class);

        return waybillInfoBo;
    }

    /* 获取扣费入参-扣费账户运单对象List
     */
    public static List<WaybillInfoBo> getWaybillInfoList(int listSize) {
        String waybillDataName = "payService/waybillInfoBo.json";
        WaybillInfoBo waybillInfoBo = getWaybillInfoBo(waybillDataName);
        List<WaybillInfoBo> waybillInfoList = new ArrayList<>();

        for (int i = 0; i < listSize; i++) {
            WaybillInfoBo copyWaybillInfoBo = (WaybillInfoBo) SerializationUtils.clone(waybillInfoBo);
            copyWaybillInfoBo.setWaybillNum("168440468" + RandomStringUtils.randomNumeric(10));
            copyWaybillInfoBo.setAmount(getRandomBigDecimal("0.05", "0.99"));
            waybillInfoList.add(copyWaybillInfoBo);
        }

        return waybillInfoList;
    }

    /* 获取扣费入参-运单总金额
     */
    public static BigDecimal getWaybillTotalAmount(List<WaybillInfoBo> waybillInfoBoList) {
        BigDecimal totalAmount = new BigDecimal("0.00");
        totalAmount.setScale(2,BigDecimal.ROUND_HALF_UP);

        Iterator<WaybillInfoBo> iterator = waybillInfoBoList.iterator();
        while (iterator.hasNext()) {
            WaybillInfoBo waybillInfoBo = (WaybillInfoBo) iterator.next();
            BigDecimal tempAmount = waybillInfoBo.getAmount();
            totalAmount = totalAmount.add(tempAmount);
        }

        return totalAmount;
    }

    /* 获取扣费入参-内部转账业务对象Bo
     */
    public static TransferBo getTransferBo(String dataName) {
        String jsonStr = getFileContent(dataName);
        TransferBo transfer = new Gson().fromJson(jsonStr, TransferBo.class);

        return transfer;
    }

    /* 获取扣费入参-内部转账业务对象List
     */
    public static List<TransferBo> getTransferList(int listSize) {
        String transferBoStr = "payService/transferBo.json";
        TransferBo transfer = getTransferBo(transferBoStr);
        List<TransferBo> transferBoList = new ArrayList<>();

        // 设置内部转账inOrgCode, outOrgCode, operatorName
        for (int i = 0; i < listSize; i++) {
            TransferBo copyTransferBo = SerializationUtils.clone(transfer);
            if (i % 3 == 0) {
                copyTransferBo.setInOrgCode(ORGCODELEVEL4);
                copyTransferBo.setOutOrgCode(ORGCODELEVEL3);
                copyTransferBo.setOperatorName(ORGCODELEVEL4);
            } else if (i % 3 == 1) {
                copyTransferBo.setInOrgCode(ORGCODELEVEL5);
                copyTransferBo.setOutOrgCode(ORGCODELEVEL3);
                copyTransferBo.setOperatorName(ORGCODELEVEL5);
            } else if (i % 3 == 2) {
                copyTransferBo.setInOrgCode(FUNDSEXTERNALSTAFFORGCODE);
                copyTransferBo.setOutOrgCode(ORGCODELEVEL3);
                copyTransferBo.setOperatorName(FUNDSEXTERNALSTAFFORGCODE);
            }
            // 设置转账金额
            copyTransferBo.setAmount(getRandomBigDecimal("0.5", "0.99"));
            // 设置基地代码
            copyTransferBo.setBaseCode(BASECODE);
            transferBoList.add(copyTransferBo);
        }

        return  transferBoList;
    }

    /* 获取扣费入参-内部转账-JsonArray
     */
    public static JsonArray getTransferList(List<TransferBo> transferList) {
        JsonArray jsonArray = new Gson().toJsonTree(transferList,
                new TypeToken<List<TransferBo>>() {}.getType()).getAsJsonArray();

        return jsonArray;
    }

    /* 获取退费入参-请求对象
     */
    public static AccountReturnReq getAccountReturnReq() {
        String jsonStr = getFileContent("payService/accountReturnReq.json");
        AccountReturnReq returnReq = new Gson().fromJson(jsonStr, AccountReturnReq.class);

        return returnReq;
    }

    /* 获取退费入参-JsonObject
     */
    public static JsonObject getAccountReturnReqToJson() {
        AccountReturnReq returnReq = getAccountReturnReq();
        JsonObject jsonObject = new Gson().toJsonTree(returnReq).getAsJsonObject();

        return jsonObject;
    }

    /* 获取退费入参业务对象Bo
     */
    public static AccountReturnBo getAccountReturnBo(String dataName) {
        String jsonStr = getFileContent(dataName);
        AccountReturnBo returnBo = new Gson().fromJson(jsonStr, AccountReturnBo.class);

        return returnBo;
    }

    /* 设置运单部分退款
     */
    private static void setWaybillTotalAmount(List<WaybillInfoBo> waybillInfoBoList) {
        Iterator<WaybillInfoBo> iterator = waybillInfoBoList.iterator();
        while (iterator.hasNext()) {
            WaybillInfoBo waybillInfoBo = (WaybillInfoBo) iterator.next();
            BigDecimal maxAmount = waybillInfoBo.getAmount()
                    .subtract(new BigDecimal("0.01"));
            BigDecimal amount = getRandomBigDecimal(new BigDecimal("0.01"), maxAmount);
            waybillInfoBo.setAmount(amount);
        }
    }

    /* 获取退费入参(资金账户)业务对象List
     */
    public static List<AccountReturnBo> getReturnListAccountFunds(int accountSize, AccountTakeOffReq takeOffReq,
                                                                  boolean bFullRefund) {
        if (accountSize < 1) accountSize = 1;

        String fundsDataName = "payService/accountReturnFunds.json";
        AccountReturnBo accountReturnBo = getAccountReturnBo(fundsDataName);
        List<AccountReturnBo> returnBoList = new ArrayList<>();

        for (int i = 0; i < accountSize; i++) {
            // 深拷贝
            AccountReturnBo copyAccountReturnBo = SerializationUtils.clone(accountReturnBo);
            // 退费账户数量大于扣费账户数量时重置计数
            int j = i % takeOffReq.getAccountTakeOffList().size();
            // 运单List
            // 使用GSON实现List<Object>深拷贝
            String waybillListStr = new GsonBuilder().create().toJson(
                    takeOffReq.getAccountTakeOffList().get(j).getWaybillInfoList());
            List<WaybillInfoBo> waybillInfoBoList = new GsonBuilder().create().fromJson(waybillListStr,
                    new TypeToken<List<WaybillInfoBo>>() {}.getType());
            if (!bFullRefund) {
                setWaybillTotalAmount(waybillInfoBoList);
            }
            copyAccountReturnBo.setWaybillInfoList(waybillInfoBoList);
            // 退费总金额
            copyAccountReturnBo.setTotalAmount(getWaybillTotalAmount(waybillInfoBoList));
            returnBoList.add(copyAccountReturnBo);
        }

        return returnBoList;
    }

    /* 获取退费入参(授信账户)业务对象List
     */
    public static List<AccountReturnBo> getReturnListAccountCredit(int accountSize, AccountTakeOffReq takeOffReq,
                                                                   boolean bFullRefund) {
        if (accountSize < 1) accountSize = 1;

        String fundsDataName = "payService/accountReturnFunds.json";
        AccountReturnBo accountReturnBo = getAccountReturnBo(fundsDataName);
        List<AccountReturnBo> returnBoList = new ArrayList<>();

        for (int i = 0; i < accountSize; i++) {
            // 深拷贝
            AccountReturnBo copyAccountReturnBo = SerializationUtils.clone(accountReturnBo);
            copyAccountReturnBo.setAccountKey(CREDITACCOUNTKEY);
            copyAccountReturnBo.setOrgCode(ORGCODELEVEL3);
            copyAccountReturnBo.setOperation(CREDITRETURNOPERATION);
            copyAccountReturnBo.setBizId(CREDITACCOUNTKEY);
            // 退费账户数量大于扣费账户数量时重置计数
            int j = i % takeOffReq.getAccountTakeOffList().size();
            // 运单List
            // 使用GSON实现List<Object>深拷贝
            String waybillListStr = new GsonBuilder().create().toJson(
                    takeOffReq.getAccountTakeOffList().get(j).getWaybillInfoList());
            List<WaybillInfoBo> waybillInfoBoList = new GsonBuilder().create().fromJson(waybillListStr,
                    new TypeToken<List<WaybillInfoBo>>() {}.getType());
//            System.out.println(new GsonBuilder().create().toJsonTree(waybillInfoBoList,
//                    new TypeToken<List<WaybillInfoBo>>() {}.getType()).getAsJsonArray());
            if (!bFullRefund) {
                // 设置部分退款
                setWaybillTotalAmount(waybillInfoBoList);
            }
//            System.out.println("setWaybillTotalAmount after: " + new GsonBuilder().create().toJson(copyAccountReturnBo));
            copyAccountReturnBo.setWaybillInfoList(waybillInfoBoList);
            // 退费总金额
            copyAccountReturnBo.setTotalAmount(getWaybillTotalAmount(waybillInfoBoList));
            returnBoList.add(copyAccountReturnBo);
        }

        return returnBoList;
    }

    /* 获取退费入参(返利账户)业务对象List
     */
    public static List<AccountReturnBo> getReturnListAccountRebate(int accountSize, AccountTakeOffReq takeOffReq,
                                                                   boolean bFullRefund) {
        if (accountSize < 1) accountSize = 1;

        String fundsDataName = "payService/accountReturnFunds.json";
        AccountReturnBo accountReturnBo = getAccountReturnBo(fundsDataName);
        List<AccountReturnBo> returnBoList = new ArrayList<>();

        for (int i = 0; i < accountSize; i++) {
            AccountReturnBo copyAccountReturnBo = SerializationUtils.clone(accountReturnBo);
            copyAccountReturnBo.setAccountKey(REBATEACCOUNTKEY);
            copyAccountReturnBo.setOrgCode(ORGCODELEVEL3);
            copyAccountReturnBo.setOperation(REBATERETURNOPERATION);
            // 退费账户数量大于扣费账户数量时重置计数
            int j = i % takeOffReq.getAccountTakeOffList().size();
            // 运单List
            // 使用GSON实现List<Object>深拷贝
            String waybillListStr = new GsonBuilder().create().toJson(
                    takeOffReq.getAccountTakeOffList().get(j).getWaybillInfoList());
            List<WaybillInfoBo> waybillInfoBoList = new GsonBuilder().create().fromJson(waybillListStr,
                    new TypeToken<List<WaybillInfoBo>>() {}.getType());
            if (!bFullRefund) {
                setWaybillTotalAmount(waybillInfoBoList);
            }
            copyAccountReturnBo.setWaybillInfoList(waybillInfoBoList);
            // 退费总金额
            copyAccountReturnBo.setTotalAmount(getWaybillTotalAmount(waybillInfoBoList));
            returnBoList.add(copyAccountReturnBo);
        }

        return returnBoList;
    }

    /* 获取扣费入参-扣费账户(外调员资金账户)业务对象List
     */
    public static List<AccountReturnBo> getReturnListAccountExternalStaff(int accountSize, AccountTakeOffReq takeOffReq,
                                                                          boolean bFullRefund) {
        if (accountSize < 1) accountSize = 1;

        String fundsDataName = "payService/accountReturnFunds.json";
        AccountReturnBo accountReturnBo = getAccountReturnBo(fundsDataName);
        List<AccountReturnBo> returnBoList = new ArrayList<>();

        for (int i = 0; i < accountSize; i++) {
            AccountReturnBo copyAccountReturnBo = SerializationUtils.clone(accountReturnBo);
            copyAccountReturnBo.setAccountKey(FUNDSEXTERNALSTAFFACCOUTKEY);
            copyAccountReturnBo.setOrgCode(ORGCODELEVEL3);
            copyAccountReturnBo.setOperation(FUNDSEXTERNALSTAFFRETURNOPERATION);
            // 退费账户数量大于扣费账户数量时重置计数
            int j = i % takeOffReq.getAccountTakeOffList().size();
            // 运单List
            // 使用GSON实现List<Object>深拷贝
            String waybillListStr = new GsonBuilder().create().toJson(
                    takeOffReq.getAccountTakeOffList().get(j).getWaybillInfoList());
            List<WaybillInfoBo> waybillInfoBoList = new GsonBuilder().create().fromJson(waybillListStr,
                    new TypeToken<List<WaybillInfoBo>>() {}.getType());
            if (!bFullRefund) {
                setWaybillTotalAmount(waybillInfoBoList);
            }
            copyAccountReturnBo.setWaybillInfoList(waybillInfoBoList);
            // 退费总金额
            copyAccountReturnBo.setTotalAmount(getWaybillTotalAmount(waybillInfoBoList));
            returnBoList.add(copyAccountReturnBo);
        }

        return returnBoList;
    }

    /* 获取退费入参-JsonArray
     */
    public static JsonArray getAccountReturnList(List<AccountReturnBo> returnList) {
        JsonArray jsonArray = new Gson().toJsonTree(returnList,
                new TypeToken<List<AccountReturnBo>>() {}.getType()).getAsJsonArray();

        return jsonArray;
    }

    /* ORM方式初始化扣费数据库
     */
    private static void takeOffORM(String transNum, int opResult,
                                             String bizId, AccountTakeOffReq takeOffReq) {
        // 获取测试环境数据库连接池
        DataSource dataSource = DataSourceUtils.getDataSource("ntocc_test");

        // 数据库表ntocc_pay_service_fee_waybill，需要根据实际情况判断是insert还是update
        List<Entity> insertTempEntity = getTakeFeeWaybill(dataSource, takeOffReq.getAccountTakeOffList());
        List<Entity> updateEntities = getUpdateTakeFeeWaybill(dataSource, takeOffReq.getAccountTakeOffList());

        // 使用Hutool db初始化测试数据
        Session session = DbUtil.newSession(dataSource);
        try {
            session.beginTransaction();

            List<Entity> insertEntities = new ArrayList<>();
            // 初始化交易记录表(ntocc_pay_service_fee_trans)
            long generatedKey1 = session.insertForGeneratedKey(getFeeTrans(transNum, takeOffReq.getType(), takeOffReq));

            // 初始化账户批量交易关系表(ntocc_pay_service_fee_account_batch)
            int[] intKeys1 = session.insert(getFeeAccountBatch(transNum, bizId, takeOffReq.getAccountTakeOffList()));

            // 初始化运单批量交易关系表(ntocc_pay_service_fee_waybill_batch)
            int[] intKeys2 = session.insert(getFeeWaybillBatch(transNum, takeOffReq.getAccountTakeOffList()));

            // 有转账则初始化批量转账交易关系表(ntocc_pay_service_fee_transfer_batch)
            if (takeOffReq.getIsTransfer()) {
                int[] intKeys3 = session.insert(getFeeTransBatch(transNum, takeOffReq.getTransferList()));
            }

            // waybill无记录则初始化insert运单扣费汇总表(ntocc_pay_service_fee_waybill)
            if (!insertTempEntity.isEmpty()) {
                int[] intKeys4 = session.insert(insertTempEntity);
            }
            // waybill有记录则初始化update运单扣费汇总表(ntocc_pay_service_fee_waybill)
            Iterator<Entity> iterator = updateEntities.iterator();
            while (iterator.hasNext()) {
                // set
                Entity entity = (Entity) iterator.next();
                // where
                Entity where = Entity.create("ntocc_pay_service_fee_waybill")
                        .set("waybill_num", entity.getStr("waybill_num"));
                session.update(entity, where);
            }

            session.commit();
        } catch (SQLException e) {
            session.quietRollback();
        } finally {
            session.close();
        }
    }

    /* 查询运单扣费汇总记录
     * 服务费-运单扣费汇总表: ntocc_pay_service_fee_waybill
     */
    private static List<Entity> getWaybill(DataSource dataSource, String waybillNum) {
        List<Entity> entityList = null;

        Db db = DbUtil.use(dataSource);
        Connection conn = null;
        try {
            conn = db.getConnection();
            entityList = SqlExecutor.query(conn,
                    "select * from ntocc_pay_service_fee_waybill where waybill_num = ?",
                    new EntityListHandler(),
                    waybillNum);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DbUtil.close(conn);
        }

        return entityList;
    }

    /* 生成Entity
     * 服务费-交易记录表: ntocc_pay_service_fee_trans
     */
    private static Entity getFeeTrans(String transNum, int operationStatus, AccountTakeOffReq takeOffReq) {
        Entity feeTrans = null;

        if (takeOffReq != null) {
            Date date=new Date();
            Timestamp timeStamp = new Timestamp(date.getTime());

            feeTrans = Entity.create("ntocc_pay_service_fee_trans")
                    .set("trans_num", transNum)
                    .set("op_type", takeOffReq.getType())
                    .set("status", operationStatus)
                    .set("msg", takeOffReq.getAccountTakeOffList().get(0).getOperation())
                    .set("create_time", timeStamp)
                    .set("update_time", timeStamp);
        }

        return feeTrans;
    }

    /* 生成Entity
     * 服务费-账户批量交易关系表: ntocc_pay_service_fee_account_batch
     */
    private static List<Entity> getFeeAccountBatch(String transNum, String bizId, List<AccountTakeOffBo> takeOffList) {
        List<Entity> entityList = new ArrayList<>();

        if (takeOffList != null && !takeOffList.isEmpty()) {
            Date date=new Date();
            Timestamp timeStamp = new Timestamp(date.getTime());

            Iterator<AccountTakeOffBo> iterator = takeOffList.iterator();
            while (iterator.hasNext()) {
                AccountTakeOffBo takeOffBo = (AccountTakeOffBo)iterator.next();
                Entity feeAccountBatch = Entity.create("ntocc_pay_service_fee_account_batch")
                        .set("trans_num", transNum)
                        .set("account_key", takeOffBo.getAccountKey())
                        .set("org_code", takeOffBo.getOrgCode())
                        .set("operation", takeOffBo.getOperation())
                        .set("base_code", takeOffBo.getBaseCode())
                        .set("remark", "")
                        .set("biz_id", bizId)
                        .set("amount", takeOffBo.getTotalAmount())
                        .set("create_time", timeStamp)
                        .set("update_time", timeStamp);
                entityList.add(feeAccountBatch);
            }
        }

        return entityList;
    }

    /* 生成Entity
     * 服务费-批量转账交易关系表: ntocc_pay_service_fee_transfer_batch
     */
    private static List<Entity> getFeeTransBatch(String transNum, List<TransferBo> transferList) {
        List<Entity> entityList = new ArrayList<>();

        if (transferList != null && !transferList.isEmpty()) {
            Date date=new Date();
            Timestamp timeStamp = new Timestamp(date.getTime());

            Iterator<TransferBo> iterator = transferList.iterator();
            while (iterator.hasNext()) {
                TransferBo transfer = (TransferBo) iterator.next();

                Entity feeTransferBatch = Entity.create("ntocc_pay_service_fee_transfer_batch")
                        .set("trans_num", transNum)
                        .set("in_org_code", transfer.getInOrgCode())
                        .set("out_org_code", transfer.getOutOrgCode())
                        .set("operator_name", transfer.getOperatorName())
                        .set("amount", transfer.getAmount())
                        .set("base_code", transfer.getBaseCode())
                        .set("create_time", timeStamp)
                        .set("update_time", timeStamp);
                entityList.add(feeTransferBatch);
            }
        }

        return entityList;
    }

    /* 生成Entity
     * 服务费-运单批量交易关系表: ntocc_pay_service_fee_waybill_batch
     */
    private static List<Entity> getFeeWaybillBatch(String transNum, List<AccountTakeOffBo> takeOffList) {
        List<Entity> entityList = new ArrayList<>();

        if (takeOffList != null && !takeOffList.isEmpty()) {
            Date date=new Date();
            Timestamp timeStamp = new Timestamp(date.getTime());

            Iterator<AccountTakeOffBo> iterator = takeOffList.iterator();
            while (iterator.hasNext()) {
                AccountTakeOffBo takeOff = (AccountTakeOffBo) iterator.next();

                Iterator<WaybillInfoBo> iteratorWaybill = takeOff.getWaybillInfoList().iterator();
                while (iteratorWaybill.hasNext()) {
                    WaybillInfoBo waybillInfo = (WaybillInfoBo) iteratorWaybill.next();

                    Entity feeWaybillBatch = Entity.create("ntocc_pay_service_fee_waybill_batch")
                            .set("trans_num", transNum)
                            .set("waybill_num", waybillInfo.getWaybillNum())
                            .set("account_key", takeOff.getAccountKey())
                            .set("amount", waybillInfo.getAmount())
                            .set("create_time", timeStamp)
                            .set("update_time", timeStamp);
                    entityList.add(feeWaybillBatch);
                }
            }
        }

        return entityList;
    }

    /* 生成Entity(insert)
     * 服务费-运单扣费汇总表: ntocc_pay_service_fee_waybill
     */
    private static List<Entity> getTakeFeeWaybill(DataSource dataSource, List<AccountTakeOffBo> takeOffList) {
        List<Entity> entityList = new ArrayList<>();

        if (takeOffList != null && !takeOffList.isEmpty()) {
            Date date=new Date();
            Timestamp timeStamp = new Timestamp(date.getTime());

            Iterator<AccountTakeOffBo> iterator = takeOffList.iterator();
            while (iterator.hasNext()) {
                AccountTakeOffBo takeOff = (AccountTakeOffBo) iterator.next();

                Iterator<WaybillInfoBo> iteratorWaybill = takeOff.getWaybillInfoList().iterator();
                while (iteratorWaybill.hasNext()) {
                    WaybillInfoBo waybillInfo = (WaybillInfoBo) iteratorWaybill.next();

                    List<Entity> waybillList = getWaybill(dataSource, waybillInfo.getWaybillNum());
                    if(waybillList.isEmpty()) {
                        Entity takeFeeWaybill = Entity.create("ntocc_pay_service_fee_waybill")
                                .set("waybill_num", waybillInfo.getWaybillNum())
                                .set("org_code", takeOff.getOrgCode())
                                .set("base_code", takeOff.getBaseCode())
                                .set("all_take_amount", waybillInfo.getAmount())
                                .set("all_return_amount", new BigDecimal("0"))
                                .set("actual_take_amount", waybillInfo.getAmount())
                                .set("create_time", timeStamp)
                                .set("update_time", timeStamp);
                        entityList.add(takeFeeWaybill);
                    }
                }
            }
        }

        return entityList;
    }

    /* 生成Entity(update)
     * 服务费-运单扣费汇总表: ntocc_pay_service_fee_waybill
     */
    private static List<Entity> getUpdateTakeFeeWaybill(DataSource dataSource, List<AccountTakeOffBo> takeOffList) {
        List<Entity> entityList = new ArrayList<>();

        if (takeOffList != null && !takeOffList.isEmpty()) {
            Date date=new Date();
            Timestamp timeStamp = new Timestamp(date.getTime());

            Iterator<AccountTakeOffBo> iterator = takeOffList.iterator();
            while (iterator.hasNext()) {
                AccountTakeOffBo takeOff = (AccountTakeOffBo) iterator.next();

                Iterator<WaybillInfoBo> iteratorWaybill = takeOff.getWaybillInfoList().iterator();
                while (iteratorWaybill.hasNext()) {
                    WaybillInfoBo waybillInfo = (WaybillInfoBo) iteratorWaybill.next();

                    List<Entity> waybillList = getWaybill(dataSource, waybillInfo.getWaybillNum());
                    if(!waybillList.isEmpty()) {
                        Entity updateTakeFeeWaybill = Entity.create("ntocc_pay_service_fee_waybill")
                                .set("waybill_num", waybillInfo.getWaybillNum())
                                .set("org_code", takeOff.getOrgCode())
                                .set("base_code", takeOff.getBaseCode())
                                .set("all_take_amount", waybillList.get(0).getBigDecimal("all_take_amount")
                                        .add(waybillInfo.getAmount()))
                                .set("all_return_amount", waybillList.get(0).getBigDecimal("all_return_amount"))
                                .set("actual_take_amount", waybillList.get(0).getBigDecimal("actual_take_amount")
                                        .add(waybillInfo.getAmount()))
                                .set("create_time", timeStamp)
                                .set("update_time", timeStamp);
                        entityList.add(updateTakeFeeWaybill);
                    }
                }
            }
        }

        return entityList;
    }

    /* 初始化三级机构资金账户扣费测试数据
     */
    public static AccountTakeOffReq initTakeOffAccountFunds(int accountSize, int waybillSize) {
        String takeOffTransNum = UUID.randomUUID().toString().replaceAll("-","");
        String bizId = RandomStringUtils.randomAlphanumeric(20);
        List<AccountTakeOffBo> takeOffBoList = getTakeOffListAccountFunds(accountSize, waybillSize);

        AccountTakeOffReq takeOffReq = getAccountTakeOffReq();
        takeOffReq.setAccountTakeOffList(takeOffBoList);
        takeOffReq.setIsTransfer(false);
        takeOffReq.setTransferList(new ArrayList<>());

        takeOffORM(takeOffTransNum, 1, bizId, takeOffReq);

        return takeOffReq;
    }

    /* 初始化授信账户扣费测试数据
     */
    public static AccountTakeOffReq initTakeOffAccountCredit(int accountSize, int waybillSize) {
        String takeOffTransNum = UUID.randomUUID().toString().replaceAll("-","");
        String bizId = RandomStringUtils.randomAlphanumeric(20);
        List<AccountTakeOffBo> takeOffBoList = getTakeOffListAccountCredit(accountSize, waybillSize);

        AccountTakeOffReq takeOffReq = getAccountTakeOffReq();
        takeOffReq.setAccountTakeOffList(takeOffBoList);
        takeOffReq.setIsTransfer(false);
        takeOffReq.setTransferList(new ArrayList<>());

        takeOffORM(takeOffTransNum, 1, bizId, takeOffReq);

        return takeOffReq;
    }

    /* 初始化返利账户扣费测试数据
     */
    public static AccountTakeOffReq initTakeOffAccountRebate(int accountSize, int waybillSize) {
        String takeOffTransNum = UUID.randomUUID().toString().replaceAll("-","");
        String bizId = RandomStringUtils.randomAlphanumeric(20);
        List<AccountTakeOffBo> takeOffBoList = getTakeOffListAccountRebate(1, 1);

        AccountTakeOffReq takeOffReq = getAccountTakeOffReq();
        takeOffReq.setAccountTakeOffList(takeOffBoList);
        takeOffReq.setIsTransfer(false);
        takeOffReq.setTransferList(new ArrayList<>());

        takeOffORM(takeOffTransNum, 1, bizId, takeOffReq);

        return takeOffReq;
    }

    /* 初始化三级机构外调员资金账户扣费测试数据
     */
    public static AccountTakeOffReq initTakeOffAccountExternalStaff(int accountSize, int waybillSize) {
        String takeOffTransNum = UUID.randomUUID().toString().replaceAll("-","");
        String bizId = RandomStringUtils.randomAlphanumeric(20);
        List<AccountTakeOffBo> takeOffBoList = getTakeOffListAccountExternalStaff(accountSize, waybillSize);

        AccountTakeOffReq takeOffReq = getAccountTakeOffReq();
        takeOffReq.setAccountTakeOffList(takeOffBoList);
        takeOffReq.setIsTransfer(false);
        takeOffReq.setTransferList(new ArrayList<>());

        takeOffORM(takeOffTransNum, 1, bizId, takeOffReq);

        return takeOffReq;
    }

    public static void main(String[] args) {
        // 获取测试环境数据库连接池
        DataSource dataSource = DataSourceUtils.getDataSource("ntocc_test");
        List<Entity> entityList = getWaybill(dataSource, "1684404688899979618");
        System.out.println(entityList);

        // 扣服务费入参对象及子对象Debug
        List<AccountTakeOffBo> takeOffBoListFunds = getTakeOffListAccountFunds(1, 1);
        BigDecimal waybillAmount = getWaybillTotalAmount(takeOffBoListFunds.get(0).getWaybillInfoList());
        System.out.println(takeOffBoListFunds);
        List<AccountTakeOffBo> takeOffBoListFunds1 = getTakeOffListAccountFunds(3, 1);
        System.out.println(takeOffBoListFunds1);
        List<AccountTakeOffBo> takeOffBoListFunds2 = getTakeOffListAccountFunds(1, 3);
        System.out.println(takeOffBoListFunds2);
    }
}
