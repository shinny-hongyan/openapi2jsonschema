import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.ParseOptions;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;

public class Convertor {
    public static void GeneratorQuerySchema(Operation operation, String filePath) throws IOException {
        ObjectSchema schema = new ObjectSchema();
        List<Parameter> parameters = operation.getParameters();
        if (parameters != null) {
            for (int index = 0; index < parameters.size(); ++index) {
                Parameter parameter = parameters.get(index);
                schema.addProperty(parameter.getName(), parameter.getSchema());
            }
            SaveToFile(schema, filePath, "query.json");
        }
    }


    public static void GeneratorBodySchema(Operation operation, String filePath) throws IOException {
        RequestBody requestBody = operation.getRequestBody();
        if (requestBody != null) {
            ObjectSchema schema = (ObjectSchema) requestBody.getContent().get("application/json").getSchema();
            SaveToFile(schema, filePath, "body.json");
        }
    }

    public static void SaveToFile(Object schema, String filePath, String fileName) throws IOException {
        File directory = new File(String.valueOf(filePath));
        if (!directory.exists()) {
            directory.mkdirs();
        }
        try (Writer writer = new FileWriter(filePath + "/" + fileName)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(schema, writer);
        }
    }

    public static void main(String[] args) throws IOException {
        ParseOptions parseOptions = new ParseOptions();
        parseOptions.setResolve(true);
        parseOptions.setResolveFully(true);
        OpenAPI openAPI = new OpenAPIV3Parser().read("api.yaml", null, parseOptions);

        ApiGateway apiGateway = new ApiGateway("debug", 1, "/var/log/ef/api-gateway");
        apiGateway.setProxyAddr("api-gateway-node1", "10.250.60.1:80");
        apiGateway.setJWSPublicRSA("api-gateway-node1", "/etc/api-gateway/jws.pub");

        // 遍历所有的 paths 获取请求参数
        Paths paths = openAPI.getPaths();
        paths.forEach((path_name, pathItem) -> {
            Map<PathItem.HttpMethod, Operation> operations = pathItem.readOperationsMap();
            operations.forEach((method, operation) -> {
                String path = path_name.replace("{", ":").replace("}", "");

                ApiGateway.ProxyRuleItem item = new ApiGateway.ProxyRuleItem();
                item.Host = "127.0.0.1:1234";
                item.QuerySchema = "file:///etc/api-gateway/schema/" + path + "/" + method.toString() + "/query.json";
                item.BodySchema = "file:///etc/api-gateway/schema/" + path + "/" + method.toString() + "/body.json";
                apiGateway.addRoute("api-gateway-node1", path, method.toString(), item);

                String queryFilePath = System.getProperty("user.dir") + "/schema/" + path.replace(":", "") + "/" + method.toString() + "/";
                String bodyFilePath = System.getProperty("user.dir") + "/schema/" + path.replace(":", "") + "/" + method.toString() + "/";
                // json schema file
                try {
                    GeneratorQuerySchema(operation, queryFilePath);
                    GeneratorBodySchema(operation, bodyFilePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        });

        SaveToFile(apiGateway.getConfig(), System.getProperty("user.dir"), "api-gateway.json");
        System.out.println("generator success.");
    }
}
