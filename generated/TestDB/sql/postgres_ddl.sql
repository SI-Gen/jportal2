-- Generated PostgreSQL DDL for TestDB
-- Database: TestDB
-- Server: "localhost"


-- =============================================================================
-- Table: Users
-- =============================================================================
DROP TABLE IF EXISTS Users CASCADE;

CREATE TABLE Users
(
id integer,
name varchar(50),
email varchar(100),
created_date date
);

-- Grants
-- Indexes (non-primary, non-unique)
-- Views
-- Field constraints and defaults
-- Primary and Unique Keys
-- Data procedures (INSERT statements)
-- =============================================================================
-- Foreign Key Constraints (added last so all tables exist)
-- =============================================================================
