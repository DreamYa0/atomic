package com.atomic.exception;

import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.service.GenericException;
import com.alibaba.fastjson.JSONException;

/**
 * Created by dreamyao on 2017/6/12.
 */
public abstract class ThrowException {

    /**
     * 判断异常具体类型,方便数据展示平台统计
     * @param e
     */
    public static void throwNewException(Exception e) {
        if (e instanceof NullPointerException) {
            throw new NullPointerException(e.getMessage());
        } else if (e instanceof AnnotationException) {
            throw new AnnotationException(e);
        } else if (e instanceof ClassCastException) {
            throw new ClassCastException(e.getMessage());
        } else if (e instanceof ArrayIndexOutOfBoundsException) {
            throw new ArrayIndexOutOfBoundsException(e.getMessage());
        } else if (e instanceof JSONException) {
            throw new JSONException(e.getMessage());
        } else if (e instanceof NumberFormatException) {
            throw new NumberFormatException(e.getMessage());
        } else if (e instanceof ArrayStoreException) {
            throw new ArrayStoreException(e.getMessage());
        } else if (e instanceof RpcException) {
            throw new RpcException(e);
        } else if (e instanceof GenericException) {
            throw new GenericException(e);
        } else if (e instanceof AssertJDBException) {
            throw new AssertJDBException(e);
        } else if (e instanceof JSONCheckException) {
            throw new JSONCheckException(e.getMessage());
        } else if (e instanceof HttpInterfaceException) {
            throw new HttpInterfaceException(e.getMessage());
        } else if (e instanceof DubboServiceException) {
            throw new DubboServiceException(e);
        } else if (e instanceof AutoTestException) {
            throw new AutoTestException(e);
        } else if (e instanceof InjectResultException) {
            throw new InjectResultException(e);
        } else if (e instanceof AssertCheckException) {
            throw new AssertCheckException(e);
        } else if (e instanceof IllegalStateException) {
            throw new IllegalStateException(e);
        } else if (e instanceof QueryDataException) {
            throw new QueryDataException(e);
        } else if (e instanceof TestTimeException) {
            throw new TestTimeException(e);
        } else if (e instanceof UserKeyException) {
            throw new UserKeyException(e);
        } else if (e instanceof DatabaseException) {
            throw new DatabaseException(e);
        } else if (e instanceof InvokeException) {
            throw new InvokeException(e);
        } else if (e instanceof GetBeanException) {
            throw new GetBeanException(e);
        } else if (e instanceof CenterConfigException) {
            throw new CenterConfigException(e);
        } else {
            throw new RuntimeException(e);
        }
    }
}
