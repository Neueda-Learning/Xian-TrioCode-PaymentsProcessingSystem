package org.hsbc.triocodebackend.service;

import org.hsbc.triocodebackend.common.enums.ErrorCodeEnum;
import org.hsbc.triocodebackend.common.enums.PaymentStatusEnum;
import org.hsbc.triocodebackend.common.exception.BizException;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 支付状态机：使用白名单映射控制合法流转，非法流转直接抛出业务异常。
 */
@Component
public class PaymentStateMachine {

    private static final Map<PaymentStatusEnum, Set<PaymentStatusEnum>> ALLOWED =
            new EnumMap<>(PaymentStatusEnum.class);

    static {
        ALLOWED.put(PaymentStatusEnum.CREATED,
                EnumSet.of(PaymentStatusEnum.VALIDATED, PaymentStatusEnum.FAILED));
        ALLOWED.put(PaymentStatusEnum.VALIDATED,
                EnumSet.of(PaymentStatusEnum.SENT, PaymentStatusEnum.FAILED));
        ALLOWED.put(PaymentStatusEnum.SENT,
                EnumSet.of(PaymentStatusEnum.COMPLETED, PaymentStatusEnum.FAILED));
        ALLOWED.put(PaymentStatusEnum.COMPLETED, Collections.emptySet());
        ALLOWED.put(PaymentStatusEnum.FAILED, Collections.emptySet());
    }

    /**
     * 检查 from -> to 是否合法，非法则抛出 INVALID_STATUS_TRANSITION。
     */
    public void checkTransition(String from, String to) {
        PaymentStatusEnum fromStatus = PaymentStatusEnum.valueOf(from);
        PaymentStatusEnum toStatus   = PaymentStatusEnum.valueOf(to);
        Set<PaymentStatusEnum> allowed = ALLOWED.getOrDefault(fromStatus, Collections.emptySet());
        if (!allowed.contains(toStatus)) {
            throw new BizException(ErrorCodeEnum.INVALID_STATUS_TRANSITION,
                    String.format("Transition from %s to %s is not allowed.", from, to));
        }
    }
}

