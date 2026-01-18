package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;
import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
private final UserService userService;

    @PostMapping
    public User add(@RequestBody @Valid User user) {
        return userService.add(user);
    }

    @PutMapping
    public User update(@RequestBody @NonNull User user) {
        return userService.update(user);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable @Positive long id, @PathVariable long friendId) {
        userService.addFriend(id, friendId);
    }

    @GetMapping("/{id}")
    public User getById(@PathVariable @Positive long id) {
        return userService.getById(id);
    }

    @GetMapping("/{id}/friends")
    public List<User> getFriendsById(@PathVariable @Positive long id) {
        return userService.getFriendsById(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(@PathVariable @Positive long id, @PathVariable long otherId) {
        return userService.getCommonFriends(id, otherId);
    }

    @GetMapping
    public List<User> getAll() {
        return userService.getAll();
    }

    @GetMapping("/{id}/feed")
    public List<Event> getEvents(@PathVariable @Positive long id) {
        return userService.getEvents(id);
    }

    @DeleteMapping
    public void delete(@RequestBody @NonNull User user) {
       userService.delete(user);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void deleteFriend(@PathVariable @Positive long id, @PathVariable @Positive long friendId) {
        userService.deleteFriend(id, friendId);
    }

    @GetMapping("{id}/recommendations")
    public Collection<Film> getRecommendationsByUserId(@PathVariable("id") @Positive long userId) {
        return userService.getRecommendationsByUserId(userId);
    }
}
