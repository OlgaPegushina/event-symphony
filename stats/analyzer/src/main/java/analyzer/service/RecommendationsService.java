package analyzer.service;

import ru.practicum.grpc.ewm.dashboard.message.InteractionsCountRequestProto;
import ru.practicum.grpc.ewm.dashboard.message.RecommendedEventProto;
import ru.practicum.grpc.ewm.dashboard.message.SimilarEventsRequestProto;
import ru.practicum.grpc.ewm.dashboard.message.UserPredictionsRequestProto;

import java.util.List;

public interface RecommendationsService {
    List<RecommendedEventProto> getRecommendationsForUser(UserPredictionsRequestProto request);

    List<RecommendedEventProto> getSimilarEvents(SimilarEventsRequestProto request);

    List<RecommendedEventProto> getInteractionsCount(InteractionsCountRequestProto request);
}
