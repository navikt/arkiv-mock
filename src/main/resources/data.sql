DROP TABLE IF EXISTS joark;

CREATE TABLE joark (
                       id VARCHAR(255) PRIMARY KEY,
                       title VARCHAR(255),
                       tema VARCHAR(255),
                       timesaved TIMESTAMP
);