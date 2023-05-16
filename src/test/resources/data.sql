TRUNCATE TABLE articles;

INSERT INTO articles (published_at, author_full_name, author_last_name, title)
VALUES (DATE '1972-01-01', 'David L. Parnas', 'Parnas', 'On the Criteria To Be Used in Decomposing Systems into Modules');
INSERT INTO articles (published_at, author_full_name, author_last_name, title)
VALUES (DATE '1976-01-01', 'Whitfield Diffie and Martin E. Hellman', 'Diffie-Hellman', 'New Directions in Cryptography');
INSERT INTO articles (published_at, author_full_name, author_last_name, title)
VALUES (DATE '1978-01-01', 'Leslie Lamport', 'Lamport', 'Time, Clocks, and the Ordering of Events in a Distributed System');
INSERT INTO articles (published_at, author_full_name, author_last_name, title)
VALUES (DATE '1986-01-01', 'Frederick P. Brooks', 'Brooks', 'No Silver Bullet: Essence and Accidents of Software Engineering');
