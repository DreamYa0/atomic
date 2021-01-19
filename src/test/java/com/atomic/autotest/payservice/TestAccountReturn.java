package com.atomic.autotest.payservice;

import cn.com.g7.api.bo.AccountReturnBo;
import cn.com.g7.api.bo.AccountTakeOffBo;
import cn.com.g7.api.bo.AccountTakeOffReq;
import cn.com.g7.api.bo.WaybillInfoBo;
import com.atomic.annotations.ServiceGroup;
import com.atomic.autotest.BaseNgTest;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.RandomStringUtils;
import org.testng.annotations.Test;
import com.atomic.enums.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import cn.com.g7.api.service.PayService;
import com.g7.framework.common.dto.Result;
import java.lang.Void;
import java.util.UUID;

import static com.atomic.autotest.bizTools.bizPayService.*;
import static com.atomic.autotest.bizTools.randomBigType.getRandomBigDecimal;

public class TestAccountReturn extends BaseNgTest<PayService> {

	/**
 	 * 测试前执行，例如:获取数据库中的值,可以用新获取的值替换excel中的值、以及初始化测试数据
 	 * @param context excel入参
 	 */
 	@Override
 	public void beforeTest(Map<String, Object> context) {
		/* 入参初始化
		 * 1.数据模版读取入参
		 */
		JsonObject jsonObject = getAccountReturnReqToJson();
		/* 入参初始化
		 * 2.初始化扣费数据
		 * 3.根据测试用例修改入参
		 */
 		if (context.get("caseName").equals("交易号已存在")) {
			String transNum = "0554b5965e9c4dedbcb1a043f65d4c46";
			jsonObject.addProperty("transNum", transNum);
		} else if (context.get("caseName").equals("授信账户退服务费")) {
 			// 初始化数据库扣服务费操作
			AccountTakeOffReq takeOffReq = initTakeOffAccountCredit(1,1);

			// 根据测试用例修改入参
			String transNum = UUID.randomUUID().toString().replaceAll("-","");
			jsonObject.remove("accountReturnList");
			jsonObject.addProperty("transNum", transNum);

			List<AccountReturnBo> returnBoList = getReturnListAccountCredit(1, takeOffReq, true);
			JsonArray returnList = getAccountReturnList(returnBoList);
			jsonObject.add("accountReturnList", returnList);
		} else if (context.get("caseName").equals("返利账户退服务费")) {
			// 初始化数据库扣服务费操作
			AccountTakeOffReq takeOffReq = initTakeOffAccountRebate(1,1);

			// 根据测试用例修改入参
 			String transNum = UUID.randomUUID().toString().replaceAll("-","");
			jsonObject.remove("accountReturnList");
			jsonObject.addProperty("transNum", transNum);

			List<AccountReturnBo> returnBoList = getReturnListAccountRebate(1, takeOffReq, true);
			JsonArray returnList = getAccountReturnList(returnBoList);
			jsonObject.add("accountReturnList", returnList);
		} else if (context.get("caseName").equals("资金账户退服务费")) {
			// 初始化数据库扣服务费操作
			AccountTakeOffReq takeOffReq = initTakeOffAccountFunds(1,1);

			// 根据测试用例修改入参
			String transNum = UUID.randomUUID().toString().replaceAll("-","");
			jsonObject.remove("accountReturnList");
			jsonObject.addProperty("transNum", transNum);

			List<AccountReturnBo> returnBoList = getReturnListAccountFunds(1, takeOffReq, true);
			JsonArray returnList = getAccountReturnList(returnBoList);
			jsonObject.add("accountReturnList", returnList);
		} else if (context.get("caseName").equals("外调员资金账户退服务费")) {
			// 初始化数据库扣服务费操作
			AccountTakeOffReq takeOffReq = initTakeOffAccountExternalStaff(1,1);

			// 根据测试用例修改入参
			String transNum = UUID.randomUUID().toString().replaceAll("-","");
			jsonObject.remove("accountReturnList");
			jsonObject.addProperty("transNum", transNum);

			List<AccountReturnBo> returnBoList = getReturnListAccountExternalStaff(1, takeOffReq, true);
			JsonArray returnList = getAccountReturnList(returnBoList);
			jsonObject.add("accountReturnList", returnList);
		} else if (context.get("caseName").equals("授信账户退部分服务费")) {
			// 初始化数据库扣服务费操作
			AccountTakeOffReq takeOffReq = initTakeOffAccountCredit(1,1);

			// 根据测试用例修改入参
			String transNum = UUID.randomUUID().toString().replaceAll("-","");
			jsonObject.remove("accountReturnList");
			jsonObject.addProperty("transNum", transNum);

			List<AccountReturnBo> returnBoList = getReturnListAccountCredit(1, takeOffReq, false);

			JsonArray returnList = getAccountReturnList(returnBoList);
			jsonObject.add("accountReturnList", returnList);
		} else if (context.get("caseName").equals("返利账户退部分服务费")) {
			// 初始化数据库扣服务费操作
			AccountTakeOffReq takeOffReq = initTakeOffAccountRebate(1,1);

			// 根据测试用例修改入参
			String transNum = UUID.randomUUID().toString().replaceAll("-","");
			jsonObject.remove("accountReturnList");
			jsonObject.addProperty("transNum", transNum);

			List<AccountReturnBo> returnBoList = getReturnListAccountRebate(1, takeOffReq, false);
			JsonArray returnList = getAccountReturnList(returnBoList);
			jsonObject.add("accountReturnList", returnList);
		} else if (context.get("caseName").equals("资金账户退部分服务费")) {
			// 初始化数据库扣服务费操作
			AccountTakeOffReq takeOffReq = initTakeOffAccountFunds(1,1);

			// 根据测试用例修改入参
			String transNum = UUID.randomUUID().toString().replaceAll("-","");
			jsonObject.remove("accountReturnList");
			jsonObject.addProperty("transNum", transNum);

			List<AccountReturnBo> returnBoList = getReturnListAccountFunds(1, takeOffReq, false);
			JsonArray returnList = getAccountReturnList(returnBoList);
			jsonObject.add("accountReturnList", returnList);
		} else if (context.get("caseName").equals("外调员资金账户部分退服务费")) {
			// 初始化数据库扣服务费操作
			AccountTakeOffReq takeOffReq = initTakeOffAccountExternalStaff(1,1);

			// 根据测试用例修改入参
			String transNum = UUID.randomUUID().toString().replaceAll("-","");
			jsonObject.remove("accountReturnList");
			jsonObject.addProperty("transNum", transNum);

			List<AccountReturnBo> returnBoList = getReturnListAccountExternalStaff(1, takeOffReq, false);
			JsonArray returnList = getAccountReturnList(returnBoList);
			jsonObject.add("accountReturnList", returnList);
		} else if (context.get("caseName").equals("多账户退服务费")) {
			// 初始化数据库扣服务费操作
			AccountTakeOffReq takeOffReq = initTakeOffAccountFunds(3,1);

			// 根据测试用例修改入参
			String transNum = UUID.randomUUID().toString().replaceAll("-","");
			jsonObject.remove("accountReturnList");
			jsonObject.addProperty("transNum", transNum);

			List<AccountReturnBo> returnBoList = getReturnListAccountFunds(3, takeOffReq, true);
			JsonArray returnList = getAccountReturnList(returnBoList);
			jsonObject.add("accountReturnList", returnList);
		} else if (context.get("caseName").equals("多运单退服务")) {
			// 初始化数据库扣服务费操作
			AccountTakeOffReq takeOffReq = initTakeOffAccountFunds(1,3);

			// 根据测试用例修改入参
			String transNum = UUID.randomUUID().toString().replaceAll("-","");
			jsonObject.remove("accountReturnList");
			jsonObject.addProperty("transNum", transNum);

			List<AccountReturnBo> returnBoList = getReturnListAccountFunds(1, takeOffReq, false);
			JsonArray returnList = getAccountReturnList(returnBoList);
			jsonObject.add("accountReturnList", returnList);
		} else if (context.get("caseName").equals("授信账户退服务费退费金额大于订单金额")) {
			// 初始化数据库扣服务费操作
			AccountTakeOffReq takeOffReq = initTakeOffAccountCredit(1,1);

			// 根据测试用例修改入参
			String transNum = UUID.randomUUID().toString().replaceAll("-","");
			jsonObject.remove("accountReturnList");
			jsonObject.addProperty("transNum", transNum);

			List<AccountReturnBo> returnBoList = getReturnListAccountCredit(1, takeOffReq, true);
			BigDecimal tempAmount = returnBoList.get(0).getTotalAmount().add(new BigDecimal("0.1"));
			returnBoList.get(0).setTotalAmount(tempAmount);
			JsonArray returnList = getAccountReturnList(returnBoList);
			jsonObject.add("accountReturnList", returnList);
		} else if (context.get("caseName").equals("返利账户退服务费退费金额大于订单总金额")) {
			// 初始化数据库扣服务费操作
			AccountTakeOffReq takeOffReq = initTakeOffAccountRebate(1,3);

			// 根据测试用例修改入参
			String transNum = UUID.randomUUID().toString().replaceAll("-","");
			jsonObject.remove("accountReturnList");
			jsonObject.addProperty("transNum", transNum);

			List<AccountReturnBo> returnBoList = getReturnListAccountRebate(1, takeOffReq, true);
			BigDecimal tempAmount = returnBoList.get(0).getTotalAmount().add(new BigDecimal("0.1"));
			returnBoList.get(0).setTotalAmount(tempAmount);
			JsonArray returnList = getAccountReturnList(returnBoList);
			jsonObject.add("accountReturnList", returnList);
		} else if (context.get("caseName").equals("资金账户退服务费有退费金额大于订单金额")) {
			// 初始化数据库扣服务费操作
			AccountTakeOffReq takeOffReq = initTakeOffAccountFunds(3,1);

			// 根据测试用例修改入参
			String transNum = UUID.randomUUID().toString().replaceAll("-","");
			jsonObject.remove("accountReturnList");
			jsonObject.addProperty("transNum", transNum);

			List<AccountReturnBo> returnBoList = getReturnListAccountFunds(3, takeOffReq, true);
			BigDecimal tempAmount = returnBoList.get(0).getTotalAmount().add(new BigDecimal("0.1"));
			returnBoList.get(0).setTotalAmount(tempAmount);
			JsonArray returnList = getAccountReturnList(returnBoList);
			jsonObject.add("accountReturnList", returnList);
		} else if (context.get("caseName").equals("外调员资金账户退服务费有退费金额大于订单总金额")) {
			// 初始化数据库扣服务费操作
			AccountTakeOffReq takeOffReq = initTakeOffAccountExternalStaff(2,2);

			// 根据测试用例修改入参
			String transNum = UUID.randomUUID().toString().replaceAll("-","");
			jsonObject.remove("accountReturnList");
			jsonObject.addProperty("transNum", transNum);

			List<AccountReturnBo> returnBoList = getReturnListAccountExternalStaff(2, takeOffReq, true);
			BigDecimal tempAmount = returnBoList.get(0).getTotalAmount().add(new BigDecimal("0.1"));
			returnBoList.get(0).setTotalAmount(tempAmount);
			JsonArray returnList = getAccountReturnList(returnBoList);
			jsonObject.add("accountReturnList", returnList);
		} else if (context.get("caseName").equals("授信账户1个订单多次退服务费")) {
			// 初始化数据库扣服务费操作
			AccountTakeOffReq takeOffReq = initTakeOffAccountCredit(1,1);

			// 根据测试用例修改入参
			String transNum = UUID.randomUUID().toString().replaceAll("-","");
			jsonObject.remove("accountReturnList");
			jsonObject.addProperty("transNum", transNum);

			List<AccountReturnBo> returnBoList = getReturnListAccountCredit(2, takeOffReq, false);
			returnBoList.get(0).setTotalAmount(new BigDecimal("0.01"));
			returnBoList.get(0).getWaybillInfoList().get(0).setAmount(new BigDecimal("0.01"));
			JsonArray returnList = getAccountReturnList(returnBoList);
			jsonObject.add("accountReturnList", returnList);
		} else if (context.get("caseName").equals("返利账户1个订单多次退服务费")) {
			// 初始化数据库扣服务费操作
			AccountTakeOffReq takeOffReq = initTakeOffAccountRebate(1,1);

			// 根据测试用例修改入参
			String transNum = UUID.randomUUID().toString().replaceAll("-","");
			jsonObject.remove("accountReturnList");
			jsonObject.addProperty("transNum", transNum);

			List<AccountReturnBo> returnBoList = getReturnListAccountRebate(2, takeOffReq, false);
			returnBoList.get(0).setTotalAmount(new BigDecimal("0.01"));
			returnBoList.get(0).getWaybillInfoList().get(0).setAmount(new BigDecimal("0.01"));
			JsonArray returnList = getAccountReturnList(returnBoList);
			jsonObject.add("accountReturnList", returnList);
		} else if (context.get("caseName").equals("资金账户1个订单多次退服务费")) {
			// 初始化数据库扣服务费操作
			AccountTakeOffReq takeOffReq = initTakeOffAccountFunds(1,1);

			// 根据测试用例修改入参
			String transNum = UUID.randomUUID().toString().replaceAll("-","");
			jsonObject.remove("accountReturnList");
			jsonObject.addProperty("transNum", transNum);

			List<AccountReturnBo> returnBoList = getReturnListAccountFunds(2, takeOffReq, false);
			returnBoList.get(0).setTotalAmount(new BigDecimal("0.01"));
			returnBoList.get(0).getWaybillInfoList().get(0).setAmount(new BigDecimal("0.01"));
			JsonArray returnList = getAccountReturnList(returnBoList);
			jsonObject.add("accountReturnList", returnList);
		} else if (context.get("caseName").equals("外调员资金账户1个订单多次退服务费")) {
			// 初始化数据库扣服务费操作
			AccountTakeOffReq takeOffReq = initTakeOffAccountExternalStaff(1,1);

			// 根据测试用例修改入参
			String transNum = UUID.randomUUID().toString().replaceAll("-","");
			jsonObject.remove("accountReturnList");
			jsonObject.addProperty("transNum", transNum);

			List<AccountReturnBo> returnBoList = getReturnListAccountExternalStaff(2, takeOffReq, false);
			returnBoList.get(0).setTotalAmount(new BigDecimal("0.01"));
			returnBoList.get(0).getWaybillInfoList().get(0).setAmount(new BigDecimal("0.01"));
			JsonArray returnList = getAccountReturnList(returnBoList);
			jsonObject.add("accountReturnList", returnList);
		} else if (context.get("caseName").equals("授信账户退服务费大于订单扣费金额")) {
			// 初始化数据库扣服务费操作
			AccountTakeOffReq takeOffReq = initTakeOffAccountCredit(1,1);

			// 根据测试用例修改入参
			String transNum = UUID.randomUUID().toString().replaceAll("-","");
			jsonObject.remove("accountReturnList");
			jsonObject.addProperty("transNum", transNum);

			List<AccountReturnBo> returnBoList = getReturnListAccountCredit(2, takeOffReq, true);
			JsonArray returnList = getAccountReturnList(returnBoList);
			jsonObject.add("accountReturnList", returnList);
		} else if (context.get("caseName").equals("返利账户退服务费大于订单扣费金额")) {
			// 初始化数据库扣服务费操作
			AccountTakeOffReq takeOffReq = initTakeOffAccountRebate(1,1);

			// 根据测试用例修改入参
			String transNum = UUID.randomUUID().toString().replaceAll("-","");
			jsonObject.remove("accountReturnList");
			jsonObject.addProperty("transNum", transNum);

			List<AccountReturnBo> returnBoList = getReturnListAccountRebate(2, takeOffReq, true);
			JsonArray returnList = getAccountReturnList(returnBoList);
			jsonObject.add("accountReturnList", returnList);
		} else if (context.get("caseName").equals("资金账户退服务费大于订单扣费金额")) {
			// 初始化数据库扣服务费操作
			AccountTakeOffReq takeOffReq = initTakeOffAccountFunds(1,1);

			// 根据测试用例修改入参
			String transNum = UUID.randomUUID().toString().replaceAll("-","");
			jsonObject.remove("accountReturnList");
			jsonObject.addProperty("transNum", transNum);

			List<AccountReturnBo> returnBoList = getReturnListAccountFunds(2, takeOffReq, true);
			JsonArray returnList = getAccountReturnList(returnBoList);
			jsonObject.add("accountReturnList", returnList);
		} else if (context.get("caseName").equals("外调员资金账户退服务费大于订单扣费金额")) {
			// 初始化数据库扣服务费操作
			AccountTakeOffReq takeOffReq = initTakeOffAccountExternalStaff(1,1);

			// 根据测试用例修改入参
			String transNum = UUID.randomUUID().toString().replaceAll("-","");
			jsonObject.remove("accountReturnList");
			jsonObject.addProperty("transNum", transNum);

			List<AccountReturnBo> returnBoList = getReturnListAccountExternalStaff(2, takeOffReq, true);
			JsonArray returnList = getAccountReturnList(returnBoList);
			jsonObject.add("accountReturnList", returnList);
		}

		System.out.println(jsonObject.toString());
		context.put("data", jsonObject.toString());
 	}

	/**
 	 * @RollBack( dbName = "数据库库名",tableName={"表名1","表名2"})注解实现单库多表数据回滚
 	 * @RollBackAll( dbAndTable = "{"库名1.表名1","库名2.表名1"}")注解实现多库多表数据回滚
 	 * @AutoTest( autoTestMode = AutoTestMode.XXXXX)注解实现自动化测试
 	 * @Test( dataProvider = Data.SINGLE(测试用例串行执行),Data.PARALLEL(测试用例并行执行))
 	 */
 	@Test(dataProvider = Data.SINGLE,enabled = true)
	@ServiceGroup(group = "native")
 	public void testCase(Map<String, Object> context, Result<Void> result) {
 		
 	}
}