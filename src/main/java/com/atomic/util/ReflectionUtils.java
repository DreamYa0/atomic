package com.atomic.util;

import com.google.common.collect.Lists;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;
import org.springframework.asm.ClassReader;
import org.springframework.asm.ClassVisitor;
import org.springframework.asm.ClassWriter;
import org.springframework.asm.Label;
import org.springframework.asm.MethodVisitor;
import org.springframework.asm.Opcodes;
import org.springframework.asm.Type;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class ReflectionUtils {

    private ReflectionUtils() {
    }

    public static <T> T initFromClass(Class<? extends T> cls) {
        // 实例化Class对象
        try {
            return cls.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> void initializeClass(Class<? extends T>... classes) {
        // 实例化Class对象
        for (Class<?> clazz : classes) {
            try {
                Class.forName(clazz.getName(), true, clazz.getClassLoader());
            } catch (ClassNotFoundException e) {
                throw new AssertionError(e);
            }
        }
    }

    public static Field getField(Object bean,
                                 Class<?> targetClass,
                                 String fieldName) throws IllegalAccessException {
        // 获取属性值
        List<Field> fields = Lists.newArrayList();
        getAllFields(bean.getClass(), fields);
        // 第一次类型和属性名都满足才返回
        Optional<Field> oneNewFields = fields.stream()
                .filter(field -> field.getName().equals(fieldName) && field.getType() == targetClass)
                .findFirst();
        // 第2次类型和属性名满足一个即返回
        Optional<Field> twoNewFields = fields.stream()
                .filter(field -> field.getName().equals(fieldName) || field.getType() == targetClass)
                .findFirst();
        return oneNewFields.orElseGet(() -> twoNewFields.orElse(null));
    }

    public static void getAllFields(Class<?> clazz, List<Field> fields) {
        // 获取所有属性，包括父类对象的属性
        if (clazz == null || fields == null || clazz == Object.class)
            return;
        fields.addAll(getAllFieldsList(clazz));
    }

    public static List<Field> getAllFieldsList(final Class<?> clazz) {
        // 获取所有属性，包括父类对象的属性
        Class<?> currentClass = clazz;
        final List<Field> allFields = new ArrayList<Field>();
        while (currentClass != null) {
            final Field[] declaredFields = currentClass.getDeclaredFields();
            Collections.addAll(allFields, declaredFields);
            currentClass = currentClass.getSuperclass();
        }
        return allFields;
    }

    public static void getFields(Class<?> clazz, List<Field> fields) {
        // 获取单个类的属性
        if (clazz == null || fields == null || clazz == Object.class) {
            return;
        }
        Field[] newFields = clazz.getDeclaredFields();
        fields.addAll(Lists.newArrayList(newFields));
    }

    public static Object getFieldInstance(Object bean,
                                          Class<?> targetClass,
                                          String fieldClassName) throws IllegalAccessException {
        // 获取属性值
        Field[] fields = targetClass.getDeclaredFields();
        Optional<Field> filterField = Arrays.stream(fields)
                .filter(field -> field.getName().equals(fieldClassName) ||
                        field.getType().toString().toLowerCase().endsWith("." + fieldClassName.toLowerCase()))
                .findFirst();
        if (filterField.isPresent()) {
            filterField.get().setAccessible(true);
            return filterField.get().get(bean);
        }
        return null;
    }

    public static Method getMethod(Class<?> clazz, String methodName) {
        // 只根据方法名称来获取method，有重载函数的不要调用
        Optional<Method> method = Arrays.stream(clazz.getMethods())
                .filter(m -> m.getName().equalsIgnoreCase(methodName))
                .findFirst();
        return method.orElse(null);
    }

    private static boolean sameType(Type[] types, Class<?>[] clazzes) {
        // 比较参数类型是否一致
        // 个数不同
        if (types.length != clazzes.length) {
            return false;
        }
        for (int i = 0; i < types.length; i++) {
            if (!Type.getType(clazzes[i]).equals(types[i])) {
                return false;
            }
        }
        return true;
    }

    public static String[] getParamNames(Method method) {
        // 获取方法的参数名
        LocalVariableTableParameterNameDiscoverer u = new LocalVariableTableParameterNameDiscoverer();
        return u.getParameterNames(method);
    }

    public static String[] getParamNames(Class<?> clazz, String method) throws NotFoundException {
        // 获取方法的参数名
        ClassPool pool = ClassPool.getDefault();
        CtClass cc = pool.get(clazz.getName());
        CtMethod cm = cc.getDeclaredMethod(method);
        // 使用javaassist的反射方法获取方法的参数名
        MethodInfo methodInfo = cm.getMethodInfo();
        CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
        LocalVariableAttribute attr = (LocalVariableAttribute) codeAttribute.getAttribute(LocalVariableAttribute.tag);
        if (attr == null) {
            throw new NotFoundException("cannot get LocalVariableAttribute");
        }
        String[] paramNames = new String[cm.getParameterTypes().length];
        int pos = Modifier.isStatic(cm.getModifiers()) ? 0 : 1;
        for (int i = 0; i < paramNames.length; i++) {
            paramNames[i] = attr.variableName(i + pos);
        }
        return paramNames;
    }

    public static String[] getMethodParamNames(final Method m) throws IOException {
        // 获取方法的参数名
        final String[] paramNames = new String[m.getParameterTypes().length];
        final String n = m.getDeclaringClass().getName();
        final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        ClassReader cr = new ClassReader(n);
        cr.accept(new ClassVisitor(Opcodes.ASM5, cw) {
            @Override
            public MethodVisitor visitMethod(final int access,
                                             final String name,
                                             final String desc,
                                             final String signature,
                                             final String[] exceptions) {

                final Type[] args = Type.getArgumentTypes(desc);
                // 方法名相同并且参数个数相同
                if (!name.equals(m.getName())
                        || !sameType(args, m.getParameterTypes())) {
                    return super.visitMethod(access, name, desc, signature,
                            exceptions);
                }
                MethodVisitor v = cv.visitMethod(access, name, desc, signature,
                        exceptions);
                return new MethodVisitor(Opcodes.ASM5, v) {
                    @Override
                    public void visitLocalVariable(String name,
                                                   String desc,
                                                   String signature,
                                                   Label start,
                                                   Label end,
                                                   int index) {
                        int i = index - 1;
                        // 如果是静态方法，则第一就是参数
                        // 如果不是静态方法，则第一个是"this"，然后才是方法的参数
                        if (Modifier.isStatic(m.getModifiers())) {
                            i = index;
                        }
                        if (i >= 0 && i < paramNames.length) {
                            paramNames[i] = name;
                        }
                        super.visitLocalVariable(name, desc, signature, start, end, index);
                    }
                };
            }
        }, 0);
        return paramNames;
    }
}
