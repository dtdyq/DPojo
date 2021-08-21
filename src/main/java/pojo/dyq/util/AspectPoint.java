package pojo.dyq.util;

public class AspectPoint {
    public static final int BEFORE = 0x0001;
    public static final int AFTER = 0x0010;
    public static final int OnException = 0x0100;
    public static final int OnReturn = 0x1000;

    static boolean forBefore(int i) {
        return (BEFORE & i) != 0;
    }

    static boolean forAfter(int i) {
        return (AFTER & i) != 0;
    }

    static boolean forException(int i) {
        return (OnException & i) != 0;
    }

    static boolean forReturn(int i) {
        return (OnReturn & i) != 0;
    }
}
