### DPojo
在单元测试等场景中，DPojo可以用来快速构建和填充java pojo类，包括基本类型、普通pojo、泛型类、数组类型和泛型数组类型、父类和泛型父类等各种复杂对象
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
```
3.泛型
```java
```
4.数组类型
```java
```
5.泛型数组类型
```java
```
6.带父类类型
```java
```
7.带泛型父类类型
```java
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
import lombok.Data;

@Data
public class Cat<T> {
    private T t;

    private String name;
}
import lombok.Data;

@Data
public class Animal {
    private String id;
}
import lombok.Data;
import lombok.ToString;

@Data
@ToString(callSuper = true)
public class Garfield extends Cat<Dog> {
    private Color color;
}

```
