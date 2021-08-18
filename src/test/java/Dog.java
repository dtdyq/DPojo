import lombok.Data;
import lombok.ToString;

@Data
@ToString(callSuper = true)
public class Dog extends Animal {
    private static final String type = "DOG";

    private String name;

    private int age;
}
