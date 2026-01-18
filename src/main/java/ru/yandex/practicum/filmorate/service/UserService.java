package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {
    public final UserStorage userStorage;
    private final FilmStorage filmStorage;

    UserService(
            @Autowired @Qualifier("userDbStorage") UserStorage userStorage,
            @Autowired @Qualifier("filmDbStorage")FilmStorage filmStorage
    ) {
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
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
    }

    public void deleteFriend(long id, long friendId) {
        userStorage.deleteFriend(id, friendId);
    }

    public Collection<Film> getRecommendationsByUserId(long userId) {
        Map<Long, Set<Long>> allLikes = filmStorage.getAllLikes();

        Set<Long> targetUserLikes = allLikes.getOrDefault(userId, Collections.emptySet());

        if (targetUserLikes.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, Long> intersectionCounts = allLikes.entrySet().stream()
                .filter(entry -> entry.getKey() != userId)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .filter(targetUserLikes::contains)
                                .count()
                ));

        long maxIntersections = intersectionCounts.values().stream()
                .max(Long::compare)
                .orElse(0L);

        if (maxIntersections == 0) {
            return Collections.emptyList();
        }

        return intersectionCounts.entrySet().stream()
                .filter(entry -> entry.getValue() == maxIntersections)
                .flatMap(entry -> allLikes.get(entry.getKey()).stream())
                .filter(filmId -> !targetUserLikes.contains(filmId))
                .distinct()
                .map(filmStorage::getById)
                .collect(Collectors.toList());
    }
}