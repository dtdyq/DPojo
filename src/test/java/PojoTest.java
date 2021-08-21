import pojo.dyq.tool.Generator;
import pojo.dyq.tool.POJO;
import pojo.dyq.tool.Supplier;
import pojo.dyq.tool.TypeRef;

import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.Arrays;

public class PojoTest {
    public static void main(String[] args) throws IOException {
        LocalDateTime pro = POJO.inst().manufacturer(LocalDateTime.class);
        System.out.println(pro);
    }

    @Test
    public void testBasicType() {
        Byte i = POJO.inst().manufacturer(byte.class);
        System.out.println(i);
    }

    @Test
    public void testEnum() {
        Color color = POJO.inst().manufacturer(Color.class);
        System.out.println(color);
    }

    @Test
    public void testPojo() {
        User user = POJO.inst().manufacturer(User.class);
        System.out.println(user);
    }

    @Test
    public void testArray() {
        User[] users = POJO.inst().manufacturer(User[].class);
        System.out.println(Arrays.toString(users));
    }

    @Test
    public void testGeneric() {
        Cat<Dog> cat = POJO.inst().manufacturer(new TypeRef<Cat<Dog>>() { });
        System.out.println(cat);
    }

    @Test
    public void testGenericArray() {
        Cat<Dog>[] cat = POJO.inst().manufacturer(new TypeRef<Cat<Dog>[]>() { });
        System.out.println(Arrays.toString(cat));
    }

    @Test
    public void testGenerator() {
        User cat = POJO.inst().with(new Generator() {
            @Override
            public Type type() {
                return User.class;
            }

            @Override
            public Object value() {
                User user = new User();
                user.setAge(111);
                user.setName("alan");
                return user;
            }
        }).manufacturer(User.class);
        System.out.println(cat);
    }

    @Test
    public void testSupplier() {
        Cat<Dog> cat = POJO.inst().with(new Supplier() {

            @Override
            public String name() {
                return "t";
            }

            @Override
            public Object value() {
                return new Dog();
            }
        }).manufacturer(new TypeRef<Cat<Dog>>() { });
        System.out.println(cat);
    }

    @Test
    public void testWithKV() {
        POJO pojo = POJO.inst().with(Dog.class, new Dog()).with("name", "assigned");
        Cat<Dog> cat = pojo.manufacturer(new TypeRef<Cat<Dog>>() { });
        System.out.println(cat);
        System.out.println(Object.class.getTypeName());
    }

    @Test
    public void testSuperClass() {
        Dog dog = POJO.inst().manufacturer(Dog.class);
        System.out.println(dog);
    }

    @Test
    public void testSuperClassGeneric() {
        Garfield garfield = POJO.inst().manufacturer(Garfield.class);
        System.out.println(garfield);
    }
}
