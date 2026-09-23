ALTER TABLE users ADD COLUMN username VARCHAR(64);

UPDATE users
SET username = split_part(email, '@', 1)
WHERE username IS NULL OR username = '';

UPDATE users u
SET username = u.username || '-' || u.id
WHERE u.id IN (
    SELECT id
    FROM (
        SELECT id,
               ROW_NUMBER() OVER (PARTITION BY LOWER(username) ORDER BY id) AS rn
        FROM users
    ) ranked
    WHERE ranked.rn > 1
);

ALTER TABLE users ALTER COLUMN username SET NOT NULL;

ALTER TABLE users ADD CONSTRAINT uk_users_username UNIQUE (username);
