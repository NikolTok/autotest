package generators;

import com.github.curiousoddman.rgxgen.RgxGen;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class RandomModelGenerator {

    private static final Random random = new Random();

    public static <T> T generate(Class<T> clazz) {
        try {
            T instance = clazz.getDeclaredConstructor().newInstance();

            for (Field field : getAllFields(clazz)) {
                field.setAccessible(true);

                GeneratingRule rule = field.getAnnotation(GeneratingRule.class);

                Object value;

                if (rule != null) {
                    value = generateFromRegex(
                            rule.regexp(),
                            field.getType()
                    );
                } else {
                    value = generateRandomValue(field);
                }

                field.set(instance, value);
            }

            return instance;

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate entity", e);
        }
    }

    private static List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();

        while (clazz != null && clazz != Object.class) {
            fields.addAll(List.of(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }

        return fields;
    }

    private static Object generateRandomValue(Field field) {
        Class<?> type = field.getType();

        if (type.equals(String.class)) {
            return UUID.randomUUID()
                    .toString()
                    .substring(0, 8);

        } else if (type.equals(Integer.class) || type.equals(int.class)) {
            return random.nextInt(1000);

        } else if (type.equals(Long.class) || type.equals(long.class)) {
            return random.nextLong(1000);

        } else if (type.equals(Double.class) || type.equals(double.class)) {
            return random.nextDouble() * 100;

        } else if (type.equals(Boolean.class) || type.equals(boolean.class)) {
            return random.nextBoolean();

        } else if (type.equals(List.class)) {
            return generateRandomList(field);

        } else if (type.equals(Date.class)) {
            return new Date(
                    System.currentTimeMillis()
                            - random.nextInt(1_000_000_000)
            );

        } else {
            return generate(type);
        }
    }

    private static Object generateFromRegex(String regexp, Class<?> type) {
        RgxGen rgxGen = new RgxGen(regexp);

        String result = rgxGen.generate();

        if (type.equals(Integer.class) || type.equals(int.class)) {
            return Integer.parseInt(result);

        } else if (type.equals(Long.class) || type.equals(long.class)) {
            return Long.parseLong(result);

        } else if (type.equals(Double.class) || type.equals(double.class)) {
            return Double.parseDouble(result);

        } else {
            return result;
        }
    }

    private static List<?> generateRandomList(Field field) {
        Type genericType = field.getGenericType();

        if (!(genericType instanceof ParameterizedType)) {
            return Collections.emptyList();
        }

        ParameterizedType parameterizedType =
                (ParameterizedType) genericType;

        Type actualType =
                parameterizedType.getActualTypeArguments()[0];

        if (actualType.equals(String.class)) {
            return List.of(
                    UUID.randomUUID().toString().substring(0, 5),
                    UUID.randomUUID().toString().substring(0, 5)
            );
        }

        if (actualType instanceof Class<?>) {
            Class<?> elementType = (Class<?>) actualType;

            return List.of(
                    generate(elementType),
                    generate(elementType)
            );
        }

        return Collections.emptyList();
    }
}