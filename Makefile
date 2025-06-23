# Makefile for Alloy Analyzer Service Python Client Example

.PHONY: help env grpc-stubs run-client run-server clean

PORT ?= 8080
ALLOY_COMMAND ?= run showHello for 1

help:
	@echo "Available targets:"
	@echo "  env         Create and activate the Conda environment (alloy-analyzer-client)"
	@echo "  grpc-stubs  Generate Python gRPC stubs from proto file"
	@echo "  run-server  Run the Java gRPC server (default: PORT=8080)"
	@echo "  run-client  Run the example client script with hello.als (uses PORT and ALLOY_COMMAND env vars)"
	@echo "  clean       Remove generated Python files and environment"

env:
	conda env create -f examples/environment.yml || echo "Environment may already exist."

grpc-stubs:
	cd examples && \
	python3 -m grpc_tools.protoc -I../src/main/proto --python_out=. --grpc_python_out=. ../src/main/proto/filestream.proto

run-server:
	java -jar target/AlloyAnalyzerService.jar $(PORT)

run-client:
	cd examples && \
	ALLOY_ANALYZER_SERVICE_PORT=$(PORT) conda run -n alloy-analyzer-client python use_service.py hello.als "$(ALLOY_COMMAND)"

clean:
	rm -f examples/filestream_pb2.py examples/filestream_pb2_grpc.py
	conda env remove -n alloy-analyzer-client || echo "Environment not found."
