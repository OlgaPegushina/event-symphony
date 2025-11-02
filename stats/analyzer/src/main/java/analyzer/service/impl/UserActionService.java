package analyzer.service.impl;

import analyzer.config.WeightProperties;
import analyzer.mapper.UserActionMapper;
import analyzer.model.ActionType;
import analyzer.model.UserAction;
import analyzer.repository.UserActionRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserActionService implements analyzer.service.UserActionService {
    WeightProperties weightProperties;
    UserActionRepository userActionRepository;
    UserActionMapper userActionMapper;

    @Transactional
    @Override
    public void handleUserAction(UserActionAvro avro) {
        log.info("Сохраняем действие: {} пользователя: {} для события {}", avro, avro.getUserId(), avro.getEventId());
        Optional<UserAction> userActionOpt = userActionRepository.findByUserIdAndEventId(avro.getUserId(),
                avro.getEventId());
        ActionType newType = avroTypeToEntity(avro.getActionType());

        if (userActionOpt.isPresent()) {
            UserAction userAction = userActionOpt.get();
            double weight = getWeightForAction(userAction.getActionType());
            double newWeight = getWeightForAction(newType);

            if (Double.compare(newWeight, weight) > 0) {
                userAction.setActionType(newType);
                userAction.setTimestamp(avro.getTimestamp());
                userAction.setActionWeight(newWeight);
            }
        } else {
            UserAction userAction = userActionMapper.AvroToEntity(avro, getWeightForAction(newType));
            userAction.setActionType(newType);
            userActionRepository.save(userAction);
        }
    }

    private ActionType avroTypeToEntity(ActionTypeAvro avroType) {
        return switch (avroType) {
            case VIEW   -> ActionType.VIEW;
            case REGISTER -> ActionType.REGISTER;
            case LIKE   -> ActionType.LIKE;
        };
    }

    private double getWeightForAction(ActionType actionType) {
        return switch (actionType) {
            case VIEW -> weightProperties.getView();
            case REGISTER -> weightProperties.getRegister();
            case LIKE -> weightProperties.getLike();
            default -> throw new IllegalArgumentException("Вес для типа " + actionType + " не определен.");
        };
    }
}
