CREATE DATABASE rewards_db;

USE rewards_db;

INSERT INTO transactions
(customer_id, customer_name, amount, transaction_date)
VALUES
    (1, 'Prerna', 120, '2026-04-10'),
    (1, 'Prerna', 75, '2026-05-12'),
    (2, 'Suman', 200, '2026-06-01');