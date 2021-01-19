package com.atomic.autotest.payservice;

import cn.com.g7.api.bo.*;
import com.atomic.annotations.ServiceGroup;
import com.atomic.autotest.BaseNgTest;
import com.atomic.util.DataSourceUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.assertj.db.type.Request;
import org.testng.Assert;
import org.testng.annotations.Test;
import com.atomic.enums.Data;

import java.math.BigDecimal;
import java.util.*;

import cn.com.g7.api.service.PayService;
import com.g7.framework.common.dto.Result;

import javax.sql.DataSource;

import static com.atomic.autotest.bizTools.bizPayService.*;
import static org.assertj.db.api.Assertions.assertThat;

public class TestAccountTakeOff extends BaseNgTest<PayService> {

	/**
 	 * 测试前执行，例如:获取数据库中的值,可以用新获取的值替换excel中的值、以及初始化测试数据
 	 * @param context excel入参
 	 */
 	@Override
 	public void beforeTest(Map<String, Object> context) {
		/* 入参初始化
		 * 1.数据模版读取入参
		 */
		JsonObject jsonObject = getAccountTakeOffReqToJson();
		/* 入参初始化
		 * 2.根据测试用例修改入参
		 */
 		if (context.get("caseName").equals("授信账户扣服务费")) {
			String transNum = UUID.randomUUID().toString().replaceAll("-","");
			jsonObject.addProperty("transNum", transNum);
			jsonObject.addProperty("isTransfer", false);
			jsonObject.remove("transferList");
			jsonObject.remove("accountTakeOffList");

			List<AccountTakeOffBo> takeOffBoList = getTakeOffListAccountCredit(1, 1);
			JsonArray accountTakeOffList = getAccoutTakeOffList(takeOffBoList);
			jsonObject.add("accountTakeOffList", accountTakeOffList);
		} else if (context.get("caseName").equals("返利账户扣服务费")) {
			String transNum = UUID.randomUUID().toString().replaceAll("-","");
			jsonObject.addProperty("transNum", transNum);
			jsonObject.addProperty("isTransfer", false);
			jsonObject.remove("transferList");
			jsonObject.remove("accountTakeOffList");

			List<AccountTakeOffBo> takeOffBoList = getTakeOffListAccountRebate(1, 1);
			JsonArray accountTakeOffList = getAccoutTakeOffList(takeOffBoList);
			jsonObject.add("accountTakeOffList", accountTakeOffList);
		} else if (context.get("caseName").equals("资金账户只扣服务费不转账")) {
			String transNum = UUID.randomUUID().toString().replaceAll("-","");
			jsonObject.addProperty("transNum", transNum);
			jsonObject.addProperty("isTransfer", false);
			jsonObject.remove("transferList");
			jsonObject.remove("accountTakeOffList");

			List<AccountTakeOffBo> takeOffBoList = getTakeOffListAccountFunds(1, 1);
			JsonArray accountTakeOffList = getAccoutTakeOffList(takeOffBoList);
			jsonObject.add("accountTakeOffList", accountTakeOffList);
		} else if (context.get("caseName").equals("资金账户出金且扣服务费")) {
			String transNum = UUID.randomUUID().toString().replaceAll("-","");
			jsonObject.addProperty("transNum", transNum);
		} else if (context.get("caseName").equals("交易号重复")) {
			String transNum = "291adfa91af144949f80ae0093395bab";
			jsonObject.addProperty("transNum", transNum);
		} else if (context.get("caseName").equals("子机构资金账户只扣服务费")) {
			String transNum = UUID.randomUUID().toString().replaceAll("-","");
			jsonObject.addProperty("transNum", transNum);
			jsonObject.addProperty("isTransfer", false);
			jsonObject.remove("transferList");
			jsonObject.remove("accountTakeOffList");

			List<AccountTakeOffBo> takeOffBoList = getTakeOffListAccountFunds(2, 1);
			JsonArray accountTakeOffList = getAccoutTakeOffList(takeOffBoList);
			jsonObject.add("accountTakeOffList", accountTakeOffList);
		} else if (context.get("caseName").equals("子机构资金账户入金后扣服务费")) {
			String transNum = UUID.randomUUID().toString().replaceAll("-","");
			jsonObject.addProperty("transNum", transNum);
			jsonObject.remove("transferList");
			jsonObject.remove("accountTakeOffList");

			List<AccountTakeOffBo> takeOffBoList = getTakeOffListAccountFunds(2, 1);
			JsonArray accountTakeOffList = getAccoutTakeOffList(takeOffBoList);
			jsonObject.add("accountTakeOffList", accountTakeOffList);

			List<TransferBo> transferBoList = getTransferList(2);
			JsonArray transferList = getTransferList(transferBoList);
			jsonObject.add("transferList", transferList);
		} else if (context.get("caseName").equals("外调员资金账户只扣服务费")) {
			String transNum = UUID.randomUUID().toString().replaceAll("-","");
			jsonObject.addProperty("transNum", transNum);
			jsonObject.addProperty("isTransfer", false);
			jsonObject.remove("transferList");
			jsonObject.remove("accountTakeOffList");

			List<AccountTakeOffBo> takeOffBoList = getTakeOffListAccountExternalStaff(1, 1);
			JsonArray accountTakeOffList = getAccoutTakeOffList(takeOffBoList);
			jsonObject.add("accountTakeOffList", accountTakeOffList);
		} else if (context.get("caseName").equals("外调员资金账户入金后扣服务费")) {
			String transNum = UUID.randomUUID().toString().replaceAll("-","");
			jsonObject.addProperty("transNum", transNum);
			jsonObject.remove("transferList");
			jsonObject.remove("accountTakeOffList");

			List<AccountTakeOffBo> takeOffBoList = getTakeOffListAccountExternalStaff(1, 1);
			JsonArray accountTakeOffList = getAccoutTakeOffList(takeOffBoList);
			jsonObject.add("accountTakeOffList", accountTakeOffList);

			List<TransferBo> transferBoList = getTransferList(3);
			JsonArray transferList = getTransferList(transferBoList);
			jsonObject.add("transferList", transferList);
		} else if (context.get("caseName").equals("扣费金额不等于订单金额")) {
			String transNum = UUID.randomUUID().toString().replaceAll("-","");
			jsonObject.addProperty("transNum", transNum);
			jsonObject.getAsJsonArray("accountTakeOffList").get(0).getAsJsonObject()
					.addProperty("operation", "扣费金额不等于订单金额");
			jsonObject.getAsJsonArray("accountTakeOffList").get(0).getAsJsonObject()
					.addProperty("totalAmount", "0.81");
		} else if (context.get("caseName").equals("扣费金额不等于订单总金额")) {
			String transNum = UUID.randomUUID().toString().replaceAll("-","");
			jsonObject.addProperty("transNum", transNum);
			jsonObject.addProperty("isTransfer", false);
			jsonObject.remove("transferList");
			jsonObject.remove("accountTakeOffList");

			List<AccountTakeOffBo> takeOffBoList = getTakeOffListAccountExternalStaff(1, 3);
			takeOffBoList.get(0).setTotalAmount(new BigDecimal("0.01"));
			JsonArray accountTakeOffList = getAccoutTakeOffList(takeOffBoList);
			jsonObject.add("accountTakeOffList", accountTakeOffList);
		} else if (context.get("caseName").equals("扣费账户复数个且账户类型不同")) {
			String transNum = UUID.randomUUID().toString().replaceAll("-","");
			jsonObject.addProperty("transNum", transNum);
			jsonObject.addProperty("isTransfer", false);
			jsonObject.remove("transferList");
			jsonObject.remove("accountTakeOffList");

			List<AccountTakeOffBo> takeOffBoList = getTakeOffListAccountExternalStaff(1, 1);
			List<AccountTakeOffBo> takeOffBoListFunds = getTakeOffListAccountFunds(2, 1);
			List<AccountTakeOffBo> takeOffBoListCredit = getTakeOffListAccountCredit(1, 1);
			List<AccountTakeOffBo> takeOffBoListRebate = getTakeOffListAccountRebate(1, 1);
			takeOffBoList.addAll(takeOffBoListFunds);
			takeOffBoList.addAll(takeOffBoListCredit);
			takeOffBoList.addAll(takeOffBoListRebate);
			JsonArray accountTakeOffList = getAccoutTakeOffList(takeOffBoList);
			jsonObject.add("accountTakeOffList", accountTakeOffList);
		} else if (context.get("caseName").equals("内部转账入账机构复数个")) {
			String transNum = UUID.randomUUID().toString().replaceAll("-","");
			jsonObject.addProperty("transNum", transNum);
			jsonObject.remove("transferList");

			List<TransferBo> transferBoList = getTransferList(3);
			JsonArray transferList = getTransferList(transferBoList);
			jsonObject.add("transferList", transferList);
		} else if (context.get("caseName").equals("仅转账不扣服务费")) {
			String transNum = UUID.randomUUID().toString().replaceAll("-","");
			jsonObject.addProperty("transNum", transNum);

			jsonObject.remove("accountTakeOffList");
		} else if (context.get("caseName").equals("transferList.size=0")) {
			String transNum = UUID.randomUUID().toString().replaceAll("-","");
			jsonObject.addProperty("transNum", transNum);

			jsonObject.getAsJsonArray("transferList").remove(0);
		} else if (context.get("caseName").equals("accountTakeOffList.size=0")) {
			String transNum = UUID.randomUUID().toString().replaceAll("-","");
			jsonObject.addProperty("transNum", transNum);

			jsonObject.getAsJsonArray("accountTakeOffList").remove(0);
		} else if (context.get("caseName").equals("扣费账户单数扣费总金额为0")) {
			String transNum = UUID.randomUUID().toString().replaceAll("-","");
			jsonObject.addProperty("transNum", transNum);

			jsonObject.getAsJsonArray("accountTakeOffList").get(0).getAsJsonObject()
					.addProperty("totalAmount", "0");
		} else if (context.get("caseName").equals("扣费账户复数有扣费总金额为0")) {
			String transNum = UUID.randomUUID().toString().replaceAll("-","");
			jsonObject.addProperty("transNum", transNum);
			jsonObject.addProperty("isTransfer", false);
			jsonObject.remove("transferList");
			jsonObject.remove("accountTakeOffList");

			List<AccountTakeOffBo> takeOffBoList = getTakeOffListAccountExternalStaff(4, 1);
			takeOffBoList.get(0).setTotalAmount(new BigDecimal("0"));
			JsonArray accountTakeOffList = getAccoutTakeOffList(takeOffBoList);
			jsonObject.add("accountTakeOffList", accountTakeOffList);
		} else if (context.get("caseName").equals("内部转账单数转账金额为0")) {
			String transNum = UUID.randomUUID().toString().replaceAll("-","");
			jsonObject.addProperty("transNum", transNum);

			jsonObject.getAsJsonArray("transferList").get(0).getAsJsonObject()
					.addProperty("amount", "0");
		} else if (context.get("caseName").equals("内部转账复数有转账金额为0")) {
			String transNum = UUID.randomUUID().toString().replaceAll("-","");
			jsonObject.addProperty("transNum", transNum);
			jsonObject.remove("transferList");

			List<TransferBo> transferBoList = getTransferList(3);
			transferBoList.get(0).setAmount(new BigDecimal("0"));
			JsonArray transferList = getTransferList(transferBoList);
			jsonObject.add("transferList", transferList);
		} else if (context.get("caseName").equals("资金账户余额不足")) {
			String transNum = UUID.randomUUID().toString().replaceAll("-","");
			jsonObject.addProperty("transNum", transNum);

			jsonObject.getAsJsonArray("accountTakeOffList").get(0).getAsJsonObject()
					.addProperty("accountKey",FUNDSACCOUNTKEYLEVEL6);
		} else if (context.get("caseName").equals("授信账户余额不足")) {
			String transNum = UUID.randomUUID().toString().replaceAll("-","");
			jsonObject.addProperty("transNum", transNum);
			jsonObject.addProperty("isTransfer", false);
			jsonObject.remove("transferList");
			jsonObject.remove("accountTakeOffList");

			List<AccountTakeOffBo> takeOffBoList = getTakeOffListAccountCredit(1, 1);
			takeOffBoList.get(0).setAccountKey("CREDIT-1117430115603836928-tj");
			takeOffBoList.get(0).setBaseCode("tj");
			JsonArray accountTakeOffList = getAccoutTakeOffList(takeOffBoList);
			jsonObject.add("accountTakeOffList", accountTakeOffList);
		} else if (context.get("caseName").equals("返利账户余额不足")) {
			String transNum = UUID.randomUUID().toString().replaceAll("-","");
			jsonObject.addProperty("transNum", transNum);
			jsonObject.addProperty("isTransfer", false);
			jsonObject.remove("transferList");
			jsonObject.remove("accountTakeOffList");

			List<AccountTakeOffBo> takeOffBoList = getTakeOffListAccountRebate(1, 1);
			takeOffBoList.get(0).setAccountKey("rebate-1117430115603836928-tj");
			takeOffBoList.get(0).setBaseCode("tj");
			JsonArray accountTakeOffList = getAccoutTakeOffList(takeOffBoList);
			jsonObject.add("accountTakeOffList", accountTakeOffList);
		} else if (context.get("caseName").equals("账号与基地代码不匹配(此场景无需验证)")) {
			String transNum = UUID.randomUUID().toString().replaceAll("-","");
			jsonObject.addProperty("transNum", transNum);

			jsonObject.getAsJsonArray("accountTakeOffList").get(0).getAsJsonObject()
					.addProperty("baseCode","tj");
		} else if (context.get("caseName").equals("账号与机构代码不匹配(此场景无需验证)")) {
			String transNum = UUID.randomUUID().toString().replaceAll("-","");
			jsonObject.addProperty("transNum", transNum);

			jsonObject.getAsJsonArray("accountTakeOffList").get(0).getAsJsonObject()
					.addProperty("orgCode","200MQT0Z01");
		} else if (context.get("caseName").equals("内部转账转给自己")) {
			String transNum = UUID.randomUUID().toString().replaceAll("-","");
			jsonObject.addProperty("transNum", transNum);

			jsonObject.getAsJsonArray("transferList").get(0).getAsJsonObject()
					.addProperty("inOrgCode","200MQT0Z02");
		} else if (context.get("caseName").equals("扣服务费为负值")) {
			String transNum = UUID.randomUUID().toString().replaceAll("-","");
			jsonObject.addProperty("transNum", transNum);
			jsonObject.addProperty("isTransfer", false);
			jsonObject.remove("transferList");
			jsonObject.remove("accountTakeOffList");

			List<AccountTakeOffBo> takeOffBoList = getTakeOffListAccountFunds(1, 1);
			takeOffBoList.get(0).setTotalAmount(new BigDecimal("-0.1"));
			takeOffBoList.get(0).getWaybillInfoList().get(0).setAmount(new BigDecimal("-0.1"));
			JsonArray accountTakeOffList = getAccoutTakeOffList(takeOffBoList);
			jsonObject.add("accountTakeOffList", accountTakeOffList);
		} else if (context.get("caseName").equals("内部转账为负值")) {
			String transNum = UUID.randomUUID().toString().replaceAll("-","");
			jsonObject.addProperty("transNum", transNum);
			jsonObject.remove("transferList");

			List<TransferBo> transferBoList = getTransferList(1);
			transferBoList.get(0).setAmount(new BigDecimal("-0.1"));
			JsonArray transferList = getTransferList(transferBoList);
			jsonObject.add("transferList", transferList);
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
 	public void testCase(Map<String, Object> context, Result<AccountTakeOffResp> result) {
		AccountTakeOffReq paramIn = getAccountTakeOffReq(context.get("data").toString());

		// 断言接口返回值
		Assert.assertEquals(result.getData().getTransNum(), paramIn.getTransNum());
		for(int i = 0; i < paramIn.getAccountTakeOffList().size(); i++) {
			Assert.assertEquals(result.getData().getBizList().get(i).getFoundAccountKey(),
					paramIn.getAccountTakeOffList().get(i).getAccountKey());
			Assert.assertNotNull(result.getData().getBizList().get(0).getBizId(), "");
		}

		Assert.assertEquals(result.getData().getBizList().size(), 1);
		Assert.assertEquals(result.getData().getBizList().get(0).getFoundAccountKey(),
				paramIn.getAccountTakeOffList().get(0).getAccountKey());

		// 断言数据库
		// 获取测试环境数据库连接池
		DataSource dataSource = DataSourceUtils.getDataSource("ntocc_test");
		// 获取查询数据
		String sqlServiceFeeTrans = "SELECT * FROM ntocc_pay_service_fee_trans WHERE trans_num = '"
				+ paramIn.getTransNum() + "';";
		String sqlServiceFeeAccountBatch = "SELECT * FROM ntocc_pay_service_fee_account_batch WHERE trans_num = '"
				+ paramIn.getTransNum() + "';";
		Request requestServiceFeeTrans = new Request(dataSource, sqlServiceFeeTrans);
		Request requestServiceFeeAccountBatch = new Request(dataSource, sqlServiceFeeAccountBatch);
		// 执行数据库断言
		assertThat(requestServiceFeeTrans)
				.row(0)
				.value("trans_num").isEqualTo(paramIn.getTransNum())
				.value("op_type").isEqualTo("1")
				.value("status").isEqualTo("1");
		assertThat(requestServiceFeeAccountBatch)
				.row(0)
				.value("trans_num").isEqualTo(paramIn.getTransNum())
				.value("account_key").isEqualTo("")
				.value("org_code").isEqualTo("");
 	}
}