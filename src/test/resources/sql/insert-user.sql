INSERT INTO users (id, username, password, created_at, updated_at)
VALUES (
    'a1b2c3d4-e5f6-7890-abcd-ef1234567890',
    'john',
    '$2a$12$LW1cTQRqnhQGLTxo1BWzeezJWM6NKYc7CKmkSUd5dl4umNGEUk0Ve',
    NOW(),
    NOW()
)
ON CONFLICT (id) DO NOTHING;

INSERT INTO users (id, username, password, created_at, updated_at)
VALUES (
    '41a385a3-de9f-44bb-ac0f-7a9fd6ac11e1',
    'peter',
    '$2a$12$LW1cTQRqnhQGLTxo1BWzeezJWM6NKYc7CKmkSUd5dl4umNGEUk0Ve',
    NOW(),
    NOW()
)
ON CONFLICT (id) DO NOTHING;
