CREATE DATABASE spreading;

-- TODO: make this role creation secure
CREATE ROLE spreading WITH LOGIN PASSWORD 'spreading';

-- Grant db ownership for user 'spreading' for corresponding database
ALTER DATABASE spreading OWNER TO spreading;
