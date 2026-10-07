ALTER TABLE `ymcx_user`
    ADD COLUMN `authentication` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '实名认证：1未提交，2审核中，3通过，4拒绝',
    ADD COLUMN `authentication_mobile` VARCHAR(20) DEFAULT NULL,
    ADD COLUMN `authentication_name` VARCHAR(50) DEFAULT NULL,
    ADD COLUMN `authentication_card` VARCHAR(255) DEFAULT NULL,
    ADD COLUMN `staff_authentication` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '员工认证：1未提交，2审核中，3通过，4拒绝',
    ADD COLUMN `staff_mobile` VARCHAR(20) DEFAULT NULL,
    ADD COLUMN `staff_name` VARCHAR(50) DEFAULT NULL,
    ADD COLUMN `staff_work` VARCHAR(255) DEFAULT NULL;
