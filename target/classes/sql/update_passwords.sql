-- Reset password untuk semua user menjadi '123456' dengan SHA-256 hash
UPDATE users 
SET password = 'jZae727K08KaOmKSgOaGzww/XVqGr/PKEgIMkjrcbJI=',  -- SHA-256 hash untuk '123456'
    salt = 'dummySalt123'  -- Salt tetap untuk testing
WHERE username IN ('admin', 'john_doe', 'jane_smith', 'mikewilson', 'sarahbrown')
  OR salt IS NULL;  -- Update juga user lain yang belum memiliki salt
