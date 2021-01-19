package  com.atomic.autotest.restService.cashdeskTruckBroker;

import com.atomic.autotest.RestTestBase;
import com.atomic.enums.Data;
import org.testng.annotations.Test;
import io.restassured.response.Response;

import java.util.Map;


public class TestElectronicReceipt extends RestTestBase {

	/**
 	 * 测试前执行，例如:获取数据库中的值,可以用新获取的值替换excel中的值、以及初始化测试数据
 	 * @param context excel入参
 	 */
 	@Override
 	public void beforeTest(Map<String, Object> context) {
 		
 	}

	/**
 	 * @RollBack( dbName = "数据库库名",tableName={"表名1","表名2"})注解实现单库多表数据回滚
 	 * @RollBackAll( dbAndTable = "{"库名1.表名1","库名2.表名1"}")注解实现多库多表数据回滚
 	 * Data.SINGLE(测试用例串行执行),Data.PARALLEL(测试用例并行执行)
 	 */
 	@Test(dataProvider = Data.SINGLE,enabled = true)
 	public void testMethod(Map<String, Object> param, Response result){
 		
 	}
}