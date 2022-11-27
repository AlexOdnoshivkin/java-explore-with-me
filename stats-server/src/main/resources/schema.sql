CREATE TABLE IF NOT EXISTS stats (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    Identification_record BIGINT,
    app VARCHAR(255),
    uri VARCHAR(512),
    ip VARCHAR(512),
    timestamp TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_stat PRIMARY KEY (id)
);