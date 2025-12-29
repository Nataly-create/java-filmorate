# Project java-filmorate
### Here is a database schema used in the project and examples of SQL queries.
![Database schema.](./docs/images/Database_schema.svg)
'''
INSERT INTO users (name, login, email, birthday)
VALUES ('John Doe', 'johndoe', 'john@example.com', '1990-05-15');

INSERT INTO friends (user_id, friend_id, confirmed)
VALUES (1, 2, FALSE);

INSERT INTO films (name, description, mpa_id, release_date, duration)
VALUES ('Film', 'Description', 3, '2010-07-16', 148);

SELECT u.* FROM users u
JOIN likes l ON u.user_id = l.user_id
WHERE l.film_id = 5;

'''