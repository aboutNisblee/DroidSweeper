-- DB Settings
PRAGMA foreign_keys=1;

-- Create tables
CREATE TABLE player ("_id" INTEGER PRIMARY KEY, "name" TEXT NOT NULL);
CREATE TABLE level ("_id" INTEGER PRIMARY KEY, "level" INTEGER UNIQUE NOT NULL, "x" INTEGER NOT NULL, "y" INTEGER NOT NULL, "bombs" INTEGER NOT NULL);
CREATE TABLE game ("_id" INTEGER PRIMARY KEY, "gameplayer" REFERENCES player("_id"), "gamelevel" REFERENCES level("level"), "time" INTEGER NOT NULL, "date" DATETIME NOT NULL, replay BLOB);

-- Create some views
CREATE VIEW IF NOT EXISTS level4game AS SELECT game._id, level, time, date, gameplayer FROM game JOIN level ON gamelevel=level;
CREATE VIEW IF NOT EXISTS player4game AS SELECT game._id, name FROM game JOIN player ON gameplayer=player._id;
CREATE VIEW IF NOT EXISTS games AS SELECT l4g._id AS _id, level, name, time, date FROM level4game l4g JOIN player4game p4g ON l4g._id=p4g._id;

-- Insert standard difficulty levels
INSERT INTO level (level, x, y, bombs) VALUES (0, 6, 8, 6);
INSERT INTO level (level, x, y, bombs) VALUES (1, 8, 12, 15);
INSERT INTO level (level, x, y, bombs) VALUES (2, 10, 18, 37);

-- Drop tables
DROP TABLE IF EXISTS game;
DROP TABLE IF EXISTS player;
DROP TABLE IF EXISTS level;

-- Drop views
DROP VIEW IF EXISTS level4game;
DROP VIEW IF EXISTS player4game;
DROP VIEW IF EXISTS games;

-- Some example entries
BEGIN TRANSACTION;
INSERT INTO player (name) VALUES ("moritz");

INSERT INTO game (gameplayer, gamelevel, time, date) VALUES (1, 0, 7000, 1416597106560);
INSERT INTO game (gameplayer, gamelevel, time, date) VALUES (1, 0, 8000, 1416597106561);
INSERT INTO game (gameplayer, gamelevel, time, date) VALUES (1, 0, 9000, 1416597106562);
INSERT INTO game (gameplayer, gamelevel, time, date) VALUES (1, 0, 10000, 1416597106563);

INSERT INTO game (gameplayer, gamelevel, time, date) VALUES (1, 1, 20000, 1416597106660);

INSERT INTO game (gameplayer, gamelevel, time, date) VALUES (1, 2, 29000, 1416597106760);
INSERT INTO game (gameplayer, gamelevel, time, date) VALUES (1, 2, 32000, 1416597106860);
END TRANSACTION;

BEGIN TRANSACTION;
INSERT INTO player (name) VALUES ("mone");

INSERT INTO game (gameplayer, gamelevel, time, date) VALUES (2, 0, 6500, 1416597106960);

INSERT INTO game (gameplayer, gamelevel, time, date) VALUES (2, 1, 21000, 1416597101560);

INSERT INTO game (gameplayer, gamelevel, time, date) VALUES (2, 2, 30000, 1416597103560);
END TRANSACTION;

-- Select games for a given level
SELECT * FROM level4game WHERE level=0;
SELECT * FROM level4game WHERE level=1;
SELECT * FROM level4game WHERE level=2;

-- Select games of a given player
SELECT * FROM player4game WHERE name="moritz";
SELECT * FROM player4game WHERE name="mone";

-- Select games
SELECT l4g._id AS _id, level, name, time, date FROM level4game l4g JOIN player4game AS p4g ON l4g._id=p4g._id;
SELECT l4g._id AS _id, level, name, time, date FROM level4game AS l4g JOIN player4game AS p4g ON l4g._id=p4g._id ORDER BY time, date;
SELECT l4g._id AS _id, level, name, time, date FROM level4game AS l4g JOIN player4game AS p4g ON l4g._id=p4g._id WHERE name="moritz";
SELECT l4g._id AS _id, level, name, time, date FROM level4game AS l4g JOIN player4game AS p4g ON l4g._id=p4g._id WHERE name="moritz" ORDER BY time, date;
SELECT l4g._id AS _id, level, name, time, date FROM level4game AS l4g JOIN player4game AS p4g ON l4g._id=p4g._id WHERE level=2;
SELECT l4g._id AS _id, level, name, time, date FROM level4game AS l4g JOIN player4game AS p4g ON l4g._id=p4g._id WHERE level=2 ORDER BY time, date;

-- Or simple
SELECT * FROM games;

-- Delete a player (should fail due to foreign key constraint)
DELETE FROM player WHERE name="mone";