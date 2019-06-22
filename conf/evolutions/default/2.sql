# --- !Ups
CREATE TABLE posts (
  id varchar(36) NOT NULL,
  user_id varchar(36) NOT NULL,
  text varchar(255) NOT NULL,
  posted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  FOREIGN KEY(user_id)
  REFERENCES test_users(id)
);

# --- !Downs
drop table test_users