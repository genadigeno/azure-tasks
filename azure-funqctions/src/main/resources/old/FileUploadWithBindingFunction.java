package old;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;
import java.util.*;

public class FileUploadWithBindingFunction {

//    @FunctionName("UploadWithBinding")
    public HttpResponseMessage run(
            @HttpTrigger(
                    name = "req",
                    methods = {HttpMethod.POST},
                    authLevel = AuthorizationLevel.ANONYMOUS)
            HttpRequestMessage<Optional<byte[]>> request,
            @BlobOutput(
                    name = "target",
                    path = "uploads/{filename}",
                    connection = "AzureWebJobsStorage")
            OutputBinding<byte[]> outputItem,
            @BindingName("filename") String filename,
            final ExecutionContext context) {
        context.getLogger().info("File upload request received");

        byte[] fileContent = request.getBody()
                .orElseThrow(() -> new IllegalArgumentException("File content is required"));

        outputItem.setValue(fileContent);

        return request.createResponseBuilder(HttpStatus.OK)
                .body("File uploaded successfully to: uploads/" + filename)
                .build();
    }
}
