package org.hsbc.triocodebackend.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.hsbc.triocodebackend.model.Payment;
import org.hsbc.triocodebackend.model.vo.PaymentListItemVO;

import java.time.LocalDateTime;
import java.util.List;

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

    long countByQuery(@Param("status") String status,
                      @Param("paymentNo") String paymentNo,
                      @Param("reference") String reference,
                      @Param("currency") String currency,
                      @Param("createdFrom") LocalDateTime createdFrom,
                      @Param("createdTo") LocalDateTime createdTo);

    List<PaymentListItemVO> selectPageByQuery(@Param("status") String status,
                                    @Param("paymentNo") String paymentNo,
                                    @Param("reference") String reference,
                                    @Param("currency") String currency,
                                    @Param("createdFrom") LocalDateTime createdFrom,
                                    @Param("createdTo") LocalDateTime createdTo,
                                    @Param("offset") int offset,
                                    @Param("limit") int limit);
}
