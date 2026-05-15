CREATE TABLE candidates (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    creation_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    city_id INT REFERENCES cities (id),
    file_id INT REFERENCES files (id)
);
