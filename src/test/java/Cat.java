import lombok.Data;

@Data
public class Cat<T> {
    private T t;

    private String name;
}
