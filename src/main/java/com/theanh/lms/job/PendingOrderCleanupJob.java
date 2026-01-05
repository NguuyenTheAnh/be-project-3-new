package com.theanh.lms.job;

import com.theanh.lms.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PendingOrderCleanupJob {

    private final OrderService orderService;

    @Scheduled(fixedDelayString = "PT5M")
    public void cleanupExpiredPendingOrders() {
        try {
            orderService.cancelExpiredPendingOrders();
        } catch (Exception ex) {
            log.error("Failed to cleanup expired pending orders", ex);
        }
    }
}
