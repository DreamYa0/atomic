package com.atomic.param.parser;

import com.atomic.param.Constants;
import com.atomic.param.TestNGUtils;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.SpelCompilerMode;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.testng.ITestResult;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author dreamyao
 * @title
 * @date 2018/7/4 下午10:16
 * @since 1.0.0
 */
@ThreadSafe
public class SpelParser implements Parser {

    private final ITestResult testResult;
    private final Object result;
    @GuardedBy("this")
    private final Map<String, Object> context;

    public SpelParser(ITestResult testResult, Object result, Map<String, Object> context) {
        this.testResult = testResult;
        this.result = result;
        this.context = context;
    }

    @Override
    public void parser() {

        ExpressionParser parser = prepareExpressionParser();
        EvaluationContext evaluation = new StandardEvaluationContext(result);

        synchronized (context) {
            ExcelResolver excel = new ExcelResolver();
            parserExcel(parser, evaluation, excel, context);
        }
    }

    private ExpressionParser prepareExpressionParser() {
        SpelParserConfiguration configuration = new SpelParserConfiguration(SpelCompilerMode.IMMEDIATE,
                SpelParser.class.getClassLoader());
        return new SpelExpressionParser(configuration);
    }

    private void parserExcel(ExpressionParser parser,
                             EvaluationContext evaluation,
                             ExcelResolver excel,
                             Map<String, Object> context) {

        String className = TestNGUtils.getTestCaseClassName(testResult);
        Class<? extends Class> clazz = testResult.getTestClass().getRealClass().getClass();
        String resource = clazz.getResource("").getPath();
        String filePath = resource + className + ".xls";

        Set<Map.Entry<String, Object>> entries = context.entrySet();
        for (Map.Entry<String, Object> entry : entries) {
            Object value = entry.getValue();
            if (Objects.isNull(value)) {
                String paramName = entry.getKey();
                Map<String, Object> paramNameOfValue = excel.readDataByRow(filePath, paramName,
                        Integer.parseInt(context.get(Constants.CASE_INDEX).toString()));
                if (Boolean.FALSE.equals(CollectionUtils.isEmpty(paramNameOfValue))) {

                    parserExcel(parser, evaluation, excel, paramNameOfValue);
                }
            }

            if (isExpression(value)) {
                // 从依赖接口返回值中解析到对应 SPEL 表达式的值
                String expression = getExpression(value);
                Object actualValue = parserActualValue(parser, evaluation, expression);
                entry.setValue(actualValue);
            }
        }
    }

    private boolean isExpression(Object value) {
        Assert.notNull(value, "value is not null.");
        String valString = value.toString();
        return valString.startsWith("${") && valString.endsWith("}");
    }

    private String getExpression(Object value) {
        String valString = value.toString();
        return valString.substring(2, valString.length() - 1);
    }

    private Object parserActualValue(ExpressionParser parser, EvaluationContext evaluation, String expression) {
        return parser.parseExpression(expression).getValue(evaluation);
    }
}
