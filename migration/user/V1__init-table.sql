CREATE TABLE IF NOT EXISTS item(
    id VARCHAR(255),
    dead BOOLEAN,
    deleted BOOLEAN,
    descendants INTEGER,
    kids BIGINT[],
    parent BIGINT,
    parts BIGINT[],
    poll BIGINT,
    score INTEGER,
    text TEXT,
    time BIGINT,
    title VARCHAR(255),
    type VARCHAR(255),
    url VARCHAR(255)
);
