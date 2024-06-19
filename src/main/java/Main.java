import AlloyAnalyzerService.AlloyAnalyzerService;
import io.grpc.Server;
import io.grpc.ServerBuilder;

public class Main {
    public static void main(String[] args) throws Exception {
        Server server = ServerBuilder.forPort(8080)
                .addService(new AlloyAnalyzerService())
                .build();
        server.start();
        System.out.println("Server started on port 8080");
        server.awaitTermination();
    }
}
