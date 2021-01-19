package com.atomic.autotest.payservice;

import com.atomic.annotations.ServiceGroup;
import com.atomic.autotest.BaseNgTest;
import org.testng.annotations.Test;
import com.atomic.enums.Data;
import java.util.Map;
import java.util.UUID;

import cn.com.g7.api.service.PayService;
import com.g7.framework.common.dto.Result;
import cn.com.g7.api.bo.AccountTransStatusResp;

public class TestAccountTransStatus extends BaseNgTest<PayService> {

	/**
 	 * 测试前执行，例如:获取数据库中的值,可以用新获取的值替换excel中的值、以及初始化测试数据
 	 * @param context excel入参
 	 */
 	@Override
 	public void beforeTest(Map<String, Object> context) {
 		/* 入参初始化
 		 * 1.根据测试用例修改入参
 		 */
 		if (context.get("caseName").equals("交易状态查询-transNum不存在")) {
			String transNum = UUID.randomUUID().toString().replaceAll("-","");
			context.put("transNum", transNum);
		} else if (context.get("caseName").equals("交易状态查询-扣费交易失败")) {
			String transNum = "d572ab8bdf2b42a484e6d67b57166888";
			context.put("transNum", transNum);
		} else if (context.get("caseName").equals("交易状态查询-扣费交易成功")) {
			String transNum = "108464efe5524e06838bb4c9b205d878";
			context.put("transNum", transNum);
		} else if (context.get("caseName").equals("交易状态查询-退费交易成功")) {
			String transNum = "7a02149e6ebd46a0a92dc21ba43c3f3b";
			context.put("transNum", transNum);
		}
 	}

	/**
 	 * @RollBack( dbName = "数据库库名",tableName={"表名1","表名2"})注解实现单库多表数据回滚
 	 * @RollBackAll( dbAndTable = "{"库名1.表名1","库名2.表名1"}")注解实现多库多表数据回滚
 	 * @AutoTest( autoTestMode = AutoTestMode.XXXXX)注解实现自动化测试
 	 * @Test( dataProvider = Data.SINGLE(测试用例串行执行),Data.PARALLEL(测试用例并行执行))
 	 */
 	@Test(dataProvider = Data.SINGLE,enabled = true)
	@ServiceGroup(group = "native")
 	public void testCase(Map<String, Object> context, Result<AccountTransStatusResp> result) {
 		
 	}
}