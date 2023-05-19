CREATE TABLE journals
    (
        id VARCHAR(10) NOT NULL PRIMARY KEY,
        name VARCHAR(255) NOT NULL,
        publisher VARCHAR(20) NOT NULL
    );

CREATE TABLE articles
    (
        id ${autoIncrement} PRIMARY KEY,
        published_at DATE NOT NULL,
        author_full_name VARCHAR(255) NOT NULL,
        author_last_name VARCHAR(100) NOT NULL,
        title VARCHAR(100) NOT NULL,
        doi VARCHAR(50) NOT NULL,
        journal_id VARCHAR(10) NOT NULL,
        FOREIGN KEY (journal_id) REFERENCES journals(id)
    );
