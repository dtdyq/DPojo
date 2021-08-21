import pojo.dyq.util.Aspect;
import pojo.dyq.util.AspectPoint;
import pojo.dyq.util.ReflectUtil;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Map;

public class TestReflect {
    @Test
    public void testAspect() {
        Greeting greet = ReflectUtil.aspect(Greeting.class, new Greeter(), new Aspect<Greeting>() {
            @Override
            public int point() {
                return AspectPoint.AFTER + AspectPoint.BEFORE;
            }

            @Override
            public void before(Greeting proxy, Method method, Object[] args) {
                System.out.println("greeting begin:" + proxy);
            }

            @Override
            public void after(Greeting o, Method method, Object[] args, Object ret) {
                System.out.println("greeting finish:" + o);
            }
        });
        greet.greet();
        /*
         * output:
         * 
         * greeting begin:Greeter@3f8f9dd6
         * greeted
         * greeting finish:Greeter@3f8f9dd6
         */
    }

    @Test
    public void testConstructor() {
        System.out.println(ReflectUtil.instance(User.class));

        User user = ReflectUtil.instance(User.class, "alan", 23).orElse(null);
        System.out.println(user);
        Assert.assertNotNull(user);
        Assert.assertEquals("alan", user.getName());
        Assert.assertEquals(new Integer(23), user.getAge());

        user = ReflectUtil.instance(User.class, "alan").orElse(null);
        Assert.assertNull(user);
    }
    @Test
    public void testField(){
        Assert.assertTrue(ReflectUtil.hasField(User.class, "name"));
        User u = new User("alan", 12);
        Map<String, Object> fMap = ReflectUtil.fieldValues(u);
        System.out.println(fMap);
        /*
        output:
        {name=alan, age=12}
         */
    }

    @Test
    public void testPropsCopy(){
        User u =new User("york", 2);
        Dog dog = new Dog();
        ReflectUtil.copyProps(u,dog);
        Assert.assertEquals("york", dog.getName());
        Assert.assertEquals(2, dog.getAge());
    }
}
