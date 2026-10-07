ALTER TABLE fm_driver_balance_log
    ADD UNIQUE KEY uk_driver_order_income (driver_id, order_id, type);
