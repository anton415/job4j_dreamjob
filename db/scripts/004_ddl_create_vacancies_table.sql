CREATE TABLE vacancies (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    creation_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    visible BOOLEAN NOT NULL DEFAULT FALSE,
    city_id INT REFERENCES cities (id),
    file_id INT REFERENCES files (id)
);
