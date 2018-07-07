package util;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.type.JavaType;

import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * JSON的序列化和反序列化
 */
public class JSON {
    private static ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.configure(SerializationConfig.Feature.WRITE_DATE_KEYS_AS_TIMESTAMPS, false);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.sss");
        dateFormat.setTimeZone(TimeZone.getDefault());
        mapper.setDateFormat(dateFormat);
    }

    /**
     * JSON的序列化
     * @param obj 序列化的对象
     * @return JSON字符串
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws IOException
     */
    public static String stringify(Object obj) throws JsonGenerationException, JsonMappingException, IOException {
        StringWriter sw = new StringWriter();
        JsonGenerator jsonGenerator = mapper.getJsonFactory().createJsonGenerator(sw);
        mapper.writeValue(jsonGenerator, obj);
        return sw.toString();
    }

    /**
     * 普通对象的反序列化
     * @param json JSON字符串
     * @param clazz 反序列化的目标类
     * @param <T> 反序列化的目标类
     * @return 反序列化后的对象实例
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws IOException
     */
    public static <T> T parse(String json, Class<T> clazz) throws JsonParseException, JsonMappingException, IOException {
        return mapper.readValue(json, clazz);
    }

    /**
     * 列表对象的反序列化
     * @param json JSON字符串
     * @param collectionClass 反序列化的列表类
     * @param elementClasses 反序列化的列表类的实体类
     * @param <T> 反序列化的列表类
     * @return 反序列化后的列表对象实例
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws IOException
     */
    public static <T> T parse(String json, Class<?> collectionClass, Class<T>... elementClasses) throws JsonParseException, JsonMappingException, IOException{
        JavaType javaType = mapper.getTypeFactory().constructParametricType(collectionClass, elementClasses);
        return mapper.readValue(json, javaType);
    }
}
