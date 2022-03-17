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

CREATE TABLE IF NOT EXISTS ratings (
    uuid text NOT NULL PRIMARY KEY,
    movie_id INTEGER PRIMARY KEY REFERENCES moviedetail (movie_id),
    rates FLOAT
);

CREATE TABLE IF NOT EXISTS favourite (
    uuid text NOT NULL PRIMARY KEY,
    movie_id INTEGER REFERENCES moviedetail (movie_id)
);

CREATE TABLE IF NOT EXISTS actorlikes (
    uuid text NOT NULL PRIMARY KEY,
    actor_id INTEGER REFERENCES actors (actor_id)
);

CREATE TABLE IF NOT EXISTS viewhistory (
    uuid text PRIMARY KEY,
    movie_id INTEGER REFERENCES moviedetail (movie_id),
    TIME TIMESTAMP
);

CREATE TABLE IF NOT EXISTS moviedetail (
    movie_id INTEGER PRIMARY KEY,
    adult BOOLEAN NOT NULL,
    backdrop_path TEXT,
    budget INTEGER,
    imdb_id TEXT NOT NULL,
    title TEXT NOT NULL,
    overview TEXT,
    popularity FLOAT,
    poster_path TEXT,
    release_date DATE,
    runtime INTEGER,
    revenue INTEGER,
    vote_average FLOAT
);

CREATE TABLE IF NOT EXISTS genretypes (
    genre_id INTEGER NOT NULL PRIMARY KEY,
    genre_name TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS moviegenre (
    movie_id INTEGER NOT NULL PRIMARY KEY REFERENCES moviedetail (movie_id),
    genre_id INTEGER NOT NULL REFERENCES genretypes (genre_id)
);

CREATE TABLE IF NOT EXISTS movieactor (
    movie_id INTEGER NOT NULL PRIMARY KEY REFERENCES moviedetail (movie_id),
    actor_id INTEGER NOT NULL REFERENCES actors (actor_id)
);

CREATE TABLE IF NOT EXISTS actors (
    actor_id INTEGER NOT NULL PRIMARY KEY,
    birthday DATE,
    know_for_department TEXT,
    actor_name TEXT NOT NULL,
    gender INTEGER NOT NULL,
    biography TEXT,
    place_of_birth TEXT,
    profile_path TEXT
);