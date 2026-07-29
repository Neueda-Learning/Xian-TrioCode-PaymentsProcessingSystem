-- ============================================================
-- Demo/display data for the Payment list & timeline UI.
-- Depends on: account, payment, payment_status_history
-- (currency_dict is already seeded via SQL/currency_dict.sql)
-- Safe to re-run: existing rows with the same id/payment_no are removed first.
-- ============================================================

-- 1. Accounts -------------------------------------------------
DELETE FROM account WHERE id BETWEEN 1 AND 6;
INSERT INTO account (id, account_no, name, balance, status, created_at) VALUES
(1, 'ACC0001', 'Alice Johnson', 50000.00, 1, '2026-01-05 09:00:00.000'),
(2, 'ACC0002', 'Bob Smith',     32000.00, 1, '2026-01-05 09:05:00.000'),
(3, 'ACC0003', 'Charlie Lee',   15000.50, 1, '2026-01-05 09:10:00.000'),
(4, 'ACC0004', 'Diana Prince',   8000.00, 1, '2026-01-05 09:15:00.000'),
(5, 'ACC0005', 'Ethan Wright', 120000.00, 1, '2026-01-05 09:20:00.000'),
(6, 'ACC0006', 'Fiona Chen',    4500.75, 1, '2026-01-05 09:25:00.000');

-- 2. Payments (covers all 5 statuses: CREATED / VALIDATED / SENT / COMPLETED / FAILED)
DELETE FROM payment WHERE payment_no IN (
  'PAY20260728001','PAY20260728002','PAY20260728003','PAY20260728004',
  'PAY20260728005','PAY20260728006','PAY20260728007','PAY20260728008',
  'PAY20260728009','PAY20260728010'
);
INSERT INTO payment
(id, payment_no, source_account_id, destination_account_id, amount, currency, reference,
 status, failure_code, failure_message, validated_at, sent_at, completed_at, failed_at,
 version, created_at, updated_at)
VALUES
(101, 'PAY20260728001', 1, 2,  1250.00, 'USD', 'Invoice #INV-1001',
 'COMPLETED', NULL, NULL,
 '2026-07-28 06:58:02.100', '2026-07-28 06:58:03.400', '2026-07-28 06:58:04.900', NULL,
 3, '2026-07-28 06:57:57.598', '2026-07-28 06:58:04.900'),

(102, 'PAY20260728002', 2, 3,   860.50, 'EUR', 'Supplier settlement',
 'COMPLETED', NULL, NULL,
 '2026-07-28 07:12:10.200', '2026-07-28 07:12:11.500', '2026-07-28 07:12:12.800', NULL,
 3, '2026-07-28 07:12:05.000', '2026-07-28 07:12:12.800'),

(103, 'PAY20260728003', 3, 4,  4200.00, 'GBP', 'Payroll batch 07',
 'SENT', NULL, NULL,
 '2026-07-28 09:03:20.000', '2026-07-28 09:03:21.300', NULL, NULL,
 2, '2026-07-28 09:03:15.000', '2026-07-28 09:03:21.300'),

(104, 'PAY20260728004', 4, 5,   300.00, 'JPY', 'Refund for order #778',
 'VALIDATED', NULL, NULL,
 '2026-07-28 10:45:08.400', NULL, NULL, NULL,
 1, '2026-07-28 10:45:05.000', '2026-07-28 10:45:08.400'),

(105, 'PAY20260728005', 5, 6,  9999.99, 'USD', 'Contract deposit',
 'CREATED', NULL, NULL,
 NULL, NULL, NULL, NULL,
 0, '2026-07-28 12:20:00.000', '2026-07-28 12:20:00.000'),

(106, 'PAY20260728006', 6, 1,   150.75, 'CNY', 'Reimbursement',
 'FAILED', 'INSUFFICIENT_BALANCE', 'Source account balance is insufficient',
 '2026-07-28 13:05:30.000', '2026-07-28 13:05:31.200', NULL, '2026-07-28 13:05:32.700',
 3, '2026-07-28 13:05:25.000', '2026-07-28 13:05:32.700'),

(107, 'PAY20260728007', 1, 3,  2500.00, 'USD', 'Marketing budget transfer',
 'COMPLETED', NULL, NULL,
 '2026-07-28 15:30:02.000', '2026-07-28 15:30:03.500', '2026-07-28 15:30:05.000', NULL,
 3, '2026-07-28 15:29:58.000', '2026-07-28 15:30:05.000'),

(108, 'PAY20260728008', 2, 4,   720.20, 'EUR', 'Equipment purchase',
 'FAILED', 'DESTINATION_ACCOUNT_DISABLED', 'Destination account is disabled',
 NULL, NULL, NULL, '2026-07-28 16:10:12.000',
 1, '2026-07-28 16:10:08.000', '2026-07-28 16:10:12.000'),

(109, 'PAY20260728009', 3, 5,  1800.00, 'GBP', 'Consulting fee',
 'SENT', NULL, NULL,
 '2026-07-28 18:00:00.000', '2026-07-28 18:00:01.400', NULL, NULL,
 2, '2026-07-28 17:59:55.000', '2026-07-28 18:00:01.400'),

(110, 'PAY20260728010', 4, 6,   99.90, 'USD', 'Subscription renewal',
 'COMPLETED', NULL, NULL,
 '2026-07-28 20:15:03.000', '2026-07-28 20:15:04.200', '2026-07-28 20:15:05.600', NULL,
 3, '2026-07-28 20:14:59.000', '2026-07-28 20:15:05.600');

-- 3. Payment status history (drives the "Payment Timeline" dialog)
DELETE FROM payment_status_history WHERE payment_id BETWEEN 101 AND 110;
INSERT INTO payment_status_history
(payment_id, from_status, to_status, reference, error_code, error_message, created_at)
VALUES
-- 101 COMPLETED
(101, NULL,        'CREATED',   'Payment created',      NULL, NULL, '2026-07-28 06:57:57.598'),
(101, 'CREATED',   'VALIDATED', 'Validation passed',    NULL, NULL, '2026-07-28 06:58:02.100'),
(101, 'VALIDATED', 'SENT',      'Payment sent',          NULL, NULL, '2026-07-28 06:58:03.400'),
(101, 'SENT',      'COMPLETED', 'Settlement completed',  NULL, NULL, '2026-07-28 06:58:04.900'),

-- 102 COMPLETED
(102, NULL,        'CREATED',   'Payment created',      NULL, NULL, '2026-07-28 07:12:05.000'),
(102, 'CREATED',   'VALIDATED', 'Validation passed',    NULL, NULL, '2026-07-28 07:12:10.200'),
(102, 'VALIDATED', 'SENT',      'Payment sent',          NULL, NULL, '2026-07-28 07:12:11.500'),
(102, 'SENT',      'COMPLETED', 'Settlement completed',  NULL, NULL, '2026-07-28 07:12:12.800'),

-- 103 SENT (in progress)
(103, NULL,        'CREATED',   'Payment created',      NULL, NULL, '2026-07-28 09:03:15.000'),
(103, 'CREATED',   'VALIDATED', 'Validation passed',    NULL, NULL, '2026-07-28 09:03:20.000'),
(103, 'VALIDATED', 'SENT',      'Payment sent',          NULL, NULL, '2026-07-28 09:03:21.300'),

-- 104 VALIDATED (in progress)
(104, NULL,        'CREATED',   'Payment created',      NULL, NULL, '2026-07-28 10:45:05.000'),
(104, 'CREATED',   'VALIDATED', 'Validation passed',    NULL, NULL, '2026-07-28 10:45:08.400'),

-- 105 CREATED (just created)
(105, NULL,        'CREATED',   'Payment created',      NULL, NULL, '2026-07-28 12:20:00.000'),

-- 106 FAILED (after SENT)
(106, NULL,        'CREATED',   'Payment created',      NULL, NULL, '2026-07-28 13:05:25.000'),
(106, 'CREATED',   'VALIDATED', 'Validation passed',    NULL, NULL, '2026-07-28 13:05:30.000'),
(106, 'VALIDATED', 'SENT',      'Payment sent',          NULL, NULL, '2026-07-28 13:05:31.200'),
(106, 'SENT',      'FAILED',    'Settlement failed', 'INSUFFICIENT_BALANCE', 'Source account balance is insufficient', '2026-07-28 13:05:32.700'),

-- 107 COMPLETED
(107, NULL,        'CREATED',   'Payment created',      NULL, NULL, '2026-07-28 15:29:58.000'),
(107, 'CREATED',   'VALIDATED', 'Validation passed',    NULL, NULL, '2026-07-28 15:30:02.000'),
(107, 'VALIDATED', 'SENT',      'Payment sent',          NULL, NULL, '2026-07-28 15:30:03.500'),
(107, 'SENT',      'COMPLETED', 'Settlement completed',  NULL, NULL, '2026-07-28 15:30:05.000'),

-- 108 FAILED (rejected right after creation, validation never passed)
(108, NULL,        'CREATED',   'Payment created',      NULL, NULL, '2026-07-28 16:10:08.000'),
(108, 'CREATED',   'FAILED',    'Validation failed', 'DESTINATION_ACCOUNT_DISABLED', 'Destination account is disabled', '2026-07-28 16:10:12.000'),

-- 109 SENT (in progress)
(109, NULL,        'CREATED',   'Payment created',      NULL, NULL, '2026-07-28 17:59:55.000'),
(109, 'CREATED',   'VALIDATED', 'Validation passed',    NULL, NULL, '2026-07-28 18:00:00.000'),
(109, 'VALIDATED', 'SENT',      'Payment sent',          NULL, NULL, '2026-07-28 18:00:01.400'),

-- 110 COMPLETED
(110, NULL,        'CREATED',   'Payment created',      NULL, NULL, '2026-07-28 20:14:59.000'),
(110, 'CREATED',   'VALIDATED', 'Validation passed',    NULL, NULL, '2026-07-28 20:15:03.000'),
(110, 'VALIDATED', 'SENT',      'Payment sent',          NULL, NULL, '2026-07-28 20:15:04.200'),
(110, 'SENT',      'COMPLETED', 'Settlement completed',  NULL, NULL, '2026-07-28 20:15:05.600');


-- ============================================================
-- Bulk demo data: 50 additional payments for service demo/testing.
-- Depends on: account, payment, payment_status_history
-- Adds 4 extra accounts (id 7-10) for more variety, on top of
-- the base accounts (id 1-6) seeded by demo_data.sql.
-- Safe to re-run: existing rows with the same id are removed first.
-- ============================================================

-- 1. Extra accounts (id 7-10) -----------------------------------
DELETE FROM account WHERE id BETWEEN 7 AND 10;
INSERT INTO account (id, account_no, name, balance, status, created_at) VALUES
                                                                            (7, 'ACC0007', 'George Miller', 65000.00, 1, '2026-01-06 09:00:00.000'),
                                                                            (8, 'ACC0008', 'Hannah Davis', 21000.25, 1, '2026-01-06 09:05:00.000'),
                                                                            (9, 'ACC0009', 'Ivan Petrov', 5400.00, 1, '2026-01-06 09:10:00.000'),
                                                                            (10, 'ACC0010', 'Julia Nguyen', 98000.00, 1, '2026-01-06 09:15:00.000');

-- 2. Payments (50 records, ids 201-250) --------------------------
DELETE FROM payment WHERE id BETWEEN 201 AND 250;
INSERT INTO payment
(id, payment_no, source_account_id, destination_account_id, amount, currency, reference,
 status, failure_code, failure_message, validated_at, sent_at, completed_at, failed_at,
 version, created_at, updated_at)
VALUES
    (201, 'PAY2026072608170000', 9, 2, 14597.27, 'CAD', 'Payroll batch #1000',
     'FAILED', 'DESTINATION_ACCOUNT_DISABLED', 'Destination account is disabled',
     '2026-07-26 08:17:04.000', NULL, NULL, '2026-07-26 08:17:07.000',
     2, '2026-07-26 08:17:00.000', '2026-07-26 08:17:07.000'),

    (202, 'PAY2026072608540001', 2, 1, 9925.72, 'CNY', 'Payroll batch #1001',
     'COMPLETED', NULL, NULL,
     '2026-07-26 08:54:02.000', '2026-07-26 08:54:06.000', '2026-07-26 08:54:09.000', NULL,
     3, '2026-07-26 08:54:00.000', '2026-07-26 08:54:09.000'),

    (203, 'PAY2026072609420002', 8, 6, 2456.56, 'AUD', 'Equipment purchase #1002',
     'COMPLETED', NULL, NULL,
     '2026-07-26 09:42:02.000', '2026-07-26 09:42:04.000', '2026-07-26 09:42:06.000', NULL,
     3, '2026-07-26 09:42:00.000', '2026-07-26 09:42:06.000'),

    (204, 'PAY2026072610310003', 3, 8, 5704.24, 'HKD', 'Marketing budget #1003',
     'COMPLETED', NULL, NULL,
     '2026-07-26 10:31:02.000', '2026-07-26 10:31:04.000', '2026-07-26 10:31:05.000', NULL,
     3, '2026-07-26 10:31:00.000', '2026-07-26 10:31:05.000'),

    (205, 'PAY2026072611180004', 6, 7, 4030.76, 'JPY', 'Freight charge #1004',
     'COMPLETED', NULL, NULL,
     '2026-07-26 11:18:03.000', '2026-07-26 11:18:07.000', '2026-07-26 11:18:11.000', NULL,
     3, '2026-07-26 11:18:00.000', '2026-07-26 11:18:11.000'),

    (206, 'PAY2026072612120005', 8, 3, 3987.90, 'JPY', 'Utility bill #1005',
     'VALIDATED', NULL, NULL,
     '2026-07-26 12:12:04.000', NULL, NULL, NULL,
     1, '2026-07-26 12:12:00.000', '2026-07-26 12:12:04.000'),

    (207, 'PAY2026072612490006', 10, 7, 13469.39, 'CAD', 'Rent payment #1006',
     'FAILED', 'INSUFFICIENT_BALANCE', 'Source account balance is insufficient',
     '2026-07-26 12:49:03.000', '2026-07-26 12:49:04.000', NULL, '2026-07-26 12:49:05.000',
     3, '2026-07-26 12:49:00.000', '2026-07-26 12:49:05.000'),

    (208, 'PAY2026072613310007', 3, 10, 11885.35, 'CAD', 'Commission payout #1007',
     'COMPLETED', NULL, NULL,
     '2026-07-26 13:31:05.000', '2026-07-26 13:31:09.000', '2026-07-26 13:31:13.000', NULL,
     3, '2026-07-26 13:31:00.000', '2026-07-26 13:31:13.000'),

    (209, 'PAY2026072614330008', 9, 5, 14566.75, 'USD', 'Refund #1008',
     'FAILED', 'INSUFFICIENT_BALANCE', 'Source account balance is insufficient',
     '2026-07-26 14:33:04.000', NULL, NULL, '2026-07-26 14:33:07.000',
     2, '2026-07-26 14:33:00.000', '2026-07-26 14:33:07.000'),

    (210, 'PAY2026072615190009', 5, 7, 2389.20, 'USD', 'Subscription renewal #1009',
     'COMPLETED', NULL, NULL,
     '2026-07-26 15:19:03.000', '2026-07-26 15:19:04.000', '2026-07-26 15:19:07.000', NULL,
     3, '2026-07-26 15:19:00.000', '2026-07-26 15:19:07.000'),

    (211, 'PAY2026072616060010', 9, 4, 2309.53, 'GBP', 'Utility bill #1010',
     'FAILED', 'INSUFFICIENT_BALANCE', 'Source account balance is insufficient',
     '2026-07-26 16:06:02.000', '2026-07-26 16:06:06.000', NULL, '2026-07-26 16:06:07.000',
     3, '2026-07-26 16:06:00.000', '2026-07-26 16:06:07.000'),

    (212, 'PAY2026072616390011', 6, 5, 3607.00, 'JPY', 'Freight charge #1011',
     'COMPLETED', NULL, NULL,
     '2026-07-26 16:39:02.000', '2026-07-26 16:39:06.000', '2026-07-26 16:39:07.000', NULL,
     3, '2026-07-26 16:39:00.000', '2026-07-26 16:39:07.000'),

    (213, 'PAY2026072617290012', 9, 3, 1943.30, 'CHF', 'Utility bill #1012',
     'COMPLETED', NULL, NULL,
     '2026-07-26 17:29:04.000', '2026-07-26 17:29:08.000', '2026-07-26 17:29:10.000', NULL,
     3, '2026-07-26 17:29:00.000', '2026-07-26 17:29:10.000'),

    (214, 'PAY2026072618250013', 9, 4, 10699.98, 'CAD', 'Rent payment #1013',
     'COMPLETED', NULL, NULL,
     '2026-07-26 18:25:05.000', '2026-07-26 18:25:06.000', '2026-07-26 18:25:08.000', NULL,
     3, '2026-07-26 18:25:00.000', '2026-07-26 18:25:08.000'),

    (215, 'PAY2026072619050014', 4, 2, 5084.52, 'SGD', 'Utility bill #1014',
     'SENT', NULL, NULL,
     '2026-07-26 19:05:03.000', '2026-07-26 19:05:04.000', NULL, NULL,
     2, '2026-07-26 19:05:00.000', '2026-07-26 19:05:04.000'),

    (216, 'PAY2026072619470015', 2, 1, 3449.55, 'USD', 'Vendor payment #1015',
     'SENT', NULL, NULL,
     '2026-07-26 19:47:03.000', '2026-07-26 19:47:06.000', NULL, NULL,
     2, '2026-07-26 19:47:00.000', '2026-07-26 19:47:06.000'),

    (217, 'PAY2026072620470016', 8, 4, 8097.72, 'SGD', 'Freight charge #1016',
     'COMPLETED', NULL, NULL,
     '2026-07-26 20:47:03.000', '2026-07-26 20:47:07.000', '2026-07-26 20:47:11.000', NULL,
     3, '2026-07-26 20:47:00.000', '2026-07-26 20:47:11.000'),

    (218, 'PAY2026072621320017', 4, 2, 1472.02, 'CAD', 'Rent payment #1017',
     'COMPLETED', NULL, NULL,
     '2026-07-26 21:32:05.000', '2026-07-26 21:32:09.000', '2026-07-26 21:32:10.000', NULL,
     3, '2026-07-26 21:32:00.000', '2026-07-26 21:32:10.000'),

    (219, 'PAY2026072622130018', 2, 1, 6051.27, 'AUD', 'Refund #1018',
     'SENT', NULL, NULL,
     '2026-07-26 22:13:03.000', '2026-07-26 22:13:05.000', NULL, NULL,
     2, '2026-07-26 22:13:00.000', '2026-07-26 22:13:05.000'),

    (220, 'PAY2026072623070019', 9, 8, 2119.93, 'GBP', 'Subscription renewal #1019',
     'COMPLETED', NULL, NULL,
     '2026-07-26 23:07:03.000', '2026-07-26 23:07:04.000', '2026-07-26 23:07:08.000', NULL,
     3, '2026-07-26 23:07:00.000', '2026-07-26 23:07:08.000'),

    (221, 'PAY2026072623420020', 9, 2, 777.81, 'HKD', 'Invoice settlement #1020',
     'VALIDATED', NULL, NULL,
     '2026-07-26 23:42:03.000', NULL, NULL, NULL,
     1, '2026-07-26 23:42:00.000', '2026-07-26 23:42:03.000'),

    (222, 'PAY2026072700280021', 3, 7, 7294.90, 'JPY', 'Loan repayment #1021',
     'COMPLETED', NULL, NULL,
     '2026-07-27 00:28:03.000', '2026-07-27 00:28:07.000', '2026-07-27 00:28:08.000', NULL,
     3, '2026-07-27 00:28:00.000', '2026-07-27 00:28:08.000'),

    (223, 'PAY2026072701270022', 7, 5, 13899.24, 'CHF', 'Reimbursement #1022',
     'SENT', NULL, NULL,
     '2026-07-27 01:27:05.000', '2026-07-27 01:27:07.000', NULL, NULL,
     2, '2026-07-27 01:27:00.000', '2026-07-27 01:27:07.000'),

    (224, 'PAY2026072702180023', 4, 5, 3281.16, 'USD', 'Freight charge #1023',
     'CREATED', NULL, NULL,
     NULL, NULL, NULL, NULL,
     0, '2026-07-27 02:18:00.000', '2026-07-27 02:18:00.000'),

    (225, 'PAY2026072703040024', 1, 6, 876.34, 'SGD', 'Insurance premium #1024',
     'VALIDATED', NULL, NULL,
     '2026-07-27 03:04:03.000', NULL, NULL, NULL,
     1, '2026-07-27 03:04:00.000', '2026-07-27 03:04:03.000'),

    (226, 'PAY2026072703540025', 1, 9, 1220.07, 'GBP', 'Payroll batch #1025',
     'FAILED', 'INSUFFICIENT_BALANCE', 'Source account balance is insufficient',
     '2026-07-27 03:54:02.000', '2026-07-27 03:54:04.000', NULL, '2026-07-27 03:54:08.000',
     3, '2026-07-27 03:54:00.000', '2026-07-27 03:54:08.000'),

    (227, 'PAY2026072704240026', 10, 4, 8692.21, 'USD', 'Commission payout #1026',
     'FAILED', 'DESTINATION_ACCOUNT_DISABLED', 'Destination account is disabled',
     '2026-07-27 04:24:05.000', '2026-07-27 04:24:08.000', NULL, '2026-07-27 04:24:11.000',
     3, '2026-07-27 04:24:00.000', '2026-07-27 04:24:11.000'),

    (228, 'PAY2026072705230027', 6, 4, 3998.91, 'GBP', 'Reimbursement #1027',
     'COMPLETED', NULL, NULL,
     '2026-07-27 05:23:04.000', '2026-07-27 05:23:05.000', '2026-07-27 05:23:06.000', NULL,
     3, '2026-07-27 05:23:00.000', '2026-07-27 05:23:06.000'),

    (229, 'PAY2026072706040028', 8, 2, 1117.45, 'JPY', 'Service fee #1028',
     'COMPLETED', NULL, NULL,
     '2026-07-27 06:04:03.000', '2026-07-27 06:04:06.000', '2026-07-27 06:04:07.000', NULL,
     3, '2026-07-27 06:04:00.000', '2026-07-27 06:04:07.000'),

    (230, 'PAY2026072706520029', 4, 6, 4289.11, 'CHF', 'Utility bill #1029',
     'VALIDATED', NULL, NULL,
     '2026-07-27 06:52:02.000', NULL, NULL, NULL,
     1, '2026-07-27 06:52:00.000', '2026-07-27 06:52:02.000'),

    (231, 'PAY2026072707380030', 9, 5, 13977.71, 'EUR', 'Contract deposit #1030',
     'CREATED', NULL, NULL,
     NULL, NULL, NULL, NULL,
     0, '2026-07-27 07:38:00.000', '2026-07-27 07:38:00.000'),

    (232, 'PAY2026072708260031', 2, 10, 11141.40, 'GBP', 'Subscription renewal #1031',
     'SENT', NULL, NULL,
     '2026-07-27 08:26:03.000', '2026-07-27 08:26:06.000', NULL, NULL,
     2, '2026-07-27 08:26:00.000', '2026-07-27 08:26:06.000'),

    (233, 'PAY2026072709060032', 4, 5, 7591.03, 'CNY', 'Supplier payment #1032',
     'FAILED', 'INSUFFICIENT_BALANCE', 'Source account balance is insufficient',
     '2026-07-27 09:06:05.000', NULL, NULL, '2026-07-27 09:06:06.000',
     2, '2026-07-27 09:06:00.000', '2026-07-27 09:06:06.000'),

    (234, 'PAY2026072710050033', 6, 3, 9563.96, 'CNY', 'Consulting fee #1033',
     'VALIDATED', NULL, NULL,
     '2026-07-27 10:05:05.000', NULL, NULL, NULL,
     1, '2026-07-27 10:05:00.000', '2026-07-27 10:05:05.000'),

    (235, 'PAY2026072710390034', 9, 1, 1695.87, 'GBP', 'Utility bill #1034',
     'COMPLETED', NULL, NULL,
     '2026-07-27 10:39:04.000', '2026-07-27 10:39:06.000', '2026-07-27 10:39:10.000', NULL,
     3, '2026-07-27 10:39:00.000', '2026-07-27 10:39:10.000'),

    (236, 'PAY2026072711310035', 3, 1, 4637.71, 'USD', 'Rent payment #1035',
     'CREATED', NULL, NULL,
     NULL, NULL, NULL, NULL,
     0, '2026-07-27 11:31:00.000', '2026-07-27 11:31:00.000'),

    (237, 'PAY2026072712310036', 4, 2, 5318.11, 'HKD', 'Bonus payout #1036',
     'CREATED', NULL, NULL,
     NULL, NULL, NULL, NULL,
     0, '2026-07-27 12:31:00.000', '2026-07-27 12:31:00.000'),

    (238, 'PAY2026072712590037', 3, 4, 12971.79, 'GBP', 'Bonus payout #1037',
     'COMPLETED', NULL, NULL,
     '2026-07-27 12:59:03.000', '2026-07-27 12:59:06.000', '2026-07-27 12:59:10.000', NULL,
     3, '2026-07-27 12:59:00.000', '2026-07-27 12:59:10.000'),

    (239, 'PAY2026072713470038', 4, 5, 2404.78, 'EUR', 'Loan repayment #1038',
     'FAILED', 'NETWORK_ERROR', 'Network communication failed. Please try again later.',
     '2026-07-27 13:47:05.000', '2026-07-27 13:47:09.000', NULL, '2026-07-27 13:47:12.000',
     3, '2026-07-27 13:47:00.000', '2026-07-27 13:47:12.000'),

    (240, 'PAY2026072714430039', 4, 10, 374.49, 'JPY', 'Loan repayment #1039',
     'SENT', NULL, NULL,
     '2026-07-27 14:43:04.000', '2026-07-27 14:43:05.000', NULL, NULL,
     2, '2026-07-27 14:43:00.000', '2026-07-27 14:43:05.000'),

    (241, 'PAY2026072715300040', 5, 6, 9629.40, 'CAD', 'Utility bill #1040',
     'COMPLETED', NULL, NULL,
     '2026-07-27 15:30:02.000', '2026-07-27 15:30:03.000', '2026-07-27 15:30:06.000', NULL,
     3, '2026-07-27 15:30:00.000', '2026-07-27 15:30:06.000'),

    (242, 'PAY2026072716180041', 3, 5, 593.09, 'SGD', 'Bonus payout #1041',
     'SENT', NULL, NULL,
     '2026-07-27 16:18:04.000', '2026-07-27 16:18:08.000', NULL, NULL,
     2, '2026-07-27 16:18:00.000', '2026-07-27 16:18:08.000'),

    (243, 'PAY2026072717020042', 10, 9, 1752.37, 'SGD', 'Equipment purchase #1042',
     'COMPLETED', NULL, NULL,
     '2026-07-27 17:02:02.000', '2026-07-27 17:02:06.000', '2026-07-27 17:02:07.000', NULL,
     3, '2026-07-27 17:02:00.000', '2026-07-27 17:02:07.000'),

    (244, 'PAY2026072717540043', 9, 10, 10309.12, 'JPY', 'Rent payment #1043',
     'COMPLETED', NULL, NULL,
     '2026-07-27 17:54:02.000', '2026-07-27 17:54:05.000', '2026-07-27 17:54:08.000', NULL,
     3, '2026-07-27 17:54:00.000', '2026-07-27 17:54:08.000'),

    (245, 'PAY2026072718400044', 2, 5, 7616.70, 'CAD', 'Vendor payment #1044',
     'COMPLETED', NULL, NULL,
     '2026-07-27 18:40:04.000', '2026-07-27 18:40:06.000', '2026-07-27 18:40:08.000', NULL,
     3, '2026-07-27 18:40:00.000', '2026-07-27 18:40:08.000'),

    (246, 'PAY2026072719330045', 7, 10, 10166.22, 'GBP', 'Commission payout #1045',
     'COMPLETED', NULL, NULL,
     '2026-07-27 19:33:04.000', '2026-07-27 19:33:08.000', '2026-07-27 19:33:09.000', NULL,
     3, '2026-07-27 19:33:00.000', '2026-07-27 19:33:09.000'),

    (247, 'PAY2026072720220046', 5, 10, 3168.54, 'SGD', 'Commission payout #1046',
     'COMPLETED', NULL, NULL,
     '2026-07-27 20:22:04.000', '2026-07-27 20:22:08.000', '2026-07-27 20:22:12.000', NULL,
     3, '2026-07-27 20:22:00.000', '2026-07-27 20:22:12.000'),

    (248, 'PAY2026072720580047', 8, 4, 7677.39, 'GBP', 'Payroll batch #1047',
     'COMPLETED', NULL, NULL,
     '2026-07-27 20:58:04.000', '2026-07-27 20:58:05.000', '2026-07-27 20:58:07.000', NULL,
     3, '2026-07-27 20:58:00.000', '2026-07-27 20:58:07.000'),

    (249, 'PAY2026072721370048', 5, 4, 12102.31, 'GBP', 'Invoice settlement #1048',
     'COMPLETED', NULL, NULL,
     '2026-07-27 21:37:03.000', '2026-07-27 21:37:07.000', '2026-07-27 21:37:08.000', NULL,
     3, '2026-07-27 21:37:00.000', '2026-07-27 21:37:08.000'),

    (250, 'PAY2026072722350049', 8, 7, 13294.33, 'SGD', 'Equipment purchase #1049',
     'VALIDATED', NULL, NULL,
     '2026-07-27 22:35:05.000', NULL, NULL, NULL,
     1, '2026-07-27 22:35:00.000', '2026-07-27 22:35:05.000');

-- 3. Payment status history (drives the "Payment Timeline" dialog)
DELETE FROM payment_status_history WHERE payment_id BETWEEN 201 AND 250;
INSERT INTO payment_status_history
(payment_id, from_status, to_status, reference, error_code, error_message, created_at)
VALUES
    (201, NULL, 'CREATED', 'Payment created', NULL, NULL, '2026-07-26 08:17:00.000'),
    (201, 'CREATED', 'VALIDATED', 'Validation passed', NULL, NULL, '2026-07-26 08:17:04.000'),
    (201, 'VALIDATED', 'FAILED', NULL, 'DESTINATION_ACCOUNT_DISABLED', 'Destination account is disabled', '2026-07-26 08:17:07.000'),
    (202, NULL, 'CREATED', 'Payment created', NULL, NULL, '2026-07-26 08:54:00.000'),
    (202, 'CREATED', 'VALIDATED', 'Validation passed', NULL, NULL, '2026-07-26 08:54:02.000'),
    (202, 'VALIDATED', 'SENT', 'Payment sent', NULL, NULL, '2026-07-26 08:54:06.000'),
    (202, 'SENT', 'COMPLETED', 'Settlement completed', NULL, NULL, '2026-07-26 08:54:09.000'),
    (203, NULL, 'CREATED', 'Payment created', NULL, NULL, '2026-07-26 09:42:00.000'),
    (203, 'CREATED', 'VALIDATED', 'Validation passed', NULL, NULL, '2026-07-26 09:42:02.000'),
    (203, 'VALIDATED', 'SENT', 'Payment sent', NULL, NULL, '2026-07-26 09:42:04.000'),
    (203, 'SENT', 'COMPLETED', 'Settlement completed', NULL, NULL, '2026-07-26 09:42:06.000'),
    (204, NULL, 'CREATED', 'Payment created', NULL, NULL, '2026-07-26 10:31:00.000'),
    (204, 'CREATED', 'VALIDATED', 'Validation passed', NULL, NULL, '2026-07-26 10:31:02.000'),
    (204, 'VALIDATED', 'SENT', 'Payment sent', NULL, NULL, '2026-07-26 10:31:04.000'),
    (204, 'SENT', 'COMPLETED', 'Settlement completed', NULL, NULL, '2026-07-26 10:31:05.000'),
    (205, NULL, 'CREATED', 'Payment created', NULL, NULL, '2026-07-26 11:18:00.000'),
    (205, 'CREATED', 'VALIDATED', 'Validation passed', NULL, NULL, '2026-07-26 11:18:03.000'),
    (205, 'VALIDATED', 'SENT', 'Payment sent', NULL, NULL, '2026-07-26 11:18:07.000'),
    (205, 'SENT', 'COMPLETED', 'Settlement completed', NULL, NULL, '2026-07-26 11:18:11.000'),
    (206, NULL, 'CREATED', 'Payment created', NULL, NULL, '2026-07-26 12:12:00.000'),
    (206, 'CREATED', 'VALIDATED', 'Validation passed', NULL, NULL, '2026-07-26 12:12:04.000'),
    (207, NULL, 'CREATED', 'Payment created', NULL, NULL, '2026-07-26 12:49:00.000'),
    (207, 'CREATED', 'VALIDATED', 'Validation passed', NULL, NULL, '2026-07-26 12:49:03.000'),
    (207, 'VALIDATED', 'SENT', 'Payment sent', NULL, NULL, '2026-07-26 12:49:04.000'),
    (207, 'SENT', 'FAILED', NULL, 'INSUFFICIENT_BALANCE', 'Source account balance is insufficient', '2026-07-26 12:49:05.000'),
    (208, NULL, 'CREATED', 'Payment created', NULL, NULL, '2026-07-26 13:31:00.000'),
    (208, 'CREATED', 'VALIDATED', 'Validation passed', NULL, NULL, '2026-07-26 13:31:05.000'),
    (208, 'VALIDATED', 'SENT', 'Payment sent', NULL, NULL, '2026-07-26 13:31:09.000'),
    (208, 'SENT', 'COMPLETED', 'Settlement completed', NULL, NULL, '2026-07-26 13:31:13.000'),
    (209, NULL, 'CREATED', 'Payment created', NULL, NULL, '2026-07-26 14:33:00.000'),
    (209, 'CREATED', 'VALIDATED', 'Validation passed', NULL, NULL, '2026-07-26 14:33:04.000'),
    (209, 'VALIDATED', 'FAILED', NULL, 'INSUFFICIENT_BALANCE', 'Source account balance is insufficient', '2026-07-26 14:33:07.000'),
    (210, NULL, 'CREATED', 'Payment created', NULL, NULL, '2026-07-26 15:19:00.000'),
    (210, 'CREATED', 'VALIDATED', 'Validation passed', NULL, NULL, '2026-07-26 15:19:03.000'),
    (210, 'VALIDATED', 'SENT', 'Payment sent', NULL, NULL, '2026-07-26 15:19:04.000'),
    (210, 'SENT', 'COMPLETED', 'Settlement completed', NULL, NULL, '2026-07-26 15:19:07.000'),
    (211, NULL, 'CREATED', 'Payment created', NULL, NULL, '2026-07-26 16:06:00.000'),
    (211, 'CREATED', 'VALIDATED', 'Validation passed', NULL, NULL, '2026-07-26 16:06:02.000'),
    (211, 'VALIDATED', 'SENT', 'Payment sent', NULL, NULL, '2026-07-26 16:06:06.000'),
    (211, 'SENT', 'FAILED', NULL, 'INSUFFICIENT_BALANCE', 'Source account balance is insufficient', '2026-07-26 16:06:07.000'),
    (212, NULL, 'CREATED', 'Payment created', NULL, NULL, '2026-07-26 16:39:00.000'),
    (212, 'CREATED', 'VALIDATED', 'Validation passed', NULL, NULL, '2026-07-26 16:39:02.000'),
    (212, 'VALIDATED', 'SENT', 'Payment sent', NULL, NULL, '2026-07-26 16:39:06.000'),
    (212, 'SENT', 'COMPLETED', 'Settlement completed', NULL, NULL, '2026-07-26 16:39:07.000'),
    (213, NULL, 'CREATED', 'Payment created', NULL, NULL, '2026-07-26 17:29:00.000'),
    (213, 'CREATED', 'VALIDATED', 'Validation passed', NULL, NULL, '2026-07-26 17:29:04.000'),
    (213, 'VALIDATED', 'SENT', 'Payment sent', NULL, NULL, '2026-07-26 17:29:08.000'),
    (213, 'SENT', 'COMPLETED', 'Settlement completed', NULL, NULL, '2026-07-26 17:29:10.000'),
    (214, NULL, 'CREATED', 'Payment created', NULL, NULL, '2026-07-26 18:25:00.000'),
    (214, 'CREATED', 'VALIDATED', 'Validation passed', NULL, NULL, '2026-07-26 18:25:05.000'),
    (214, 'VALIDATED', 'SENT', 'Payment sent', NULL, NULL, '2026-07-26 18:25:06.000'),
    (214, 'SENT', 'COMPLETED', 'Settlement completed', NULL, NULL, '2026-07-26 18:25:08.000'),
    (215, NULL, 'CREATED', 'Payment created', NULL, NULL, '2026-07-26 19:05:00.000'),
    (215, 'CREATED', 'VALIDATED', 'Validation passed', NULL, NULL, '2026-07-26 19:05:03.000'),
    (215, 'VALIDATED', 'SENT', 'Payment sent', NULL, NULL, '2026-07-26 19:05:04.000'),
    (216, NULL, 'CREATED', 'Payment created', NULL, NULL, '2026-07-26 19:47:00.000'),
    (216, 'CREATED', 'VALIDATED', 'Validation passed', NULL, NULL, '2026-07-26 19:47:03.000'),
    (216, 'VALIDATED', 'SENT', 'Payment sent', NULL, NULL, '2026-07-26 19:47:06.000'),
    (217, NULL, 'CREATED', 'Payment created', NULL, NULL, '2026-07-26 20:47:00.000'),
    (217, 'CREATED', 'VALIDATED', 'Validation passed', NULL, NULL, '2026-07-26 20:47:03.000'),
    (217, 'VALIDATED', 'SENT', 'Payment sent', NULL, NULL, '2026-07-26 20:47:07.000'),
    (217, 'SENT', 'COMPLETED', 'Settlement completed', NULL, NULL, '2026-07-26 20:47:11.000'),
    (218, NULL, 'CREATED', 'Payment created', NULL, NULL, '2026-07-26 21:32:00.000'),
    (218, 'CREATED', 'VALIDATED', 'Validation passed', NULL, NULL, '2026-07-26 21:32:05.000'),
    (218, 'VALIDATED', 'SENT', 'Payment sent', NULL, NULL, '2026-07-26 21:32:09.000'),
    (218, 'SENT', 'COMPLETED', 'Settlement completed', NULL, NULL, '2026-07-26 21:32:10.000'),
    (219, NULL, 'CREATED', 'Payment created', NULL, NULL, '2026-07-26 22:13:00.000'),
    (219, 'CREATED', 'VALIDATED', 'Validation passed', NULL, NULL, '2026-07-26 22:13:03.000'),
    (219, 'VALIDATED', 'SENT', 'Payment sent', NULL, NULL, '2026-07-26 22:13:05.000'),
    (220, NULL, 'CREATED', 'Payment created', NULL, NULL, '2026-07-26 23:07:00.000'),
    (220, 'CREATED', 'VALIDATED', 'Validation passed', NULL, NULL, '2026-07-26 23:07:03.000'),
    (220, 'VALIDATED', 'SENT', 'Payment sent', NULL, NULL, '2026-07-26 23:07:04.000'),
    (220, 'SENT', 'COMPLETED', 'Settlement completed', NULL, NULL, '2026-07-26 23:07:08.000'),
    (221, NULL, 'CREATED', 'Payment created', NULL, NULL, '2026-07-26 23:42:00.000'),
    (221, 'CREATED', 'VALIDATED', 'Validation passed', NULL, NULL, '2026-07-26 23:42:03.000'),
    (222, NULL, 'CREATED', 'Payment created', NULL, NULL, '2026-07-27 00:28:00.000'),
    (222, 'CREATED', 'VALIDATED', 'Validation passed', NULL, NULL, '2026-07-27 00:28:03.000'),
    (222, 'VALIDATED', 'SENT', 'Payment sent', NULL, NULL, '2026-07-27 00:28:07.000'),
    (222, 'SENT', 'COMPLETED', 'Settlement completed', NULL, NULL, '2026-07-27 00:28:08.000'),
    (223, NULL, 'CREATED', 'Payment created', NULL, NULL, '2026-07-27 01:27:00.000'),
    (223, 'CREATED', 'VALIDATED', 'Validation passed', NULL, NULL, '2026-07-27 01:27:05.000'),
    (223, 'VALIDATED', 'SENT', 'Payment sent', NULL, NULL, '2026-07-27 01:27:07.000'),
    (224, NULL, 'CREATED', 'Payment created', NULL, NULL, '2026-07-27 02:18:00.000'),
    (225, NULL, 'CREATED', 'Payment created', NULL, NULL, '2026-07-27 03:04:00.000'),
    (225, 'CREATED', 'VALIDATED', 'Validation passed', NULL, NULL, '2026-07-27 03:04:03.000'),
    (226, NULL, 'CREATED', 'Payment created', NULL, NULL, '2026-07-27 03:54:00.000'),
    (226, 'CREATED', 'VALIDATED', 'Validation passed', NULL, NULL, '2026-07-27 03:54:02.000'),
    (226, 'VALIDATED', 'SENT', 'Payment sent', NULL, NULL, '2026-07-27 03:54:04.000'),
    (226, 'SENT', 'FAILED', NULL, 'INSUFFICIENT_BALANCE', 'Source account balance is insufficient', '2026-07-27 03:54:08.000'),
    (227, NULL, 'CREATED', 'Payment created', NULL, NULL, '2026-07-27 04:24:00.000'),
    (227, 'CREATED', 'VALIDATED', 'Validation passed', NULL, NULL, '2026-07-27 04:24:05.000'),
    (227, 'VALIDATED', 'SENT', 'Payment sent', NULL, NULL, '2026-07-27 04:24:08.000'),
    (227, 'SENT', 'FAILED', NULL, 'DESTINATION_ACCOUNT_DISABLED', 'Destination account is disabled', '2026-07-27 04:24:11.000'),
    (228, NULL, 'CREATED', 'Payment created', NULL, NULL, '2026-07-27 05:23:00.000'),
    (228, 'CREATED', 'VALIDATED', 'Validation passed', NULL, NULL, '2026-07-27 05:23:04.000'),
    (228, 'VALIDATED', 'SENT', 'Payment sent', NULL, NULL, '2026-07-27 05:23:05.000'),
    (228, 'SENT', 'COMPLETED', 'Settlement completed', NULL, NULL, '2026-07-27 05:23:06.000'),
    (229, NULL, 'CREATED', 'Payment created', NULL, NULL, '2026-07-27 06:04:00.000'),
    (229, 'CREATED', 'VALIDATED', 'Validation passed', NULL, NULL, '2026-07-27 06:04:03.000'),
    (229, 'VALIDATED', 'SENT', 'Payment sent', NULL, NULL, '2026-07-27 06:04:06.000'),
    (229, 'SENT', 'COMPLETED', 'Settlement completed', NULL, NULL, '2026-07-27 06:04:07.000'),
    (230, NULL, 'CREATED', 'Payment created', NULL, NULL, '2026-07-27 06:52:00.000'),
    (230, 'CREATED', 'VALIDATED', 'Validation passed', NULL, NULL, '2026-07-27 06:52:02.000'),
    (231, NULL, 'CREATED', 'Payment created', NULL, NULL, '2026-07-27 07:38:00.000'),
    (232, NULL, 'CREATED', 'Payment created', NULL, NULL, '2026-07-27 08:26:00.000'),
    (232, 'CREATED', 'VALIDATED', 'Validation passed', NULL, NULL, '2026-07-27 08:26:03.000'),
    (232, 'VALIDATED', 'SENT', 'Payment sent', NULL, NULL, '2026-07-27 08:26:06.000'),
    (233, NULL, 'CREATED', 'Payment created', NULL, NULL, '2026-07-27 09:06:00.000'),
    (233, 'CREATED', 'VALIDATED', 'Validation passed', NULL, NULL, '2026-07-27 09:06:05.000'),
    (233, 'VALIDATED', 'FAILED', NULL, 'INSUFFICIENT_BALANCE', 'Source account balance is insufficient', '2026-07-27 09:06:06.000'),
    (234, NULL, 'CREATED', 'Payment created', NULL, NULL, '2026-07-27 10:05:00.000'),
    (234, 'CREATED', 'VALIDATED', 'Validation passed', NULL, NULL, '2026-07-27 10:05:05.000'),
    (235, NULL, 'CREATED', 'Payment created', NULL, NULL, '2026-07-27 10:39:00.000'),
    (235, 'CREATED', 'VALIDATED', 'Validation passed', NULL, NULL, '2026-07-27 10:39:04.000'),
    (235, 'VALIDATED', 'SENT', 'Payment sent', NULL, NULL, '2026-07-27 10:39:06.000'),
    (235, 'SENT', 'COMPLETED', 'Settlement completed', NULL, NULL, '2026-07-27 10:39:10.000'),
    (236, NULL, 'CREATED', 'Payment created', NULL, NULL, '2026-07-27 11:31:00.000'),
    (237, NULL, 'CREATED', 'Payment created', NULL, NULL, '2026-07-27 12:31:00.000'),
    (238, NULL, 'CREATED', 'Payment created', NULL, NULL, '2026-07-27 12:59:00.000'),
    (238, 'CREATED', 'VALIDATED', 'Validation passed', NULL, NULL, '2026-07-27 12:59:03.000'),
    (238, 'VALIDATED', 'SENT', 'Payment sent', NULL, NULL, '2026-07-27 12:59:06.000'),
    (238, 'SENT', 'COMPLETED', 'Settlement completed', NULL, NULL, '2026-07-27 12:59:10.000'),
    (239, NULL, 'CREATED', 'Payment created', NULL, NULL, '2026-07-27 13:47:00.000'),
    (239, 'CREATED', 'VALIDATED', 'Validation passed', NULL, NULL, '2026-07-27 13:47:05.000'),
    (239, 'VALIDATED', 'SENT', 'Payment sent', NULL, NULL, '2026-07-27 13:47:09.000'),
    (239, 'SENT', 'FAILED', NULL, 'NETWORK_ERROR', 'Network communication failed. Please try again later.', '2026-07-27 13:47:12.000'),
    (240, NULL, 'CREATED', 'Payment created', NULL, NULL, '2026-07-27 14:43:00.000'),
    (240, 'CREATED', 'VALIDATED', 'Validation passed', NULL, NULL, '2026-07-27 14:43:04.000'),
    (240, 'VALIDATED', 'SENT', 'Payment sent', NULL, NULL, '2026-07-27 14:43:05.000'),
    (241, NULL, 'CREATED', 'Payment created', NULL, NULL, '2026-07-27 15:30:00.000'),
    (241, 'CREATED', 'VALIDATED', 'Validation passed', NULL, NULL, '2026-07-27 15:30:02.000'),
    (241, 'VALIDATED', 'SENT', 'Payment sent', NULL, NULL, '2026-07-27 15:30:03.000'),
    (241, 'SENT', 'COMPLETED', 'Settlement completed', NULL, NULL, '2026-07-27 15:30:06.000'),
    (242, NULL, 'CREATED', 'Payment created', NULL, NULL, '2026-07-27 16:18:00.000'),
    (242, 'CREATED', 'VALIDATED', 'Validation passed', NULL, NULL, '2026-07-27 16:18:04.000'),
    (242, 'VALIDATED', 'SENT', 'Payment sent', NULL, NULL, '2026-07-27 16:18:08.000'),
    (243, NULL, 'CREATED', 'Payment created', NULL, NULL, '2026-07-27 17:02:00.000'),
    (243, 'CREATED', 'VALIDATED', 'Validation passed', NULL, NULL, '2026-07-27 17:02:02.000'),
    (243, 'VALIDATED', 'SENT', 'Payment sent', NULL, NULL, '2026-07-27 17:02:06.000'),
    (243, 'SENT', 'COMPLETED', 'Settlement completed', NULL, NULL, '2026-07-27 17:02:07.000'),
    (244, NULL, 'CREATED', 'Payment created', NULL, NULL, '2026-07-27 17:54:00.000'),
    (244, 'CREATED', 'VALIDATED', 'Validation passed', NULL, NULL, '2026-07-27 17:54:02.000'),
    (244, 'VALIDATED', 'SENT', 'Payment sent', NULL, NULL, '2026-07-27 17:54:05.000'),
    (244, 'SENT', 'COMPLETED', 'Settlement completed', NULL, NULL, '2026-07-27 17:54:08.000'),
    (245, NULL, 'CREATED', 'Payment created', NULL, NULL, '2026-07-27 18:40:00.000'),
    (245, 'CREATED', 'VALIDATED', 'Validation passed', NULL, NULL, '2026-07-27 18:40:04.000'),
    (245, 'VALIDATED', 'SENT', 'Payment sent', NULL, NULL, '2026-07-27 18:40:06.000'),
    (245, 'SENT', 'COMPLETED', 'Settlement completed', NULL, NULL, '2026-07-27 18:40:08.000'),
    (246, NULL, 'CREATED', 'Payment created', NULL, NULL, '2026-07-27 19:33:00.000'),
    (246, 'CREATED', 'VALIDATED', 'Validation passed', NULL, NULL, '2026-07-27 19:33:04.000'),
    (246, 'VALIDATED', 'SENT', 'Payment sent', NULL, NULL, '2026-07-27 19:33:08.000'),
    (246, 'SENT', 'COMPLETED', 'Settlement completed', NULL, NULL, '2026-07-27 19:33:09.000'),
    (247, NULL, 'CREATED', 'Payment created', NULL, NULL, '2026-07-27 20:22:00.000'),
    (247, 'CREATED', 'VALIDATED', 'Validation passed', NULL, NULL, '2026-07-27 20:22:04.000'),
    (247, 'VALIDATED', 'SENT', 'Payment sent', NULL, NULL, '2026-07-27 20:22:08.000'),
    (247, 'SENT', 'COMPLETED', 'Settlement completed', NULL, NULL, '2026-07-27 20:22:12.000'),
    (248, NULL, 'CREATED', 'Payment created', NULL, NULL, '2026-07-27 20:58:00.000'),
    (248, 'CREATED', 'VALIDATED', 'Validation passed', NULL, NULL, '2026-07-27 20:58:04.000'),
    (248, 'VALIDATED', 'SENT', 'Payment sent', NULL, NULL, '2026-07-27 20:58:05.000'),
    (248, 'SENT', 'COMPLETED', 'Settlement completed', NULL, NULL, '2026-07-27 20:58:07.000'),
    (249, NULL, 'CREATED', 'Payment created', NULL, NULL, '2026-07-27 21:37:00.000'),
    (249, 'CREATED', 'VALIDATED', 'Validation passed', NULL, NULL, '2026-07-27 21:37:03.000'),
    (249, 'VALIDATED', 'SENT', 'Payment sent', NULL, NULL, '2026-07-27 21:37:07.000'),
    (249, 'SENT', 'COMPLETED', 'Settlement completed', NULL, NULL, '2026-07-27 21:37:08.000'),
    (250, NULL, 'CREATED', 'Payment created', NULL, NULL, '2026-07-27 22:35:00.000'),
    (250, 'CREATED', 'VALIDATED', 'Validation passed', NULL, NULL, '2026-07-27 22:35:05.000');
