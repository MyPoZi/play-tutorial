# --- !Ups
CREATE TABLE comments (
  id varchar(36) NOT NULL,
  user_id varchar(36) NOT NULL,
  text varchar(255) NOT NULL,
  parent_post_id varchar(36) NOT NULL,
  posted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  FOREIGN KEY(user_id)
  REFERENCES test_users(id),
  FOREIGN KEY(parent_post_id)
  REFERENCES posts(id),

);

# --- !Downs
drop table test_users