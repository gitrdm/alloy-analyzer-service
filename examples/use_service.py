#!/usr/bin/env python3
"""
Example client script to send an Alloy model file to the AlloyAnalyzerService gRPC server and print the analysis result.
This script uses the generated gRPC Python client stubs.
"""
import grpc
import sys
import os
from pathlib import Path

# Import generated classes (assumes they are available in PYTHONPATH)
import filestream_pb2
import filestream_pb2_grpc

def file_chunks(filepath, chunk_size=4096):
    with open(filepath, 'rb') as f:
        seq = 0
        while True:
            chunk = f.read(chunk_size)
            if not chunk:
                break
            yield filestream_pb2.FileStreamRequest(chunk=filestream_pb2.FileChunk(content=chunk, sequence=seq))
            seq += 1

def run_alloy_analysis(filename, command="run {} for 3"):  # Example command
    # Allow port override via environment variable, default to 8080
    port = os.environ.get("ALLOY_ANALYZER_SERVICE_PORT", "8080")
    channel = grpc.insecure_channel(f'localhost:{port}')
    stub = filestream_pb2_grpc.FileStreamStub(channel)
    # Send AlloyProperties first
    requests = [filestream_pb2.FileStreamRequest(props=filestream_pb2.AlloyProperties(command=command))]
    # Then send file chunks
    requests.extend(file_chunks(filename))
    responses = stub.UploadAndAnalyze(iter(requests))
    print("Analysis results:")
    for response in responses:
        print(response.result)

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print(f"Usage: {sys.argv[0]} <alloy_model_file> [alloy_command]")
        sys.exit(1)
    filename = sys.argv[1]
    command = sys.argv[2] if len(sys.argv) > 2 else None
    if command is None:
        # Try to guess a command from the filename (e.g., run Hello for 3)
        # Otherwise, default to 'run {} for 3'
        command = "run {} for 3"
    run_alloy_analysis(filename, command)
