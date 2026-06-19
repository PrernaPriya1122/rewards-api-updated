INSERT INTO CUSTOMER (customer_id, customer_name)
VALUES
    (1, 'John Doe'),
    (2, 'Jane Smith'),
    (3, 'Robert Brown'),
    (4, 'Emily Davis'),
    (5, 'Michael Wilson'),
    (6, 'Sarah Johnson'),
    (7, 'David Miller'),
    (8, 'Sophia Taylor'),
    (9, 'James Anderson'),
    (10, 'Olivia Thomas');

INSERT INTO TRANSACTION (customer_id, amount, transaction_date)
VALUES
    -- Customer 1
    (1, 120.00, DATE '2026-04-10'),
    (1, 75.00,  DATE '2026-04-15'),
    (1, 200.00, DATE '2026-05-05'),

    -- Customer 2
    (2, 95.00,  DATE '2026-04-11'),
    (2, 150.00, DATE '2026-05-12'),
    (2, 220.00, DATE '2026-06-18'),

    -- Customer 3
    (3, 60.00,  DATE '2026-04-20'),
    (3, 180.00, DATE '2026-06-01'),

    -- Customer 4
    (4, 130.00, DATE '2026-04-08'),
    (4, 85.00,  DATE '2026-05-21'),
    (4, 250.00, DATE '2026-06-10'),

    -- Customer 5
    (5, 55.00,  DATE '2026-04-18'),
    (5, 105.00, DATE '2026-05-06'),
    (5, 175.00, DATE '2026-06-25'),

    -- Customer 6
    (6, 90.00,  DATE '2026-04-03'),
    (6, 120.00, DATE '2026-05-14'),
    (6, 145.00, DATE '2026-06-19'),

    -- Customer 7
    (7, 80.00,  DATE '2026-04-07'),
    (7, 210.00, DATE '2026-05-16'),
    (7, 70.00,  DATE '2026-06-02'),

    -- Customer 8
    (8, 160.00, DATE '2026-04-22'),
    (8, 110.00, DATE '2026-05-08'),
    (8, 95.00,  DATE '2026-06-13'),

    -- Customer 9
    (9, 65.00,  DATE '2026-04-05'),
    (9, 140.00, DATE '2026-05-20'),
    (9, 190.00, DATE '2026-06-28'),

    -- Customer 10
    (10, 100.00, DATE '2026-04-12'),
    (10, 125.00, DATE '2026-05-09'),
    (10, 300.00, DATE '2026-06-15');