-- Drop tables if they exist to start fresh
DROP TABLE IF EXISTS book;
DROP TABLE IF EXISTS author;

--  Author table
CREATE TABLE author (
                        id SERIAL PRIMARY KEY,
                        first_name VARCHAR(100) NOT NULL,
                        last_name  VARCHAR(100) NOT NULL,
                        email      VARCHAR(150) NOT NULL UNIQUE,
                        bio        TEXT,
                        genere     VARCHAR(100)
);

-- Book table
CREATE TABLE book (
                      id BIGINT PRIMARY KEY,
                      title     VARCHAR(200) NOT NULL,
                      genere    VARCHAR(100),
                      author_id BIGINT NOT NULL,

                      CONSTRAINT fk_book_author
                          FOREIGN KEY (author_id)
                              REFERENCES author(id)
                              ON DELETE CASCADE
);