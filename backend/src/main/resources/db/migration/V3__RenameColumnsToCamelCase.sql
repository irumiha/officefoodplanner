ALTER TABLE auth.users RENAME COLUMN first_name TO "firstName";
ALTER TABLE auth.users RENAME COLUMN last_name TO "lastName";
ALTER TABLE auth.users RENAME COLUMN password_hash TO "passwordHash";
ALTER TABLE auth.users RENAME COLUMN created_on TO "createdOn";
ALTER TABLE auth.users RENAME COLUMN updated_on TO "updatedOn";

ALTER TABLE auth.sessions RENAME COLUMN user_id TO "userID";
ALTER TABLE auth.sessions RENAME COLUMN created_on TO "createdOn";
ALTER TABLE auth.sessions RENAME COLUMN expires_on TO "expiresOn";
ALTER TABLE auth.sessions RENAME COLUMN updated_on TO "updatedOn";

ALTER TABLE auth.group_permissions RENAME COLUMN group_id TO "groupId";
ALTER TABLE auth.group_permissions RENAME COLUMN permission_id TO "permissionId";

ALTER TABLE auth.user_groups RENAME COLUMN group_id TO "groupId";
ALTER TABLE auth.user_groups RENAME COLUMN user_id TO "userId";

ALTER TABLE auth.user_permissions RENAME COLUMN permission_id TO "permissionId";
ALTER TABLE auth.user_permissions RENAME COLUMN user_id TO "userId";
