package org.hsbc.triocodebackend.service.serviceImpl;

import org.hsbc.triocodebackend.common.enums.ErrorCodeEnum;
import org.hsbc.triocodebackend.common.enums.PaymentStatusEnum;
import org.hsbc.triocodebackend.common.exception.BizException;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Validates state transitions using a whitelist approach.
 * Non-whitelisted transitions throw INVALID_STATUS_TRANSITION.
 */
@Component
public class PaymentStateMachine {

    private static final Map<PaymentStatusEnum, Set<PaymentStatusEnum>> ALLOWED = new EnumMap<>(PaymentStatusEnum.class);

    static {
        ALLOWED.put(PaymentStatusEnum.CREATED,   EnumSet.of(PaymentStatusEnum.VALIDATED, PaymentStatusEnum.FAILED));
        ALLOWED.put(PaymentStatusEnum.VALIDATED, EnumSet.of(PaymentStatusEnum.SENT,      PaymentStatusEnum.FAILED));
        ALLOWED.put(PaymentStatusEnum.SENT,      EnumSet.of(PaymentStatusEnum.COMPLETED,  PaymentStatusEnum.FAILED));
        ALLOWED.put(PaymentStatusEnum.COMPLETED, EnumSet.noneOf(PaymentStatusEnum.class));
        ALLOWED.put(PaymentStatusEnum.FAILED,    EnumSet.noneOf(PaymentStatusEnum.class));
    }

    public void canTransit(PaymentStatusEnum from, PaymentStatusEnum to) {
        Set<PaymentStatusEnum> allowed = ALLOWED.getOrDefault(from, EnumSet.noneOf(PaymentStatusEnum.class));
        if (!allowed.contains(to)) {
            throw new BizException(ErrorCodeEnum.INVALID_STATUS_TRANSITION,
                    String.format("不允许从 %s 流转到 %s", from, to));
        }
    }
}
