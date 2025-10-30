CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    creation_date DATETIME(6) NOT NULL,
    last_modified_date DATETIME(6),
    uuid CHAR(36) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    CONSTRAINT uq_user_uuid UNIQUE (uuid),
    CONSTRAINT uq_user_email UNIQUE (email)
) ENGINE=InnoDB;

CREATE TABLE species (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    creation_date DATETIME(6) NOT NULL,
    last_modified_date DATETIME(6),
    uuid CHAR(36) NOT NULL,
    code VARCHAR(64) NOT NULL,
    name VARCHAR(100) NOT NULL,
    is_premium TINYINT(1) NOT NULL,
    price INT NOT NULL,
    is_enabled TINYINT(1) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    default_available TINYINT(1) NOT NULL DEFAULT 0,
    CONSTRAINT uq_species_uuid UNIQUE (uuid),
    CONSTRAINT uq_species_code UNIQUE (code)
) ENGINE=InnoDB;

CREATE TABLE wallet (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    creation_date DATETIME(6) NOT NULL,
    last_modified_date DATETIME(6),
    user_id BIGINT NOT NULL,
    soft_currency INT NOT NULL DEFAULT 0,
    hard_currency INT NOT NULL DEFAULT 0,
    CONSTRAINT uq_wallet_user_id UNIQUE (user_id),
    CONSTRAINT fk_wallet_user FOREIGN KEY (user_id) REFERENCES users (id)
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

CREATE TABLE focus_session (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    creation_date DATETIME(6) NOT NULL,
    last_modified_date DATETIME(6),
    uuid CHAR(36) NOT NULL,
    user_id BIGINT NOT NULL,
    species_id BIGINT NOT NULL,
    client_start_time DATETIME(6) NOT NULL,
    client_end_time DATETIME(6),
    server_start_time DATETIME(6) NOT NULL,
    server_end_time DATETIME(6),
    state VARCHAR(20) NOT NULL,
    tag VARCHAR(20),
    planned_minutes INT,
    duration_minutes INT,
    flags_json TEXT,
    CONSTRAINT uq_focus_session_uuid UNIQUE (uuid),
    CONSTRAINT fk_focus_session_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_focus_session_species FOREIGN KEY (species_id) REFERENCES species (id)
) ENGINE=InnoDB;

CREATE INDEX idx_focus_session_user ON focus_session (user_id, creation_date DESC);
CREATE INDEX idx_focus_session_species ON focus_session (species_id);

CREATE TABLE user_species_unlock (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    creation_date DATETIME(6) NOT NULL,
    last_modified_date DATETIME(6),
    user_id BIGINT NOT NULL,
    species_id BIGINT NOT NULL,
    unlocked_at DATETIME(6) NOT NULL,
    method VARCHAR(20) NOT NULL,
    price_paid INT NOT NULL,
    currency_type VARCHAR(10) NOT NULL,
    notes VARCHAR(255),
    CONSTRAINT fk_user_species_unlock_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_user_species_unlock_species FOREIGN KEY (species_id) REFERENCES species (id),
    CONSTRAINT uq_user_species_unlock UNIQUE (user_id, species_id)
) ENGINE=InnoDB;

CREATE INDEX idx_user_species_unlock_user ON user_species_unlock (user_id);
CREATE INDEX idx_user_species_unlock_species ON user_species_unlock (species_id);
