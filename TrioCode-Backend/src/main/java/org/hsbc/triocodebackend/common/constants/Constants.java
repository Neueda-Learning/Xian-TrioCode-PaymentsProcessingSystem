package org.hsbc.triocodebackend.common.constants;

import java.math.BigDecimal;

public final class Constants {

    private Constants() {
    }

    public static final class Payment {
        public static final int MAX_AMOUNT = 1_000_000;
        public static final int CURRENCY_SCALE = 2;
        public static final BigDecimal MAX_AMOUNT_DECIMAL = BigDecimal.valueOf(MAX_AMOUNT);
        public static final int MAX_SEND_RETRIES = 3;
        public static final String OPERATION_DEBIT = "DEBIT";
        public static final String OPERATION_CREDIT = "CREDIT";

        private Payment() {
        }
    }

    public static final class Regex {
        // 仅示例，按规则重新调整
        public static final String CURRENCY = "^[A-Z]{3}$";

        private Regex() {
        }
    }
}
