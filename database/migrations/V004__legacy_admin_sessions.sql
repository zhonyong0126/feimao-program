CREATE TABLE IF NOT EXISTS feimao_admin_session (
  token varchar(64) NOT NULL,
  admin_id bigint unsigned NOT NULL,
  create_time bigint NOT NULL,
  expire_time bigint NOT NULL,
  PRIMARY KEY (token),
  KEY idx_admin_expire (admin_id, expire_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='迁移后台管理员会话';
