-- Reset password untuk user yang ada menjadi '123456' dengan SHA-256
UPDATE users 
SET password = 'jZae727K08KaOmKSgOaGzww/XVqGr/PKEgIMkjrcbJI=',  -- SHA-256 hash untuk '123456'
    salt = NULL
WHERE username IN ('john_doe', 'jane_smith', 'bob_wilson');
