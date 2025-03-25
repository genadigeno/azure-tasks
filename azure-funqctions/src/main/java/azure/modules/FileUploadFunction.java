package azure.modules;

import com.azure.storage.blob.*;
import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class FileUploadFunction {

    @FunctionName("UploadFileToBlob")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.POST}, authLevel = AuthorizationLevel.ANONYMOUS)
            HttpRequestMessage<Optional<byte[]>> request,
            @BindingName("filename") String filename,
            final ExecutionContext context) {

        context.getLogger().info("File upload request received");

        try {
            Map<String, String> headers = request.getHeaders();
            if (headers.isEmpty()) {
                context.getLogger().info("No http headers received");
            }
            else {
                headers.forEach((key, value) -> context.getLogger().info("\t "+key + ": " + value));
            }

            // Get connection string from environment variables
            String connectionString = System.getenv("AzureWebJobsStorage");
            String containerName = "uploads";

            // Generate unique filename if not provided
            String blobName = filename != null ? filename : "file_" + UUID.randomUUID() + ".dat";
            context.getLogger().info("connectionString : " + connectionString);

            // Get file content from request body
            byte[] fileContent = request.getBody()
                    .orElseThrow(() -> new IllegalArgumentException("File content is required"));
            context.getLogger().info("file size : " + fileContent.length);

            // Upload to Blob Storage
            BlobClient blobClient = new BlobClientBuilder()
                    .connectionString(connectionString)
                    .containerName(containerName)
                    .blobName(blobName)
                    .buildClient();

            if (!blobClient.exists()) {
                context.getLogger().info("uploading a new blob...");
                blobClient.upload(new ByteArrayInputStream(fileContent), fileContent.length);
            }
            else {
                context.getLogger().warning("blob already exists");
                context.getLogger().info("deleting existing blob...");
                blobClient.delete();
                context.getLogger().info("the blob deleted successfully. uploading a new blob...");
                blobClient.upload(new ByteArrayInputStream(fileContent), fileContent.length);
            }

            return request.createResponseBuilder(HttpStatus.OK)
                    .body("File uploaded successfully. Blob name: " + blobName)
                    .build();

        } catch (Exception e) {
            context.getLogger().severe("Error uploading file: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error uploading file: " + e.getMessage())
                    .build();
        }
    }
}
