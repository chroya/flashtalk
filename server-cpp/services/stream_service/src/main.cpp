#include <grpcpp/grpcpp.h>
#include "stream_service.h"
#include <memory>
#include <string>

int main(int argc, char** argv) {
    std::string server_address("0.0.0.0:50051");
    speech::StreamService service;

    grpc::ServerBuilder builder;
    builder.AddListeningPort(server_address, grpc::InsecureServerCredentials());
    builder.RegisterService(&service);

    std::unique_ptr<grpc::Server> server(builder.BuildAndStart());
    std::cout << "Server listening on " << server_address << std::endl;
    server->Wait();

    return 0;
} 