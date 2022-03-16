CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION citext;
CREATE DOMAIN domain_email AS citext
CHECK(
   VALUE ~ '^\w+@[a-zA-Z_]+?\.[a-zA-Z]{2,3}$'
);
CREATE EXTENSION pgcrypto;

CREATE TABLE IF NOT EXISTS accounts (
  user_id uuid DEFAULT uuid_generate_v4 () PRIMARY KEY,
  name varchar(250) NOT NULL,
  email domain_email,
  password TEXT NOT NULL,
  created_on TIMESTAMP NOT NULL,
  last_login TIMESTAMP 
);

