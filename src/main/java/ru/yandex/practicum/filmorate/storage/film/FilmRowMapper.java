package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class FilmRowMapper implements RowMapper<Film> {
    private final MpaStorage mpaStorage;
    private final GenreDbStorage genreDbStorage;
    private final UserDbStorage userDbStorage;
    private final DirectorDbStorage directorDbStorage;

    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        Film film = Film.builder()
                .id(rs.getLong("film_id"))
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .mpa(mpaStorage.getById(rs.getInt("mpa_id")))
                .duration(rs.getInt("duration"))
                .releaseDate(Optional.ofNullable(rs.getDate("release_date"))
                        .map(Date::toLocalDate)
                        .orElse(null))
                .build();

        if (film != null) {
            film.setGenres(new HashSet<>(genreDbStorage.getGenresById((int) film.getId())));
            film.setLikes(new HashSet<>(userDbStorage.getLikesById((int) film.getId())));
            film.setDirectors(new HashSet<>(directorDbStorage.getDirectorsByFilmId((int) film.getId())));
        }
        return film;
    }
}
