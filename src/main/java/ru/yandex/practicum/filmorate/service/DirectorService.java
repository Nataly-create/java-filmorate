package ru.yandex.practicum.filmorate.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.film.DirectorDbStorage;

import java.util.List;

@Service
public class DirectorService {
    private static final Logger log = LoggerFactory.getLogger(DirectorService.class);
    private final DirectorDbStorage directorStorage;

    @Autowired
    public DirectorService(DirectorDbStorage directorStorage) {
        this.directorStorage = directorStorage;
    }

    public List<Director> getAllDirectors() {
        log.debug("Получение списка всех режиссёров");
        return directorStorage.getAll();
    }

    public Director getDirectorById(long id) {
        log.debug("Получение режиссёра с id {}", id);
        return directorStorage.getById(id)
                .orElseThrow(() -> new IllegalArgumentException("Режиссёр с id " + id + " не найден"));
    }

    public Director createDirector(Director director) {
        log.debug("Создание нового режиссёра: {}", director.getName());
        Director createdDirector = directorStorage.create(director);
        log.info("Создан новый режиссёр: '{}' (id: {})", createdDirector.getName(), createdDirector.getId());
        return createdDirector;
    }

    public boolean directorExists(long id) {
        log.debug("Проверка существования режиссёра с id {}", id);
        return directorStorage.getById(id).isPresent();
    }

    public Director updateDirector(Director director) {
        log.debug("Обновление режиссёра с id {}", director.getId());
        if (!directorExists(director.getId())) {
            throw new IllegalArgumentException("Режиссёр с id " + director.getId() + " не найден");
        }
        Director updatedDirector = directorStorage.update(director);
        log.info("Обновлён режиссёр: '{}' (id: {})", updatedDirector.getName(), updatedDirector.getId());
        return updatedDirector;
    }

    public void deleteDirector(long id) {
        log.debug("Удаление режиссёра с id {}", id);
        if (!directorExists(id)) {
            throw new IllegalArgumentException("Режиссёр с id " + id + " не найден");
        }
        directorStorage.delete(id);
        log.info("Удалён режиссёр с id {}", id);
    }
}
