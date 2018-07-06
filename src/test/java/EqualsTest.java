import com.chinadaas.dataflow.entities.Person;
import org.junit.jupiter.api.Test;

/**
 * @author xyf
 * @Data Created in 10:43 2018/6/5
 * @Descriptions
 */
public class EqualsTest {
    @Test
    public void equalsTest(){
        Person p1 = new Person();
        p1.setName("张三");
        p1.setId(12L);
        Person p2 = new Person();
        p2.setId(13L);
        p2.setName("张三");
        boolean equals = p1.equals(p2);
        int i = 0;
    }
}
