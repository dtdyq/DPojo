package pojo.dyq.tool;

import lombok.Data;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

@Data
public class FieldDesc<T> {
    private Class<T> clz;

    private String name;

    private Field origin;

    private int flag;

    public boolean isFinal(){
        return Modifier.isFinal(flag);
    }
    public boolean isPrivate(){
        return Modifier.isPrivate(flag);
    }
    public boolean isStatic(){
        return Modifier.isStatic(flag);
    }
    public boolean isPublic(){
        return Modifier.isPublic(flag);
    }
}
