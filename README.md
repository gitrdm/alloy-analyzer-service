# alloy-analyzer-service

This is a fork of the Alloy Analyzer Service project, which provides a gRPC-based service for analyzing Alloy models and exporting results as DOT graphs for visualization and JSON for further downstream processing. I forked this project to add a Python client example as an experiment in using LLM modeling partners and formal design methods. Since the original project was not actively maintained, I also updated the dependencies and fixed some issues to ensure it works with the latest versions of Java and Maven.

I do not plan to maintain this fork long-term, but I hope it can serve as a useful example for others interested in using Alloy with gRPC and Python clients.

## Features
- Accepts Alloy model files and analysis commands via gRPC
- Uses the MIT Alloy Analyzer for model analysis
- Converts analysis results to DOT format for graph visualization
- Converts analysis results to JSON for downstream processing
- Containerized with Docker and Docker Compose

## Requirements
- Java 21
- Maven
- Docker & Docker Compose (for containerized deployment)

## Installation

### 1. Clone the repository
```bash
git clone <your-repo-url>
cd alloy-analyzer-service
```

### 2. Build with Maven
```bash
mvn clean package
```

### 3. Run with Docker Compose
```bash
docker compose up --build
```
This will build and start the service on port 8080.

Alternatively, you can run locally:
```bash
java -cp target/AlloyAnalyzerService-1.0-SNAPSHOT.jar:lib/org.alloytools.alloy.dist.jar Main
```

### 3. (Optional) Set the gRPC server port if not using the default (8080)
   Start the server on a custom port, e.g. 50051:
```bash
java -cp target/AlloyAnalyzerService-1.0-SNAPSHOT.jar:lib/org.alloytools.alloy.dist.jar Main 50051
```

Then set the client to use the same port:
```bash
export ALLOY_ANALYZER_SERVICE_PORT=50051
```

## Usage

### gRPC API
The service exposes a gRPC API defined in `src/main/proto/filestream.proto`:
- `UploadAndAnalyze(stream FileStreamRequest) returns (stream AnalysisResult)`

#### Example client flow:
1. Stream an Alloy model file and analysis command to the service
2. Receive streamed analysis results (including DOT format)

#### Regenerate gRPC stubs after editing proto:
```bash
mvn clean compile
```

### Visualizing DOT Output
- Use https://dreampuf.github.io/GraphvizOnline/ to visualize DOT-formatted strings returned by the service.

## Example: Using the Service from Python

A sample Python script is provided in `examples/use_service.py` to send an Alloy model file to the service and print the analysis result.

**How to use:**
1. Ensure the gRPC server is running (see above for how to start the service).
2. Generate the Python gRPC client stubs from `src/main/proto/filestream.proto` using `grpcio-tools`:
   ```bash
   python3 -m grpc_tools.protoc -I../src/main/proto --python_out=. --grpc_python_out=. ../src/main/proto/filestream.proto
   ```
   Place the generated `filestream_pb2.py` and `filestream_pb2_grpc.py` in the same directory as `use_service.py` or in your Python path.
3. Run the script:
   ```bash
   python3 examples/use_service.py examples/hello.als
   ```
   This will send the `hello.als` Alloy model to the service and print the streamed analysis results.

## Python Client Quickstart (with Conda & Makefile)

A Makefile is provided for easy setup and usage of the Python client example:

```bash
# 1. Create the Conda environment (only needed once)
make env

# 2. Generate Python gRPC stubs from the proto file
make grpc-stubs

# 3. Run the example client (ensure the gRPC server is running)
make run-client

# 4. Clean up generated files and environment (optional)
make clean
```

- The Conda environment is defined in `examples/environment.yml` (env name: `alloy-analyzer-client`).
- The Makefile automates environment setup, stub generation, and running the client script.
- The client script (`examples/use_service.py`) will send `examples/hello.als` to the service and print the results.

## Development
- Source code: `src/main/java/AlloyAnalyzerService/`
- DOT export logic: `src/main/java/AlloyAnalyzerService/DotExporter/`
- Proto definition: `src/main/proto/filestream.proto`

## Credits
- Alloy to DOT conversion logic inspired by: https://github.com/AlloyTools/org.alloytools.alloy/issues/211

## TODO
- See `TODO.md` for planned features and improvements.

# Running the Service

To run the service, use the following command:

```bash
java -jar target/AlloyAnalyzerService.jar [port]
```

- This will start the gRPC server on the specified port (default: 8080).
- If you want to use a custom port (e.g. 50051), specify it as an argument:

```bash
java -jar target/AlloyAnalyzerService.jar 50051
```

- In another terminal, set the client to use the same port:

```bash
export ALLOY_ANALYZER_SERVICE_PORT=50051
```

- Then, you can run the client using `make run-client` or any other method you prefer.

## Example: Running Alloy Analyzer Service with the Python Client

To analyze an Alloy model and generate DOT, SVG, and JSONL outputs:

1. **Start the gRPC server** (in a separate terminal):
   ```sh
   make run-server PORT=5032
   # or
   java -jar target/AlloyAnalyzerService.jar 5032
   ```

2. **Run the Python client with your Alloy model and command:**
   ```sh
   # Example for a 'run' command (produces multiple solutions):
   ALLOY_ANALYZER_SERVICE_PORT=5032 python3 examples/use_service.py examples/alloy-analyzer-service.als "run ShowTrace"

   # Example for a 'check' command (for assertions):
   ALLOY_ANALYZER_SERVICE_PORT=5032 python3 examples/use_service.py examples/alloy-analyzer-service.als "check IsNeverStuck"
   ```

3. **Output files:**
   - For each solution, you will get:
     - `alloy-analyzer-service.0.dot`, `alloy-analyzer-service.1.dot`, ... (DOT files)
     - `alloy-analyzer-service.0.svg`, `alloy-analyzer-service.1.svg`, ... (SVG images)
   - All JSON outputs are appended as lines to `alloy-analyzer-service.jsonl` (JSON Lines format, one solution per line).
   - For assertion checks with no counterexample, the files will contain a status/message indicating the assertion holds.

4. **Visualize DOT or SVG files:**
   - Open `.dot` files with [Graphviz Online](https://dreampuf.github.io/GraphvizOnline/) or any Graphviz tool.
   - Open `.svg` files in your browser or image viewer.

5. **Process JSONL output:**
   - Each line in the `.jsonl` file is a JSON object for one solution, suitable for LLM or automation workflows.

**Tip:** You can use any Alloy model and command; just adjust the file and command arguments accordingly.
