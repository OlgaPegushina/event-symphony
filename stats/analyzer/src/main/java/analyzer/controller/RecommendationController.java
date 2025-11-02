package analyzer.controller;

import analyzer.service.impl.RecommendationsService;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.grpc.ewm.dashboard.analyzer.RecommendationsControllerGrpc;
import ru.practicum.grpc.ewm.dashboard.message.InteractionsCountRequestProto;
import ru.practicum.grpc.ewm.dashboard.message.RecommendedEventProto;
import ru.practicum.grpc.ewm.dashboard.message.SimilarEventsRequestProto;
import ru.practicum.grpc.ewm.dashboard.message.UserPredictionsRequestProto;

import java.util.List;

@GrpcService
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@SuppressWarnings("unused")
public class RecommendationController extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {
    RecommendationsService recommendationsService;

    @Override
    public void getRecommendationsForUser(UserPredictionsRequestProto userPredictionsRequestProto,
                                          StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            log.info("Начинаем обрабатывать запрос на получение пользовательских рекомендаций");
            List<RecommendedEventProto> eventProtos =
                    recommendationsService.getRecommendationsForUser(userPredictionsRequestProto);
            eventProtos.forEach(responseObserver::onNext);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e)
            ));
        }
    }

    @Override
    public void getSimilarEvents(SimilarEventsRequestProto similarEventsRequestProto, StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            log.info("Начинаем обрабатывать запрос на получение похожих событий");
            List<RecommendedEventProto> eventProtos =
                    recommendationsService.getSimilarEvents(similarEventsRequestProto);
            eventProtos.forEach(responseObserver::onNext);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e)
            ));
        }
    }

    @Override
    public void getInteractionsCount(InteractionsCountRequestProto interactionsCountRequestProto,
                                     StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            log.info("Начинаем обрабатывать запрос на получение количества взаимодействий");
            List<RecommendedEventProto> eventProtos =
                    recommendationsService.getInteractionsCount(interactionsCountRequestProto);
            eventProtos.forEach(responseObserver::onNext);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e)
            ));
        }
    }
}
