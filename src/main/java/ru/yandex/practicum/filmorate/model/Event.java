package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Event {
    long eventId;
    long timestamp;
    long userId;
    EventType eventType;
    Operation operation;
    long entityId;

}
