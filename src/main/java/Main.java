import AlloyAnalyzerService.AlloyAnalyzerService;
import io.grpc.Server;
import io.grpc.ServerBuilder;

public class Main {
    public static void main(String[] args) throws Exception {
        int port = 8080;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port argument, using default 8080");
            }
        }
        Server server = ServerBuilder.forPort(port)
                .addService(new AlloyAnalyzerService())
                .build();
        server.start();
        System.out.println("Server started on port " + port);
        server.awaitTermination();
    }
}
