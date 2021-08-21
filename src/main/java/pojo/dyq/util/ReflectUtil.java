package pojo.dyq.util;

import com.sun.istack.internal.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * reflect util class
 * 
 * @author dyq
 * @since 2021/3/2
 */
public final class ReflectUtil {
    /**
     * initialize T from giving class with default no args constructor
     * 
     * @param clz class
     * @param <T> type of class
     * @return instance of class
     */
    public static <T> Optional<T> instance(@NotNull Class<T> clz) {
        instanceCheck(clz);
        try {
            Constructor<T> con = clz.getDeclaredConstructor();
            con.setAccessible(true);
            return Optional.of(con.newInstance());
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException
            | IllegalAccessException e) {
            throw new ReflectException(e.getMessage());
        }
    }

    /**
     * initialize T from giving class with parameters,find the constructor which params count equals to args and each
     * param can assign from corresponding arg,or else return null
     * 
     * @param clz class to init
     * @param args constructor's args
     * @param <T> type
     * @return instance of T
     */
    @SuppressWarnings("unchecked")
    public static <T> Optional<T> instance(@NotNull Class<T> clz, Object... args) {
        instanceCheck(clz);
        if (args.length == 0) {
            return instance(clz);
        }
        List<Class<?>> argTypes = Arrays.stream(args).map(arg -> flipType(arg.getClass())).collect(Collectors.toList());
        return Stream.of((Constructor<T>[]) clz.getDeclaredConstructors())
            .peek(c -> c.setAccessible(true))
            .filter(c -> c.getParameterCount() == args.length)
            .filter(con -> {
                List<Class<?>> paraTypes =
                    Stream.of(con.getParameterTypes()).map(ReflectUtil::flipType).collect(Collectors.toList());
                return IntStream.range(0, paraTypes.size())
                    .allMatch(idx -> paraTypes.get(idx).isAssignableFrom(argTypes.get(idx)));
            })
            .findFirst()
            .map(con -> {
                try {
                    return con.newInstance(args);
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    throw new ReflectException(e.getMessage());
                }
            });
    }

    private static <T> void instanceCheck(@NotNull Class<T> clz) {
        if (clz.isInterface()) {
            throw new ReflectException("interface error");
        }
        if (Modifier.isAbstract(clz.getModifiers())) {
            throw new ReflectException("abstract class error");
        }
    }

    /**
     * set obj's field to giving value
     * 
     * @param origin object
     * @param name field name
     * @param val field val to set
     */
    public static void setFieldVal(@NotNull Object origin, @NotNull String name, @NotNull Object val) {
        try {
            Field field = origin.getClass().getDeclaredField(name);
            field.setAccessible(true);
            field.set(origin, val);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new ReflectException(e.getMessage());
        }
    }

    /**
     * obtain the giving field value from origin object
     * 
     * @param origin obj
     * @param name field name
     * @param <T> type
     * @return field type
     */
    @SuppressWarnings("unchecked")
    public static <T> T getFieldVal(@NotNull Object origin, @NotNull String name) {
        try {
            Field field = origin.getClass().getDeclaredField(name);
            field.setAccessible(true);
            return (T) field.get(origin);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new ReflectException(e.getMessage());
        }
    }

    /**
     * copy object src properties to object dest based on field,copy principle:field name equals 、 type is assignable
     * and not a final field
     * 
     * @param src source object
     * @param dest dest object
     */
    public static void copyProps(@NotNull Object src, @NotNull Object dest) {
        Class<?> srcC = src.getClass();
        Class<?> destC = dest.getClass();

        List<Field> sFields =
            Arrays.stream(srcC.getDeclaredFields()).peek(f -> f.setAccessible(true)).collect(Collectors.toList());
        Arrays.stream(destC.getDeclaredFields()).forEach(field -> {
            if (!Modifier.isFinal(field.getModifiers())) {
                sFields.stream().filter(f -> field.getName().equals(f.getName())).findFirst().ifPresent(sf -> {
                    try {
                        if (flipType(field.getType()).isAssignableFrom(flipType(sf.getType()))) {
                            field.setAccessible(true);
                            field.set(dest, sf.get(src));
                        }
                    } catch (IllegalAccessException e) {
                        // do nothing
                        e.printStackTrace();
                        throw new ReflectException("field set error" + e.getMessage());
                    }
                });
            }
        });
    }

    private static Class<?> flipType(Class<?> clz) {
        if (clz.isPrimitive()) {
            return clz;
        }
        try {
            if (hasField(clz, "TYPE")) {
                Class<?> o = (Class<?>) clz.getField("TYPE").get(null);
                if (o != null && o.isPrimitive()) {
                    return o;
                }
            }
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new ReflectException(e.getMessage());
        }
        return clz;
    }

    /**
     * return true if the giving class contains field fName
     * 
     * @param clz class
     * @param fName field name
     * @return bool
     */
    public static boolean hasField(@NotNull Class<?> clz, @NotNull String fName) {
        return Arrays.stream(clz.getDeclaredFields())
            .peek(f -> f.setAccessible(true))
            .anyMatch(field -> field.getName().equals(fName));
    }

    /**
     * return true if the giving class contains method mName
     * 
     * @param clz class
     * @param mName method name
     * @return bool
     */
    public static boolean hasMethod(@NotNull Class<?> clz, @NotNull String mName) {
        return Arrays.stream(clz.getDeclaredMethods())
            .peek(m -> m.setAccessible(true))
            .anyMatch(method -> method.getName().equals(mName));
    }

    /**
     * obtain the giving obj fields to map,key is field name and value is the field val
     * 
     * @param o object
     * @return map
     */
    public static Map<String, Object> fieldValues(@NotNull Object o) {
        return Arrays.stream(o.getClass().getDeclaredFields())
            .peek(f -> f.setAccessible(true))
            .collect(Collectors.toMap(Field::getName, f -> {
                try {
                    return f.get(o);
                } catch (IllegalAccessException e) {
                    throw new ReflectException(e.getMessage());
                }
            }));
    }

    /**
     * create a proxy object for giving instance
     * 
     * @param inf interface
     * @param o to be proxied obj
     * @param aspect aspect inf
     * @param <T> proxy object
     * @return proxy
     */
    @SuppressWarnings("unchecked")
    public static <T> T aspect(@NotNull Class<T> inf, @NotNull T o, @NotNull Aspect<T> aspect) {
        class ProxyHandler implements InvocationHandler {
            private final T o;

            private ProxyHandler(T o) {
                this.o = o;
            }

            @Override
            public Object invoke(Object proxy, Method method, Object[] args) {
                Object ret = null;
                if (AspectPoint.forBefore(aspect.point())) {
                    aspect.before(o, method, args);
                }
                try {
                    ret = method.invoke(o, args);
                    if (AspectPoint.forAfter(aspect.point())) {
                        aspect.after(o, method, args, ret);
                    }
                } catch (Throwable e) {
                    if (AspectPoint.forException(aspect.point())) {
                        aspect.onException(o, method, args, e);
                    }
                } finally {
                    if (AspectPoint.forReturn(aspect.point())) {
                        aspect.onReturn(o, method, args, ret);
                    }
                }
                return ret;
            }
        }
        return (T) Proxy.newProxyInstance(inf.getClassLoader(), new Class[] {inf}, new ProxyHandler(o));
    }
}
