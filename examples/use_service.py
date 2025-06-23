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

def run_alloy_analysis(filename, command="run showHello for 1"):
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
        if hasattr(response, 'dot') and hasattr(response, 'json'):
            print("DOT output:")
            print(response.dot)
            print("\nJSON output:")
            print(response.json)
            # Save DOT file in the same directory as the input .als file
            als_path = Path(filename)
            dot_path = als_path.with_suffix('.dot')
            dot_path = als_path.parent / dot_path.name
            with open(dot_path, 'w') as f:
                f.write(response.dot)
            # Generate SVG from DOT using Graphviz (requires 'dot' installed)
            svg_path = als_path.with_suffix('.svg')
            svg_path = als_path.parent / svg_path.name
            os.system(f'dot -Tsvg "{dot_path}" -o "{svg_path}"')
            print(f"DOT file saved to: {dot_path}")
            print(f"SVG file generated at: {svg_path}")
        else:
            # fallback for old server
            print(getattr(response, 'result', response))

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print(f"Usage: {sys.argv[0]} <alloy_model_file> [alloy_command]")
        sys.exit(1)
    filename = sys.argv[1]
    command = sys.argv[2] if len(sys.argv) > 2 else None
    if command is None:
        # Use a valid default command for hello.als
        command = "run showHello for 1"
    run_alloy_analysis(filename, command)
