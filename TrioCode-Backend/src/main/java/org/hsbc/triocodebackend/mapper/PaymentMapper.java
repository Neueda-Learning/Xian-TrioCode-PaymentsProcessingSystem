package org.hsbc.triocodebackend.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.hsbc.triocodebackend.model.Payment;

@Mapper
public interface PaymentMapper {

    int insert(Payment payment);

    Payment selectByPaymentNo(String paymentNo);

    Payment selectById(Long id);

    /**
     * 带乐观锁的状态更新。
     * WHERE id=#{payment.id} AND version=#{payment.version} AND status=#{expectedStatus}
     * 返回影响行数，为 0 说明发生并发冲突。
     */
    int updateStatusWithLock(@Param("payment") Payment payment,
                             @Param("expectedStatus") String expectedStatus);
}

