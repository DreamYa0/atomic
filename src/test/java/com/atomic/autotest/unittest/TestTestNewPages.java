package  com.atomic.autotest.unittest;

import com.atomic.autotest.CsvDataProvider;
import org.testng.annotations.Test;

import java.util.Map;


public class TestTestNewPages extends CsvDataProvider {

	/**
 	 * @RollBack( dbName = "数据库库名",tableName={"表名1","表名2"})注解实现单库多表数据回滚
 	 * @RollBackAll( dbAndTable = "{"库名1.表名1","库名2.表名1"}")注解实现多库多表数据回滚
 	 * @AutoAssert( checkMode = CheckMode.REC)注解实现智能化断言录制
 	 * @AutoAssert( checkMode = CheckMode.REPLAY)注解实现智能化断言回放
 	 * @Scenario 使用此注解来标记用例为某个场景的用例，框架会为场景用例自动注入很多场景相关的属性
 	 * Data.SINGLE(测试用例串行执行),Data.PARALLEL(测试用例并行执行)
 	 */
 	@Test(dataProvider = "csv")
 	public void testMethod(Map<String, Object> param,Object result){
		final Object name = param.get("name");
		System.out.println(name);
	}
}