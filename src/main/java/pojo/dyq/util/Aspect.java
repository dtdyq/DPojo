package pojo.dyq.util;

import com.sun.istack.internal.Nullable;

import java.lang.reflect.Method;

/**
 * implement this inf to user aspect func
 *
 * @param <T>
 */
public interface Aspect<T> {
    /**
     * assign the aspect point
     * 
     * @see AspectPoint
     * @return aspect point
     */
    int point();

    /**
     * invoke before method call
     * 
     * @param o proxy obj
     * @param method target method
     * @param args method ags
     */
    default void before(T o, Method method, @Nullable Object[] args) {
    }

    /**
     * invoke after method call
     *
     * @param o proxy obj
     * @param method target method
     * @param args method ags
     */
    default void after(T o, Method method, @Nullable Object[] args, @Nullable Object ret) {
    }

    /**
     * invoke when method call occur exception
     *
     * @param o proxy obj
     * @param method target method
     * @param args method ags
     * @param e exception
     */
    default void onException(T o, Method method, @Nullable Object[] args, @Nullable Throwable e) {
    }

    /**
     * invoke after method exit
     *
     * @param o proxy obj
     * @param method target method
     * @param args method ags
     * @param ret method ret val
     */
    default void onReturn(T o, Method method, @Nullable Object[] args, @Nullable Object ret) {
    }
}
