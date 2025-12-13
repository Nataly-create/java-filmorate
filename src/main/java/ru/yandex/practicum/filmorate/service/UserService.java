package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserStorage userStorage;

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
        return  userStorage.getFriendsById(id);
    }

    public List<User> getCommonFriends(long id, long otherId) {
        return userStorage.getCommonFriends(id, otherId);
    }

    public void delete(User user) {
        userStorage.delete(user);
    }

    public void addFriend(long id, long friendId) {
        userStorage.getById(id).getFriends().add(friendId);
        userStorage.getById(friendId).getFriends().add(id);
    }

    public void deleteFriend(long id, long friendId) {
        userStorage.getById(id).getFriends().remove(friendId);
        userStorage.getById(friendId).getFriends().remove(id);
    }

    public Set<Long> allFriends(User user) {
        return user.getFriends();
    }
}