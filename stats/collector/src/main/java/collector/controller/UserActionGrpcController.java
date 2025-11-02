package collector.controller;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import collector.kafka.producer.UserActionProducer;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import collector.mapper.ProtobufToAvroMapper;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.grpc.ewm.stats.collector.UserActionControllerGrpc;
import ru.practicum.grpc.ewm.stats.message.UserActionProto;

@GrpcService
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@SuppressWarnings("unused")
public class UserActionGrpcController extends UserActionControllerGrpc.UserActionControllerImplBase {
    ProtobufToAvroMapper protobufToAvroMapper;
    UserActionProducer userActionProducer;

    @Override
    public void collectUserAction(UserActionProto request, StreamObserver<Empty> responseObserver) {
        log.info("Получено gRPC сообщение: {}", request);

        try {
            UserActionAvro avroMessage = protobufToAvroMapper.toAvro(request); //--маппинг
            userActionProducer.sendUserAction(avroMessage); //-- отправка в Kafka

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(new StatusRuntimeException(Status.INTERNAL.withDescription(e.getMessage())
                    .withCause(e)));
        }
    }
}
