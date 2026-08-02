package com.example.coffee.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.coffee.dto.response.DashboardResp;
import com.example.coffee.entity.Orders;
import com.example.coffee.entity.OrderItem;
import com.example.coffee.entity.Product;
import com.example.coffee.entity.RiderLocation;
import com.example.coffee.entity.SysUser;
import com.example.coffee.mapper.*;
import com.example.coffee.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final SysUserMapper sysUserMapper;
    private final ProductMapper productMapper;
    private final ProductCategoryMapper productCategoryMapper;
    private final RiderLocationMapper riderLocationMapper;

    @Override
    public DashboardResp getDashboard() {
        DashboardResp resp = new DashboardResp();

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);

        // ---- 今日数据 ----
        List<Orders> todayOrders = orderMapper.selectList(
                new LambdaQueryWrapper<Orders>()
                        .ge(Orders::getCreateTime, todayStart)
                        .le(Orders::getCreateTime, todayEnd)
                        .ne(Orders::getStatus, 4));

        DashboardResp.DashboardToday today = new DashboardResp.DashboardToday();
        today.setOrderCount((long) todayOrders.size());
        today.setRevenue(todayOrders.stream()
                .map(o -> o.getTotalPrice().add(o.getDeliveryFee()))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .doubleValue());

        today.setNewUser(sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>()
                        .ge(SysUser::getCreateTime, todayStart)
                        .le(SysUser::getCreateTime, todayEnd)));

        // 在线骑手（5分钟内上报过位置）
        today.setRiderOnline(riderLocationMapper.selectCount(
                new LambdaQueryWrapper<RiderLocation>()
                        .ge(RiderLocation::getUpdateTime, LocalDateTime.now().minusMinutes(5))));

        resp.setToday(today);

        // ---- 累计数据 ----
        DashboardResp.DashboardTotal total = new DashboardResp.DashboardTotal();
        total.setOrderCount(orderMapper.selectCount(null));
        List<Orders> allOrders = orderMapper.selectList(
                new LambdaQueryWrapper<Orders>().ne(Orders::getStatus, 4));
        total.setRevenue(allOrders.stream()
                .map(o -> o.getTotalPrice().add(o.getDeliveryFee()))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .doubleValue());
        total.setUserCount(sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getRole, "USER")));
        total.setRiderCount(sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getRole, "RIDER")));
        resp.setTotal(total);

        // ---- 图表数据 ----
        DashboardResp.DashboardChart chart = new DashboardResp.DashboardChart();
        chart.setRecentWeekOrder(buildRecentWeekData());
        chart.setCategorySales(buildCategorySales());
        resp.setChartData(chart);

        return resp;
    }

    private List<DashboardResp.ChartPoint> buildRecentWeekData() {
        List<DashboardResp.ChartPoint> points = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd");
        for (int i = 6; i >= 0; i--) {
            LocalDate day = LocalDate.now().minusDays(i);
            LocalDateTime start = day.atStartOfDay();
            LocalDateTime end = day.atTime(LocalTime.MAX);
            long count = orderMapper.selectCount(
                    new LambdaQueryWrapper<Orders>()
                            .ge(Orders::getCreateTime, start)
                            .le(Orders::getCreateTime, end)
                            .ne(Orders::getStatus, 4));
            DashboardResp.ChartPoint p = new DashboardResp.ChartPoint();
            p.setDate(day.format(fmt));
            p.setCount((int) count);
            points.add(p);
        }
        return points;
    }

    private List<DashboardResp.ChartPie> buildCategorySales() {
        List<OrderItem> allItems = orderItemMapper.selectList(null);
        Map<Long, Integer> catCounts = new LinkedHashMap<>();
        for (OrderItem item : allItems) {
            Product product = productMapper.selectById(item.getProductId());
            if (product != null && product.getCategoryId() != null) {
                catCounts.merge(product.getCategoryId(), item.getQuantity(), Integer::sum);
            }
        }
        return catCounts.entrySet().stream()
                .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
                .limit(6)
                .map(e -> {
                    var catEntity = productCategoryMapper.selectById(e.getKey());
                    DashboardResp.ChartPie p = new DashboardResp.ChartPie();
                    p.setName(catEntity != null ? catEntity.getName() : "未知");
                    p.setValue(e.getValue());
                    return p;
                })
                .collect(Collectors.toList());
    }
}
