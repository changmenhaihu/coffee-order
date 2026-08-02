package com.example.coffee.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.coffee.entity.OrderStatusLog;
import com.example.coffee.entity.Orders;
import com.example.coffee.mapper.OrderMapper;
import com.example.coffee.mapper.OrderStatusLogMapper;
import com.example.coffee.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCancelTask {

    private static final String LOCK_KEY = "task:cancel:expired:orders";
    private static final int BATCH_SIZE = 100;

    private final OrderMapper orderMapper;
    private final OrderStatusLogMapper orderStatusLogMapper;
    private final RedisUtil redisUtil;

    @Scheduled(cron = "0 * * * * ?")
    public void cancelExpiredOrders() {
        if (!redisUtil.tryLock(LOCK_KEY, 55, TimeUnit.SECONDS)) {
            return;
        }
        try {
            List<Orders> expired = orderMapper.selectList(
                    new LambdaQueryWrapper<Orders>()
                            .eq(Orders::getStatus, 0)
                            .lt(Orders::getCreateTime, LocalDateTime.now().minusMinutes(5))
                            .last("LIMIT " + BATCH_SIZE));

            for (Orders order : expired) {
                cancelOrder(order);
            }
            if (!expired.isEmpty()) {
                log.info("Auto-cancelled {} expired unpaid orders", expired.size());
            }
        } catch (Exception e) {
            log.error("Failed to cancel expired orders", e);
        } finally {
            redisUtil.unlock(LOCK_KEY);
        }
    }

    @Transactional
    public void cancelOrder(Orders order) {
        order.setStatus(4);
        order.setCancelTime(LocalDateTime.now());
        order.setCancelReason("支付超时自动取消");
        orderMapper.updateById(order);

        OrderStatusLog log = new OrderStatusLog();
        log.setOrderId(order.getId());
        log.setFromStatus(0);
        log.setToStatus(4);
        log.setOperatorId(null);
        log.setOperatorType(0);
        log.setRemark("支付超时自动取消");
        orderStatusLogMapper.insert(log);
    }
}
