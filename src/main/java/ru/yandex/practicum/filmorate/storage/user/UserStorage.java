package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.model.User;
import java.util.List;

public interface UserStorage {
    User add(User user);

    User update(User user);

    List<User> getAll();

    User getById(long id);

    void addFriend(long id, long friendId);

    List<User> getFriendsById(long id);

    List<User> getCommonFriends(long id, long otherId);

    void delete(User user);

    void deleteFriend(long id, long friendId);

    void addEvent(long userId, long entityId, EventType eventType, Operation operation);

    List<Event> getEvents(long id);
}
