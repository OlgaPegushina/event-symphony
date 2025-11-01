package stats.client;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.practicum.grpc.ewm.dashboard.analyzer.RecommendationsControllerGrpc;
import ru.practicum.grpc.ewm.dashboard.message.InteractionsCountRequestProto;
import ru.practicum.grpc.ewm.dashboard.message.RecommendedEventProto;
import ru.practicum.grpc.ewm.dashboard.message.SimilarEventsRequestProto;
import ru.practicum.grpc.ewm.dashboard.message.UserPredictionsRequestProto;

import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AnalyzerClient {

    @GrpcClient("analyzer")
    RecommendationsControllerGrpc.RecommendationsControllerBlockingStub server;

    public Stream<RecommendedEventProto> getRecommendationsForUser(Long userId, Long maxResults) {
        final UserPredictionsRequestProto request = UserPredictionsRequestProto.newBuilder()
                .setUserId(userId)
                .setMaxResult(maxResults)
                .build();
        final Iterator<RecommendedEventProto> iterator = server.getRecommendationsForUser(request);
        return asStream(iterator);
    }

    public Stream<RecommendedEventProto> getSimilarEvents(Long eventId, Long userId, Long maxResults) {
        final SimilarEventsRequestProto request = SimilarEventsRequestProto.newBuilder()
                .setEventId(eventId)
                .setUserId(userId)
                .setMaxResult(maxResults)
                .build();
        final Iterator<RecommendedEventProto> iterator = server.getSimilarEvents(request);
        return asStream(iterator);
    }

    public Stream<RecommendedEventProto> getInteractionsCount(List<Long> eventIds) {
        final InteractionsCountRequestProto request = InteractionsCountRequestProto.newBuilder()
                .addAllEventId(eventIds)
                .build();
        final Iterator<RecommendedEventProto> iterator = server.getInteractionsCount(request);
        return asStream(iterator);
    }

    private Stream<RecommendedEventProto> asStream(Iterator<RecommendedEventProto> iterator) {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED),
                false
        );
    }
}
