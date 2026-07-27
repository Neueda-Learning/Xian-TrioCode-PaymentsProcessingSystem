package org.hsbc.triocodebackend.common.constants;

public final class Constants {

    private Constants() {
    }

    public static final class Payment {
        // 逐步添加
        public static final int MAX_AMOUNT = 1_000_000;
        public static final int CURRENCY_SCALE = 2;

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
