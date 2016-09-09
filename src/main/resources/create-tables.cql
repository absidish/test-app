use hub;

drop table test_users;
drop table test_environments;
drop table test_user_by_environment;
drop table test_environments_by_user;

CREATE TABLE hub.test_users (
    id uuid PRIMARY KEY,
    access_token uuid,
    email text,
    login text,
    name text
);

CREATE TABLE hub.test_environments (
    id uuid PRIMARY KEY,
    description text,
    name text,
    access_token uuid
);

CREATE TABLE hub.test_environments_by_user (
    user_id uuid,
    environment_id uuid,
    access_token uuid,
    PRIMARY KEY (user_id, environment_id)
);

CREATE TABLE hub.test_user_by_environment (
    environment_id uuid,
    user_id uuid,
    access_token uuid,
    PRIMARY KEY (environment_id, user_id)
);