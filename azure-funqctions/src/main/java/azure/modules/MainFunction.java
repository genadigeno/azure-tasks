package azure.modules;

import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;

public class MainFunction {

    @FunctionName("helloFunction")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.GET}, authLevel = AuthorizationLevel.ANONYMOUS)
            HttpRequestMessage<String> request,
            final ExecutionContext context
    ){

        context.getLogger().info("Java Azure Function executed.");
        String name = request.getQueryParameters().get("name");

        return request.createResponseBuilder(HttpStatus.OK)
                .body("Hello, " + (name != null ? name : "World") + "!")
                .build();
    }
}