CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    creation_date DATETIME(6) NOT NULL,
    last_modified_date DATETIME(6),
    uuid CHAR(36) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    CONSTRAINT uq_users_uuid UNIQUE (uuid),
    CONSTRAINT uq_users_email UNIQUE (email)
) ENGINE=InnoDB;

CREATE TABLE species (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    creation_date DATETIME(6) NOT NULL,
    last_modified_date DATETIME(6),
    uuid CHAR(36) NOT NULL,
    name VARCHAR(100) NOT NULL,
    is_premium TINYINT(1) NOT NULL,
    price INT NOT NULL,
    is_enabled TINYINT(1) NOT NULL,
    CONSTRAINT uq_species_uuid UNIQUE (uuid)
) ENGINE=InnoDB;

CREATE TABLE wallets (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    creation_date DATETIME(6) NOT NULL,
    last_modified_date DATETIME(6),
    user_id BIGINT NOT NULL,
    soft_currency INT NOT NULL DEFAULT 0,
    hard_currency INT NOT NULL DEFAULT 0,
    CONSTRAINT uq_wallets_user_id UNIQUE (user_id),
    CONSTRAINT fk_wallets_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB;

CREATE TABLE wallet_ledger (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    creation_date DATETIME(6) NOT NULL,
    last_modified_date DATETIME(6),
    user_id BIGINT NOT NULL,
    delta_soft_currency INT NOT NULL,
    delta_hard_currency INT NOT NULL,
    reason VARCHAR(50) NOT NULL,
    ref_type VARCHAR(50) NOT NULL,
    ref_id VARCHAR(100) NOT NULL,
    CONSTRAINT fk_wallet_ledger_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB;

CREATE INDEX idx_wallet_ledger_user ON wallet_ledger (user_id);

CREATE TABLE focus_sessions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    creation_date DATETIME(6) NOT NULL,
    last_modified_date DATETIME(6),
    uuid CHAR(36) NOT NULL,
    user_id BIGINT NOT NULL,
    species_id BIGINT,
    client_start_time DATETIME(6) NOT NULL,
    client_end_time DATETIME(6),
    server_start_time DATETIME(6) NOT NULL,
    server_end_time DATETIME(6),
    state VARCHAR(20) NOT NULL,
    tag VARCHAR(20),
    duration_minutes INT,
    drift_minutes INT,
    flags_json TEXT,
    CONSTRAINT uq_focus_sessions_uuid UNIQUE (uuid),
    CONSTRAINT fk_focus_sessions_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_focus_sessions_species FOREIGN KEY (species_id) REFERENCES species (id)
) ENGINE=InnoDB;

CREATE INDEX idx_focus_sessions_user ON focus_sessions (user_id, creation_date DESC);
CREATE INDEX idx_focus_sessions_species ON focus_sessions (species_id);
