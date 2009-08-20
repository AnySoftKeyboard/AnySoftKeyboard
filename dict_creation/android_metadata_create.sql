BEGIN TRANSACTION;
DROP TABLE IF EXISTS "android_metadata";
CREATE TABLE "android_metadata" (
    "locale" TEXT
);
INSERT INTO "android_metadata" VALUES('en_US');
COMMIT;
