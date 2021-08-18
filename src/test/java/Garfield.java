import lombok.Data;
import lombok.ToString;

@Data
@ToString(callSuper = true)
public class Garfield extends Cat<Dog> {
    private Color color;
}
