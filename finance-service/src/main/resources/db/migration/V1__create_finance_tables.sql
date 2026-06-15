CREATE TABLE families (
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE family_members (
    id         BIGSERIAL PRIMARY KEY,
    family_id  BIGINT       NOT NULL REFERENCES families(id) ON DELETE CASCADE,
    user_id    BIGINT       NOT NULL,
    user_email VARCHAR(255) NOT NULL,
    role       VARCHAR(20)  NOT NULL,
    joined_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    UNIQUE (family_id, user_id)
);

CREATE TABLE categories (
    id      BIGSERIAL PRIMARY KEY,
    name    VARCHAR(100) NOT NULL,
    type    VARCHAR(20)  NOT NULL,
    user_id BIGINT
);

CREATE TABLE transactions (
    id          BIGSERIAL PRIMARY KEY,
    amount      DECIMAL(15,2) NOT NULL,
    type        VARCHAR(20)   NOT NULL,
    description VARCHAR(500),
    date        DATE          NOT NULL,
    user_id     BIGINT        NOT NULL,
    category_id BIGINT        NOT NULL REFERENCES categories(id),
    family_id   BIGINT        REFERENCES families(id),
    created_at  TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_transactions_user_id ON transactions (user_id);
CREATE INDEX idx_transactions_family_id ON transactions (family_id);
CREATE INDEX idx_transactions_date ON transactions (date);
CREATE INDEX idx_family_members_user_id ON family_members (user_id);
