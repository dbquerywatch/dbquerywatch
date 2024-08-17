package org.dbquerywatch.common;

import java.lang.reflect.Method;

public final class MethodUtils {
    private MethodUtils() {
    }

    public static Object invokeStaticMethod1(Class<?> clazz, String methodName, Class<?> parameterType, Object parameter)
        throws ReflectiveOperationException {
        Method method = clazz.getMethod(methodName, parameterType);
        return method.invoke(null, parameter);
    }

    public static Object invokeMethod1(Object object, String methodName, Class<?> parameterType, Object parameter)
        throws ReflectiveOperationException {
        Method method = object.getClass().getMethod(methodName, parameterType);
        return method.invoke(object, parameter);
    }
}
