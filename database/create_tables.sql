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

CREATE TABLE IF NOT EXISTS moviedetail (
    id INTEGER PRIMARY KEY,
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

CREATE TABLE IF NOT EXISTS castinfo (
    id INTEGER NOT NULL PRIMARY KEY,
    birthday DATE,
    know_for_department TEXT,
    name TEXT NOT NULL,
    gender INTEGER NOT NULL,
    biography TEXT,
    place_of_birth TEXT,
    profile_path TEXT
);

CREATE TABLE IF NOT EXISTS userratings (
    uuid text NOT NULL,
    movie_id INTEGER REFERENCES moviedetail (id),
    rates FLOAT,
    PRIMARY KEY(uuid, movie_id)
);

CREATE TABLE IF NOT EXISTS userfavourite (
    uuid text NOT NULL,
    movie_id INTEGER REFERENCES moviedetail (id),
    PRIMARY KEY(uuid, movie_id)
);

CREATE TABLE IF NOT EXISTS actorlikes (
    uuid text NOT NULL,
    actor_id INTEGER REFERENCES castinfo (id),
    PRIMARY KEY(uuid, actor_id)
);

CREATE TABLE IF NOT EXISTS viewhistory (
    uuid text NOT NULL,
    movie_id INTEGER REFERENCES moviedetail (id),
    TIME TIMESTAMP,
    PRIMARY KEY(uuid, movie_id)
);


CREATE TABLE IF NOT EXISTS genretypes (
    id INTEGER NOT NULL PRIMARY KEY,
    name TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS moviegenre (
    movie_id INTEGER NOT NULL REFERENCES moviedetail (id),
    genre_id INTEGER NOT NULL REFERENCES genretypes (id),
    PRIMARY KEY(movie_id, genre_id)
);

CREATE TABLE IF NOT EXISTS moviecast (
    movie_id INTEGER NOT NULL REFERENCES moviedetail (id),
    cast_id INTEGER NOT NULL REFERENCES castinfo (id),
    PRIMARY KEY(movie_id, cast_id)
);
