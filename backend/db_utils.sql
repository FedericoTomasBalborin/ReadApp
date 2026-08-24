
-- Reset tables
TRUNCATE TABLE users, reservations, ratings, user_roles, refresh_tokens RESTART IDENTITY CASCADE;

-- Reset schema
DROP SCHEMA public CASCADE; CREATE SCHEMA public;

-- Componente 1
CREATE OR REPLACE VIEW view_books_reserved_current_year AS
SELECT
    r.user_id,
    b.id AS book_id,
    b.title,
    b.author
FROM reservations r
         JOIN books b ON b.id = r.book_id
WHERE EXTRACT(YEAR FROM r.start_date) = EXTRACT(YEAR FROM CURRENT_DATE);

SELECT *
FROM view_books_reserved_current_year
WHERE user_id = 2;

-- Componente 2
CREATE TABLE book_score_update_log (
    id SERIAL PRIMARY KEY,
    book_id INTEGER REFERENCES books(id),
    previous_score DOUBLE PRECISION,
    updated_score DOUBLE PRECISION,
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE OR REPLACE FUNCTION log_book_score_update()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.calification IS DISTINCT FROM OLD.calification THEN
        INSERT INTO book_score_update_log (
            book_id,
            previous_score,
            updated_score,
            updated_at
        )
        VALUES (
            OLD.id,
            OLD.calification,
            NEW.calification,
            NOW()
        );
END IF;

RETURN NEW;
END;
$$ LANGUAGE plpgsql;


CREATE TRIGGER book_score_log_trigger
    AFTER UPDATE ON books
    FOR EACH ROW
    EXECUTE FUNCTION log_book_score_update();

-- Componente 3
CREATE OR REPLACE VIEW view_users_with_reservation_count AS
SELECT
    u.id,
    u.name,
    u.lastname,
    u.email,
    COUNT(r.id) AS reservation_count
FROM users u
         LEFT JOIN reservations r ON u.id = r.user_id
GROUP BY u.id, u.name, u.lastname, u.email;

-- Componente 4
ALTER TABLE users
ALTER COLUMN bibliokarma SET DEFAULT 0,
ADD CONSTRAINT chk_bibliokarma_positive CHECK (bibliokarma >= 0);

--Componente 5
CREATE OR REPLACE VIEW usuarios_con_mas_de_2_reservas_devueltas AS
SELECT
    u.id,
    u.name,
    u.lastname,
    u.email,
    u.residence_city,
    u.bibliokarma,
    COUNT(r.id) AS cantidad_reservas_devueltas
FROM users u JOIN reservations r ON r.user_id = u.id
WHERE r.is_active = true
GROUP BY u.id, u.name, u.lastname, u.email, u.residence_city, u.bibliokarma
HAVING COUNT(r.id) > 2

select * from reservations