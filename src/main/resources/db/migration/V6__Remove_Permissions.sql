-- V6: Remove Permissions System - Keep Only Roles (USER and ADMIN)
-- Date: 2025-11-17
-- Description: Drop PERMISSIONS table and ROLE_PERMISSIONS junction table, simplify RBAC to role-only

-- Drop junction table first (foreign key constraints)
DROP TABLE ROLE_PERMISSIONS CASCADE CONSTRAINTS;

-- Drop PERMISSIONS table
DROP TABLE PERMISSIONS CASCADE CONSTRAINTS;

-- Drop permission sequence
DROP SEQUENCE PERMISSION_SEQ;

-- Clean up any permission-related indexes (if exist)
-- Note: Indexes on dropped tables are automatically dropped

-- Add comments for simplified RBAC
COMMENT ON TABLE ROLES IS 'Simplified role table: USER (basic access) and ADMIN (full access)';
COMMENT ON COLUMN ROLES.NAME IS 'Role name: USER or ADMIN';
