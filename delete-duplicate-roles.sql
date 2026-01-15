-- =========================================================
-- Clean up duplicate INSTRUCTOR roles
-- Run this SQL script to remove duplicate role entries
-- =========================================================

-- Step 1: Check current duplicate roles (for verification)
SELECT id, name, description, created_date 
FROM role 
WHERE name = 'INSTRUCTOR' 
ORDER BY id;

-- Step 2: Find the role ID we want to KEEP (lowest ID)
SELECT @keep_role_id := MIN(id) 
FROM role 
WHERE name = 'INSTRUCTOR';

-- Step 3: Find duplicate role IDs to DELETE
SELECT @delete_role_ids := GROUP_CONCAT(id) 
FROM role 
WHERE name = 'INSTRUCTOR' 
AND id != @keep_role_id;

-- Step 4: Update user_roles to point to the role we're keeping
UPDATE user_roles 
SET role_id = @keep_role_id 
WHERE role_id IN (
    SELECT id 
    FROM role 
    WHERE name = 'INSTRUCTOR' 
    AND id != @keep_role_id
);

-- Step 5: Update role_permissions to point to the role we're keeping
UPDATE role_permissions 
SET role_id = @keep_role_id 
WHERE role_id IN (
    SELECT id 
    FROM role 
    WHERE name = 'INSTRUCTOR' 
    AND id != @keep_role_id
);

-- Step 6: Delete duplicate role entries
DELETE FROM role 
WHERE name = 'INSTRUCTOR' 
AND id != @keep_role_id;

-- Step 7: Verify cleanup (should show only 1 row)
SELECT id, name, description, created_date 
FROM role 
WHERE name = 'INSTRUCTOR';

-- Step 8: Verify all roles are unique now
SELECT name, COUNT(*) as count 
FROM role 
GROUP BY name 
HAVING COUNT(*) > 1;
-- (Should return empty result)
