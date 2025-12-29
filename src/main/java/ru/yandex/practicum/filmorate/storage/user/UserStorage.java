package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;
import java.util.List;

public interface UserStorage {
    User add(User user);

    User update(User user);

    List<User> getAll();

    User getById(long id);

    List<User> getFriendsById(long id);

    List<User> getCommonFriends(long id, long otherId);

    void delete(User user);
}
