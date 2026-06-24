package com.resos.modules.analytics.service;

import com.resos.modules.analytics.dto.*;
import com.resos.modules.employee.domain.Employee;
import com.resos.modules.employee.domain.EmployeeSchedule;
import com.resos.modules.employee.domain.EmployeeStatus;
import com.resos.modules.employee.repository.EmployeeRepository;
import com.resos.modules.employee.repository.EmployeeScheduleRepository;
import com.resos.modules.inventory.domain.InventoryItem;
import com.resos.modules.inventory.domain.TransactionType;
import com.resos.modules.inventory.repository.InventoryItemRepository;
import com.resos.modules.inventory.repository.InventoryTransactionRepository;
import com.resos.modules.order.domain.OrderStatus;
import com.resos.modules.order.repository.RestaurantOrderRepository;
import com.resos.modules.restaurant.repository.RestaurantRepository;
import com.resos.shared.exception.BusinessException;
import com.resos.shared.tenant.TenantContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantOrderRepository orderRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final EmployeeRepository employeeRepository;
    private final EmployeeScheduleRepository scheduleRepository;

    @Transactional(readOnly = true)
    public RevenueAnalyticsResponse getRevenueAnalytics(
            UUID restaurantId, LocalDate startDate, LocalDate endDate, String groupBy) {
        UUID tenantId = validateRestaurant(tenantId(), restaurantId);
        DateRange range = resolveRange(startDate, endDate, 30);

        List<String> labels = new ArrayList<>();
        List<BigDecimal> values = new ArrayList<>();
        long orderCount = 0;
        BigDecimal totalRevenue = BigDecimal.ZERO;

        for (LocalDate date = range.start(); !date.isAfter(range.end()); date = date.plusDays(1)) {
            Instant dayStart = date.atStartOfDay(ZoneOffset.UTC).toInstant();
            Instant dayEnd = date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
            BigDecimal dayRevenue = orderRepository.sumCompletedRevenue(tenantId, restaurantId, dayStart, dayEnd);
            labels.add(date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH));
            values.add(dayRevenue);
            totalRevenue = totalRevenue.add(dayRevenue);
            orderCount += orderRepository.countByTenantIdAndRestaurantIdAndCreatedAtBetweenAndStatus(
                    tenantId, restaurantId, dayStart, dayEnd, OrderStatus.COMPLETED);
        }

        BigDecimal avgOrderValue = orderCount > 0
                ? totalRevenue.divide(BigDecimal.valueOf(orderCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        DateRange previous = previousPeriod(range);
        BigDecimal previousRevenue = orderRepository.sumCompletedRevenue(
                tenantId,
                restaurantId,
                previous.start().atStartOfDay(ZoneOffset.UTC).toInstant(),
                previous.end().plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant());

        double changePercent = percentChange(previousRevenue, totalRevenue);

        return new RevenueAnalyticsResponse(totalRevenue, orderCount, avgOrderValue, changePercent, labels, values);
    }

    @Transactional(readOnly = true)
    public InventoryAnalyticsResponse getInventoryAnalytics(UUID restaurantId, String period) {
        UUID tenantId = validateRestaurant(tenantId(), restaurantId);
        DateRange range = periodRange(period);

        Instant start = range.start().atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant end = range.end().plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        long totalItems = inventoryItemRepository.countByTenantIdAndRestaurantIdAndDeletedAtIsNull(tenantId, restaurantId);
        long lowStockItems = inventoryItemRepository.countLowStockByRestaurant(tenantId, restaurantId);
        BigDecimal inventoryValue = inventoryItemRepository.sumInventoryValue(tenantId, restaurantId);
        long wasteTransactions = inventoryTransactionRepository.countByRestaurantAndTypeBetween(
                tenantId, restaurantId, TransactionType.WASTE, start, end);
        BigDecimal wasteCost =
                inventoryTransactionRepository.sumWasteCostByRestaurantBetween(tenantId, restaurantId, start, end);
        long usageTransactions = inventoryTransactionRepository.countByRestaurantAndTypeBetween(
                tenantId, restaurantId, TransactionType.USAGE, start, end);

        List<CategoryStockSummary> topCategories = inventoryItemRepository
                .findByTenantIdAndRestaurantIdAndDeletedAtIsNull(tenantId, restaurantId)
                .stream()
                .collect(Collectors.groupingBy(
                        item -> item.getCategory() != null ? item.getCategory() : "Uncategorized",
                        Collectors.toList()))
                .entrySet()
                .stream()
                .map(entry -> new CategoryStockSummary(
                        entry.getKey(),
                        entry.getValue().size(),
                        entry.getValue().stream()
                                .map(item -> item.getCurrentStock()
                                        .multiply(item.getUnitCost() != null ? item.getUnitCost() : BigDecimal.ZERO))
                                .reduce(BigDecimal.ZERO, BigDecimal::add)))
                .sorted(Comparator.comparing(CategoryStockSummary::stockValue).reversed())
                .limit(5)
                .toList();

        return new InventoryAnalyticsResponse(
                totalItems,
                lowStockItems,
                inventoryValue,
                wasteTransactions,
                wasteCost,
                usageTransactions,
                topCategories);
    }

    @Transactional(readOnly = true)
    public EmployeeAnalyticsResponse getEmployeeAnalytics(
            UUID restaurantId, LocalDate startDate, LocalDate endDate) {
        UUID tenantId = validateRestaurant(tenantId(), restaurantId);
        DateRange range = resolveRange(startDate, endDate, 30);

        long activeEmployees = employeeRepository.countByTenantIdAndRestaurantIdAndStatusAndDeletedAtIsNull(
                tenantId, restaurantId, EmployeeStatus.ACTIVE);
        List<EmployeeSchedule> schedules = scheduleRepository.findByTenantIdAndRestaurantIdAndShiftDateBetween(
                tenantId, restaurantId, range.start(), range.end());
        Map<UUID, Employee> employees = employeeRepository
                .findByTenantIdAndRestaurantIdAndDeletedAtIsNull(tenantId, restaurantId)
                .stream()
                .collect(Collectors.toMap(Employee::getId, employee -> employee));

        BigDecimal totalHours = BigDecimal.ZERO;
        BigDecimal laborCost = BigDecimal.ZERO;
        Map<String, PositionAccumulator> positions = new HashMap<>();

        for (EmployeeSchedule schedule : schedules) {
            long minutes = Duration.between(schedule.getStartTime(), schedule.getEndTime()).toMinutes();
            BigDecimal hours = BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
            totalHours = totalHours.add(hours);

            Employee employee = employees.get(schedule.getEmployeeId());
            if (employee != null && employee.getHourlyRate() != null) {
                laborCost = laborCost.add(hours.multiply(employee.getHourlyRate()));
            }

            String position = employee != null ? employee.getPosition() : "Unknown";
            positions.computeIfAbsent(position, key -> new PositionAccumulator())
                    .add(employee != null ? employee.getId() : null);
        }

        List<PositionSummary> byPosition = positions.entrySet().stream()
                .map(entry -> new PositionSummary(
                        entry.getKey(),
                        entry.getValue().employeeIds.size(),
                        entry.getValue().shiftCount))
                .sorted(Comparator.comparing(PositionSummary::scheduledShifts).reversed())
                .toList();

        return new EmployeeAnalyticsResponse(
                activeEmployees, schedules.size(), totalHours, laborCost, byPosition);
    }

    @Transactional(readOnly = true)
    public OrderAnalyticsResponse getOrderAnalytics(UUID restaurantId, LocalDate startDate, LocalDate endDate) {
        UUID tenantId = validateRestaurant(tenantId(), restaurantId);
        DateRange range = resolveRange(startDate, endDate, 30);

        Instant start = range.start().atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant end = range.end().plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        long totalOrders = orderRepository.countByTenantIdAndRestaurantIdAndCreatedAtBetween(
                tenantId, restaurantId, start, end);
        long completedOrders = orderRepository.countByTenantIdAndRestaurantIdAndCreatedAtBetweenAndStatus(
                tenantId, restaurantId, start, end, OrderStatus.COMPLETED);
        BigDecimal revenue = orderRepository.sumCompletedRevenue(tenantId, restaurantId, start, end);
        BigDecimal avgTicket = completedOrders > 0
                ? revenue.divide(BigDecimal.valueOf(completedOrders), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        List<PeakHourSummary> peakHours = orderRepository.countOrdersGroupedByHour(tenantId, restaurantId, start, end)
                .stream()
                .limit(5)
                .map(row -> new PeakHourSummary(((Number) row[0]).intValue(), ((Number) row[1]).longValue()))
                .toList();

        List<StatusSummary> byStatus = orderRepository
                .countOrdersGroupedByStatus(tenantId, restaurantId, start, end)
                .stream()
                .map(row -> new StatusSummary(((OrderStatus) row[0]).name(), (Long) row[1]))
                .toList();

        return new OrderAnalyticsResponse(totalOrders, completedOrders, avgTicket, peakHours, byStatus);
    }

    private UUID tenantId() {
        return TenantContextHolder.requireTenantId();
    }

    private UUID validateRestaurant(UUID tenantId, UUID restaurantId) {
        restaurantRepository
                .findByIdAndTenantIdAndDeletedAtIsNull(restaurantId, tenantId)
                .orElseThrow(() -> new BusinessException("NOT_FOUND", "Restaurant not found"));
        return tenantId;
    }

    private DateRange resolveRange(LocalDate startDate, LocalDate endDate, int defaultDays) {
        LocalDate end = endDate != null ? endDate : LocalDate.now();
        LocalDate start = startDate != null ? startDate : end.minusDays(defaultDays - 1L);
        if (start.isAfter(end)) {
            throw new BusinessException("VALIDATION_ERROR", "startDate must be on or before endDate");
        }
        return new DateRange(start, end);
    }

    private DateRange periodRange(String period) {
        LocalDate end = LocalDate.now();
        int days = "WEEK".equalsIgnoreCase(period) ? 7 : 30;
        return new DateRange(end.minusDays(days - 1L), end);
    }

    private DateRange previousPeriod(DateRange current) {
        long days = current.end().toEpochDay() - current.start().toEpochDay() + 1;
        LocalDate previousEnd = current.start().minusDays(1);
        LocalDate previousStart = previousEnd.minusDays(days - 1);
        return new DateRange(previousStart, previousEnd);
    }

    private double percentChange(BigDecimal previous, BigDecimal current) {
        if (previous.compareTo(BigDecimal.ZERO) == 0) {
            return current.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0;
        }
        return current.subtract(previous)
                .divide(previous, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }

    private record DateRange(LocalDate start, LocalDate end) {}

    private static final class PositionAccumulator {
        private final Set<UUID> employeeIds = new HashSet<>();
        private long shiftCount;

        void add(UUID employeeId) {
            shiftCount++;
            if (employeeId != null) {
                employeeIds.add(employeeId);
            }
        }
    }
}
