DELETE FROM articles;
DELETE FROM journals;

INSERT INTO journals(id, name, publisher) VALUES ('CACM', 'Communications of the ACM', 'ACM');
INSERT INTO journals(id, name, publisher) VALUES ('COMP', 'Computer', 'IEEE');
INSERT INTO journals(id, name, publisher) VALUES ('TIT', 'IEEE Transactions on Information Theory', 'IEEE');

INSERT INTO articles (published_at, author_full_name, author_last_name, title, doi, journal_id)
VALUES (DATE '1972-12-01', 'David L. Parnas', 'Parnas', 'On the Criteria To Be Used in Decomposing Systems into Modules', '10.1145/361598.361623', 'CACM');
INSERT INTO articles (published_at, author_full_name, author_last_name, title, doi, journal_id)
VALUES (DATE '1976-11-01', 'Whitfield Diffie and Martin E. Hellman', 'Diffie-Hellman', 'New Directions in Cryptography', '10.1109/TIT.1976.1055638', 'TIT');
INSERT INTO articles (published_at, author_full_name, author_last_name, title, doi, journal_id)
VALUES (DATE '1978-07-01', 'Leslie Lamport', 'Lamport', 'Time, Clocks, and the Ordering of Events in a Distributed System', '10.1145/359545.359563', 'CACM');
INSERT INTO articles (published_at, author_full_name, author_last_name, title, doi, journal_id)
VALUES (DATE '1987-04-01', 'Frederick P. Brooks', 'Brooks', 'No Silver Bullet: Essence and Accidents of Software Engineering', '10.1109/MC.1987.1663532', 'COMP');
