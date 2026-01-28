CREATE TABLE tenants (
    id UUID NOT NULL,
    creation_date TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    name VARCHAR(255) NOT NULL UNIQUE,
    status VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE users (
    id UUID NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE user_memberships (
    id UUID NOT NULL,
    roles VARCHAR(255) NOT NULL,
    tenant_id UUID NOT NULL,
    user_id UUID NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT FK_membership_user FOREIGN KEY (user_id) REFERENCES users(id)
);