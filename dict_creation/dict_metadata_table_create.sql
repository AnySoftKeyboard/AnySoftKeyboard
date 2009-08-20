BEGIN TRANSACTION;
DROP TABLE IF EXISTS "en_metadata";
CREATE TABLE "en_metadata" ("key" TEXT PRIMARY KEY  NOT NULL , "value" TEXT NOT NULL );
INSERT INTO "en_metadata" VALUES('revision','1');
INSERT INTO "en_metadata" VALUES('source','http://en.wiktionary.org/wiki/Wiktionary:Frequency_lists');
INSERT INTO "en_metadata" VALUES('creator','Menny Even Danan');
INSERT INTO "en_metadata" VALUES('creation_time','13-08-2009 09:19');
COMMIT;
