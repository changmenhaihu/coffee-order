package com.example.coffee.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class DashboardResp {

    private DashboardToday today;
    private DashboardTotal total;
    private DashboardChart chartData;

    @Data
    public static class DashboardToday {
        private Long orderCount;
        private Double revenue;
        private Long newUser;
        private Long riderOnline;
    }

    @Data
    public static class DashboardTotal {
        private Long orderCount;
        private Double revenue;
        private Long userCount;
        private Long riderCount;
    }

    @Data
    public static class DashboardChart {
        private List<ChartPoint> recentWeekOrder;
        private List<ChartPie> categorySales;
    }

    @Data
    public static class ChartPoint {
        private String date;
        private Integer count;
    }

    @Data
    public static class ChartPie {
        private String name;
        private Integer value;
    }
}
