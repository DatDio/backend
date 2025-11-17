-- Init script for Oracle Database
-- This script runs automatically when container starts for the first time

-- Create user for MailShop application
CREATE USER mailshop_user IDENTIFIED BY mailshop_pass;

-- Grant necessary privileges
GRANT CONNECT, RESOURCE TO mailshop_user;
GRANT CREATE SESSION TO mailshop_user;
GRANT CREATE TABLE TO mailshop_user;
GRANT CREATE VIEW TO mailshop_user;
GRANT CREATE SEQUENCE TO mailshop_user;
GRANT CREATE TRIGGER TO mailshop_user;
GRANT CREATE PROCEDURE TO mailshop_user;
GRANT UNLIMITED TABLESPACE TO mailshop_user;

-- Grant additional privileges for full operation
GRANT SELECT ANY TABLE TO mailshop_user;
GRANT INSERT ANY TABLE TO mailshop_user;
GRANT UPDATE ANY TABLE TO mailshop_user;
GRANT DELETE ANY TABLE TO mailshop_user;

-- Commit changes
COMMIT;

-- Set default schema
ALTER USER mailshop_user DEFAULT TABLESPACE USERS;
ALTER USER mailshop_user TEMPORARY TABLESPACE TEMP;

-- Display success message
SELECT 'MailShop database user created successfully!' AS status FROM DUAL;
