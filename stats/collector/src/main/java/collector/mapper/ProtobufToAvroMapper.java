package collector.mapper;

import com.google.protobuf.Timestamp;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.grpc.ewm.stats.message.ActionTypeProto;
import ru.practicum.grpc.ewm.stats.message.UserActionProto;

import java.time.Instant;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ProtobufToAvroMapper {
    UserActionAvro toAvro(UserActionProto userActionProto);

    default ActionTypeAvro toAvroActionType(ActionTypeProto actionTypeProto) {
        if (actionTypeProto == null) {
            throw new IllegalArgumentException("ActionTypeProto не может быть null.");
        }

        return switch (actionTypeProto) {
            case ACTION_VIEW -> ActionTypeAvro.VIEW;
            case ACTION_REGISTER -> ActionTypeAvro.REGISTER;
            case ACTION_LIKE -> ActionTypeAvro.LIKE;
            default -> throw new IllegalArgumentException("Неизвестный тип ActionTypeProto: " + actionTypeProto);
        };
    }

    default Instant toInstant(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
    }
}
