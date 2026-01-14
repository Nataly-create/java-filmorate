package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Service
public class UserService {
    public final UserStorage userStorage;

    UserService(@Autowired @Qualifier("userDbStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User add(User user) {
        user.validate();
        return userStorage.add(user);
    }

    public User update(User user) {
        user.validate();
        return userStorage.update(user);
    }

    public List<User> getAll() {
        return userStorage.getAll();
    }

    public User getById(long id) {
        return userStorage.getById(id);
    }

    public List<User> getFriendsById(long id) {
        return userStorage.getFriendsById(id);
    }

    public List<User> getCommonFriends(long id, long otherId) {
        return userStorage.getCommonFriends(id, otherId);
    }

    public void delete(User user) {
        userStorage.delete(user);
    }

    public void addFriend(long id, long friendId) {
        userStorage.addFriend(id, friendId);
        addEvent(id, friendId, EventType.FRIEND, Operation.ADD);
    }

    public void deleteFriend(long id, long friendId) {
        userStorage.deleteFriend(id, friendId);
        addEvent(id, friendId, EventType.FRIEND, Operation.REMOVE);
    }

    public void addEvent(long userId, long entityId, EventType eventType, Operation operation) {
        userStorage.addEvent(userId, entityId, eventType, operation);
    }

    public List<Event> getEvents(long userId) {
        return userStorage.getEvents(userId);
    }
}