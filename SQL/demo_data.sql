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
