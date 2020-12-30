import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.io.StringWriter;

public class dataSerializer extends JsonSerializer<Project> {

    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public void serialize(Project project, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {


        StringWriter writer = new StringWriter();
        mapper.writeValue(writer, project);
        jsonGenerator.writeFieldName(writer.toString());

    }
}
