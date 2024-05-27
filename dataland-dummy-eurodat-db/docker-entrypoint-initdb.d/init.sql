-- init.sql
CREATE SCHEMA IF NOT EXISTS safedeposit;

CREATE TABLE IF NOT EXISTS safedeposit.json (
    uuid_json VARCHAR(36) PRIMARY KEY,
    blob_json JSONB
    );

CREATE TABLE IF NOT EXISTS safedeposit.pdf (
    uuid_pdf VARCHAR(36) PRIMARY KEY,
    blob_pdf BYTEA
    );
