### DPojo
在单元测试等场景中，DPojo可以用来快速构建和填充java pojo类，包括基本类型、枚举、普通pojo、泛型类、数组类型和泛型数组类型、父类和泛型父类等各种复杂对象，并提供接口，支持用户自定义指定字段或类型的value

使用示例：

1.生成基本类型
```java
@Test
public void testBasicType() {
    Byte i = POJO.inst().manufacturer(byte.class);
    System.out.println(i);
}
```
2.生成普通pojo
```java
@Test
public void testPojo() {
	User user = POJO.inst().manufacturer(User.class);
	System.out.println(user);
}
```
3.泛型
```java
@Test
public void testGeneric() {
	Cat<Dog> cat = POJO.inst().manufacturer(new TypeRef<Cat<Dog>>() { });
	System.out.println(cat);
}
```
4.数组类型
```java
@Test
public void testArray() {
	User[] users = POJO.inst().manufacturer(User[].class);
	System.out.println(Arrays.toString(users));
}
```
5.泛型数组类型
```java
@Test
public void testGenericArray() {
	Cat<Dog>[] cat = POJO.inst().manufacturer(new TypeRef<Cat<Dog>[]>() { });
	System.out.println(Arrays.toString(cat));
}
```
6.带父类类型
```java
@Test
public void testSuperClass() {
	Dog dog = POJO.inst().manufacturer(Dog.class);
	System.out.println(dog);
}
```
7.带泛型父类类型
```java
@Test
public void testSuperClassGeneric() {
	Garfield garfield = POJO.inst().manufacturer(Garfield.class);
	System.out.println(garfield);
}
```
8.枚举类型
```java
@Test
public void testEnum() {
    Color color = POJO.inst().manufacturer(Color.class);
    System.out.println(color);
}
```
9.为特定属性名称或class类型提供指定值,可使用生成器或key、value方式实现
```java
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
	    user.setAge((short) 111);
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
```
工具类：
```java
@Data
@ToString(callSuper = true)
public class Dog extends Animal {
    private static final String type = "DOG";

    private String name;

    private int age;
}


public enum Color {
    BLACK,
    GRAY
}


@Data
public class Cat<T> {
    private T t;

    private String name;
}


@Data
public class Animal {
    private String id;
}


@Data
@ToString(callSuper = true)
public class Garfield extends Cat<Dog> {
    private Color color;
}

@Data
public class User {
    private String name;

    private short age;
}
```
