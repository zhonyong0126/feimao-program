CREATE TABLE IF NOT EXISTS `feimao_legacy_session` (
    `token` VARCHAR(64) NOT NULL,
    `subject_id` BIGINT UNSIGNED NOT NULL,
    `subject_type` VARCHAR(20) NOT NULL,
    `create_time` BIGINT NOT NULL,
    `expire_time` BIGINT NOT NULL,
    PRIMARY KEY (`token`),
    KEY `idx_subject` (`subject_type`,`subject_id`),
    KEY `idx_expire` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='旧门店/酒店/加盟商会话兼容表';
