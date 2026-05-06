import java.lang.reflect.Method;

public class TestReflection {
    public static void main(String[] args) throws Exception {
        Class<?> clazz = Class.forName("org.springframework.kafka.annotation.RetryableTopic");
        for (Method m : clazz.getDeclaredMethods()) {
            System.out.println(m.getName() + " -> " + m.getReturnType().getName());
        }
    }
}

