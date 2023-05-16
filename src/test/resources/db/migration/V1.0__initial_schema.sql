CREATE TABLE articles
    (
        id ${autoIncrement},
        published_at DATE NOT NULL,
        author_full_name VARCHAR(255) NOT NULL,
        author_last_name VARCHAR(100) NOT NULL,
        title VARCHAR(100) NOT NULL
    );

