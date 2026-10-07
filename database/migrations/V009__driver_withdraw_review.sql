ALTER TABLE fm_withdraw
    ADD COLUMN transfer_batch_id VARCHAR(100) DEFAULT NULL AFTER reject_reason,
    ADD COLUMN review_note VARCHAR(255) DEFAULT NULL AFTER transfer_batch_id,
    ADD COLUMN review_time DATETIME DEFAULT NULL AFTER review_note;
