package org.hsbc.triocodebackend.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.hsbc.triocodebackend.dto.PaymentListQueryDTO;
import org.hsbc.triocodebackend.model.Payment;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface PaymentRepository {

    Payment selectById(@Param("id") Long id);

    Payment selectByPaymentNo(@Param("paymentNo") String paymentNo);

    long countByQuery(@Param("query") PaymentListQueryDTO query);

    List<Payment> selectByQuery(@Param("query") PaymentListQueryDTO query,
                                @Param("offset") int offset,
                                @Param("limit") int limit);

    int insert(Payment payment);

    int updateToValidated(@Param("id") Long id,
                          @Param("version") Integer version,
                          @Param("validatedAt") LocalDateTime validatedAt);

    int updateToSent(@Param("id") Long id,
                     @Param("version") Integer version,
                     @Param("sentAt") LocalDateTime sentAt);

    int updateToCompleted(@Param("id") Long id,
                          @Param("version") Integer version,
                          @Param("completedAt") LocalDateTime completedAt);

    int updateToFailed(@Param("id") Long id,
                       @Param("version") Integer version,
                       @Param("currentStatus") String currentStatus,
                       @Param("failedAt") LocalDateTime failedAt,
                       @Param("failureCode") String failureCode,
                       @Param("failureMessage") String failureMessage);

    int updateToFailedAny(@Param("id") Long id,
                          @Param("version") Integer version,
                          @Param("failedAt") LocalDateTime failedAt,
                          @Param("failureCode") String failureCode,
                          @Param("failureMessage") String failureMessage);
}
