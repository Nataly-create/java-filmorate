package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.controller.MpaController;
import ru.yandex.practicum.filmorate.exeption.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class MpaDbStorage implements MpaStorage {
    private static final Logger log = LoggerFactory.getLogger(MpaController.class);
    protected final JdbcTemplate jdbc;
    private final MpaRowMapper rowMapper;
    private static final String GET_ALL_QUERY = "SELECT * FROM mpa";
    private static final String GET_BY_ID_QUERY = "SELECT * FROM mpa WHERE mpa_id = ?";

    @Override
    public List<Mpa> getAll() {
        return jdbc.query(GET_ALL_QUERY, rowMapper);
    }

    @Override
    public Mpa getById(int id) {
        try {
            return jdbc.queryForObject(GET_BY_ID_QUERY, rowMapper, id);
        } catch (EmptyResultDataAccessException e) {
            log.warn("MPA mit id {} not found", id);
            throw new NotFoundException(id, "MPA");
        }
    }
}
