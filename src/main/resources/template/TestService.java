package template;

import org.testng.annotations.BeforeClass;

import java.util.Map;


//import

public class Test$methodName extends Base$serviceNameTest<$serviceName>{

    /**
     * db初始化,此方法中初始化的数据无法自动回滚
     */
    @BeforeClass
    @Override
    public void initDb(){

    }

    /**
     * 测试前执行，例如:获取数据库中的值,可以用新获取的值替换excel中的值、以及初始化测试数据
     * @param context
     */
    @Override
    public void beforeTest(Map<String, Object> context) {

    }

	//TestNg
}
