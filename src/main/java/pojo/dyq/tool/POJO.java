package pojo.dyq.tool;

import pojo.dyq.util.ReflectException;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

public final class POJO {

    private static final Generator NULL_GEN = new Generator() {
        @Override
        public Type type() {
            return null;
        }

        @Override
        public Object value() {
            return null;
        }
    };

    private static final Supplier NULL_SUP = new Supplier() {
        @Override
        public String name() {
            return null;
        }

        @Override
        public Object value() {
            return null;
        }
    };

    private final Random random = new Random();

    private final List<Generator> generators = new ArrayList<>();

    private final List<Supplier> suppliers = new ArrayList<>();

    private final Map<String, Object> fieldValMap = new HashMap<>();

    private final Map<Class<?>, Object> clzValMap = new HashMap<>();

    private int minBound = 1;

    private int maxBound = 10;

    private POJO() {
    }

    private static int randInt(int min, int max) {
        return new Random().nextInt((max - min) + 1) + min;
    }

    public static POJO inst() {
        return new POJO();
    }

    public POJO with(Generator generator) {
        generators.add(generator);
        return this;
    }

    public POJO with(Supplier supplier) {
        suppliers.add(supplier);
        return this;
    }

    public POJO with(String field, Object obj) {
        fieldValMap.put(field, obj);
        return this;
    }

    public <Z> POJO with(Class<Z> clz, Z obj) {
        clzValMap.put(clz, obj);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> T manufacturer(TypeRef<T> ref) {
        try {
            ParameterizedType clz = (ParameterizedType) ref.getClass().getGenericSuperclass();
            return (T) generate(new TypeDesc(clz.getActualTypeArguments()[0]));
        } catch (Exception e) {
            throw new ReflectException(e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T manufacturer(Class<T> clz) {
        try {
            return (T) generate(new TypeDesc(clz));
        } catch (Exception e) {
            throw new ReflectException(e.getMessage());
        }
    }

    private Object generate(TypeDesc typeDesc)
        throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        Object o = clzValMap.getOrDefault(typeDesc.type, null);
        o = o == null ? fieldValMap.getOrDefault(typeDesc.typeName, null) : o;
        o = o == null
            ? generators.stream().filter(gen -> gen.type() == typeDesc.type).findFirst().orElse(NULL_GEN).value()
            : o;
        o = o == null ? typeDesc.typeName == null ? null
            : suppliers.stream().filter(s -> typeDesc.typeName.equals(s.name())).findFirst().orElse(NULL_SUP).value()
            : o;
        o = o == null ? fromBaseType(typeDesc.type) : o;
        o = o == null ? fromArray(typeDesc.type) : o;
        o = o == null ? fromArrayGeneric(typeDesc.type) : o;
        if (o != null) {
            return o;
        }
        TypeDesc desc = parameterizedDesc(typeDesc.type);
        desc = desc == null ? typeDesc : desc;
        fromClass(desc);
        return desc.inst;
    }

    private void fromClass(TypeDesc typeDesc)
        throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        if (typeDesc.type instanceof Class<?>) {
            Class<?> clz = (Class<?>) typeDesc.type;
            Object obj = typeDesc.inst;
            if (obj == null) {
                Constructor<?> con = clz.getDeclaredConstructor();
                con.setAccessible(true);
                obj = con.newInstance();
            }
            for (Field field : clz.getDeclaredFields()) {
                if (Modifier.isFinal(field.getModifiers()) && Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                field.setAccessible(true);
                if (typeDesc.argsType.containsKey(field.getGenericType().toString())) {
                    field.set(obj, generate(
                        new TypeDesc(typeDesc.argsType.get(field.getGenericType().toString()), field.getName())));
                } else {
                    field.set(obj, generate(new TypeDesc(field.getGenericType(), field.getName())));
                }
            }
            typeDesc.inst = obj;
            Type sc = clz.getGenericSuperclass();
            while (sc != null && !"java.lang.Object".equals(sc.getTypeName())) {
                TypeDesc desc = parameterizedDesc(sc);
                typeDesc.type = desc.type;
                typeDesc.argsType.putAll(desc.argsType);
                fromClass(typeDesc);
                sc = ((Class<?>) desc.type).getGenericSuperclass();
            }
        }
    }

    private TypeDesc parameterizedDesc(Type type) {
        if (type instanceof ParameterizedType) {
            ParameterizedType pType = (ParameterizedType) type;
            TypeDesc sub = new TypeDesc(pType.getRawType());
            Class<?> clz = (Class<?>) pType.getRawType();
            List<TypeVariable<? extends Class<?>>> params =
                Arrays.stream(clz.getTypeParameters()).collect(Collectors.toList());
            List<Type> acts = Arrays.stream(pType.getActualTypeArguments()).collect(Collectors.toList());
            sub.argsType
                .putAll(params.stream().collect(Collectors.toMap(Type::getTypeName, k -> acts.get(params.indexOf(k)))));
            return sub;
        }
        return new TypeDesc(type);
    }

    private Object fromArray(Type type)
        throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        if (type instanceof Class && ((Class<?>) type).isArray()) {
            int len = randInt(minBound, maxBound);
            Object array = Array.newInstance(((Class<?>) type).getComponentType(), len);
            for (int i = 0; i < len; i++) {
                Array.set(array, i, generate(new TypeDesc(((Class<?>) type).getComponentType())));
            }
            return array;
        }
        return null;
    }

    private Object fromArrayGeneric(Type type)
        throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        if (type instanceof GenericArrayType) {
            GenericArrayType gType = (GenericArrayType) type;
            int len = randInt(minBound, maxBound);
            Object array =
                Array.newInstance((Class<?>) ((ParameterizedType) gType.getGenericComponentType()).getRawType(), len);
            for (int i = 0; i < len; i++) {
                Array.set(array, i, generate(new TypeDesc(gType.getGenericComponentType())));
            }
            return array;
        }
        return null;
    }

    private Object fromBaseType(Type type) {
        if (type == int.class || type == Integer.class) {
            return random.nextInt();
        }
        if (type == short.class || type == Short.class) {
            return Short.valueOf(String.valueOf(randInt(Short.MIN_VALUE, Short.MAX_VALUE)));
        }
        if (type == byte.class || type == Byte.class) {
            return Byte.valueOf(String.valueOf(randInt(Byte.MIN_VALUE, Byte.MAX_VALUE)));
        }
        if (type == boolean.class || type == Boolean.class) {
            return random.nextBoolean();
        }
        if (type == long.class || type == Long.class) {
            return random.nextLong();
        }
        if (type == double.class || type == Double.class) {
            return random.nextDouble();
        }
        if (type == float.class || type == Float.class) {
            return (float) Math.random() * (Float.MAX_VALUE - Float.MIN_VALUE) + Float.MIN_VALUE;
        }
        if (type == char.class || type == Character.class) {
            return (char) randInt(Character.MIN_VALUE, Character.MAX_VALUE);
        }
        if (type == String.class) {
            String s = UUID.randomUUID().toString().replaceAll("-", "");
            return s.substring(0, randInt(minBound, maxBound));
        }
        if (isEnum(type)) {
            return generateEnum(type);
        }
        return null;
    }

    private boolean isEnum(Type type) {
        return type instanceof Class<?> && ((Class<?>) type).isEnum();
    }

    @SuppressWarnings("unchecked")
    private Object generateEnum(Type type) {
        Class c = (Class<?>) type;
        List<String> keys = Arrays.stream(c.getEnumConstants()).map(Object::toString).collect(Collectors.toList());
        return Enum.valueOf(c, keys.get(randInt(0, keys.size() - 1)));
    }

    private final static class TypeDesc {
        private Object inst;

        private Type type;

        private String typeName;

        private Map<String, Type> argsType = new HashMap<>();

        private TypeDesc(Type type) {
            this.type = type;
        }

        private TypeDesc(Type type, String name) {
            this.type = type;
            this.typeName = name;
        }
    }

}
