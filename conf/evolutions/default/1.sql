# --- !Ups
CREATE TABLE test_users (
  id varchar(36) NOT NULL,
  name varchar(255) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE (name)
);

SET @user_1 = '11111111-1111-1111-1111-111111111111';
SET @user_2 = '22222222-2222-2222-2222-222222222222';
SET @user_3 = '33333333-3333-3333-3333-333333333333';

INSERT INTO test_users(id, name) VALUES
  (@user_1, 'alice')
  ,(@user_2, 'bob')
  ,(@user_3, 'charlie');


# --- !Downs
drop table test_users