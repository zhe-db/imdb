CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION citext;
CREATE EXTENSION pgcrypto;

CREATE TABLE IF NOT EXISTS users (
  user_id uuid DEFAULT uuid_generate_v4 () PRIMARY KEY,
  name varchar(250) NOT NULL,
  email citext UNIQUE NOT NULL,
  password TEXT NOT NULL,
  created_on TIMESTAMP NOT NULL,
  last_login TIMESTAMP 
);

