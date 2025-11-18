package com.mailshop_dragonvu.service.impl;

import com.mailshop_dragonvu.dto.orders.OrderCreateRequest;
import com.mailshop_dragonvu.dto.orders.OrderUpdateRequest;
import com.mailshop_dragonvu.dto.orders.OrderResponse;
import com.mailshop_dragonvu.entity.Order;
import com.mailshop_dragonvu.entity.OrderItem;
import com.mailshop_dragonvu.entity.User;
import com.mailshop_dragonvu.enums.OrderStatus;
import com.mailshop_dragonvu.exception.BusinessException;
import com.mailshop_dragonvu.exception.ErrorCode;
import com.mailshop_dragonvu.mapper.OrderItemMapper;
import com.mailshop_dragonvu.mapper.OrderMapper;
import com.mailshop_dragonvu.repository.OrderRepository;
import com.mailshop_dragonvu.repository.UserRepository;
import com.mailshop_dragonvu.service.EmailService;
import com.mailshop_dragonvu.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final EmailService emailService;

    @Override
    @Transactional
    @CacheEvict(value = "orders", allEntries = true)
    public OrderResponse createOrder(OrderCreateRequest request, Long userId) {
        log.info("Creating new order for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Order order = orderMapper.toEntity(request);
        order.setUser(user);
        order.setOrderNumber(generateOrderNumber());
        order.setOrderStatus(OrderStatus.PENDING);

        // Set discount and tax amounts
        order.setDiscountAmount(request.getDiscountAmount() != null ? 
                request.getDiscountAmount() : BigDecimal.ZERO);

        // Add order items
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (var itemRequest : request.getOrderItems()) {
            OrderItem orderItem = orderItemMapper.toEntity(itemRequest);
            
            // Set default values if not provided
            if (orderItem.getDiscountAmount() == null) {
                orderItem.setDiscountAmount(BigDecimal.ZERO);
            }

            order.addOrderItem(orderItem);
            totalAmount = totalAmount.add(orderItem.getTotalPrice());
        }

        order.setTotalAmount(totalAmount);
        order.calculateFinalAmount();

        order = orderRepository.save(order);
        log.info("Order created successfully with order number: {}", order.getOrderNumber());

        // Send order confirmation email asynchronously
        try {
            emailService.sendOrderConfirmationEmail(order);
        } catch (Exception e) {
            log.error("Failed to send order confirmation email for order {}: {}", 
                    order.getOrderNumber(), e.getMessage());
        }

        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional
    @CacheEvict(value = "orders", key = "#id")
    public OrderResponse updateOrder(Long id, OrderUpdateRequest request, Long userId) {
        log.info("Updating order ID: {} by user ID: {}", id, userId);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        // Check if user owns the order or is admin
        if (!order.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED);
        }

        // Only allow updates for PENDING orders
        if (!order.getOrderStatus().equals(OrderStatus.PENDING)) {
            throw new BusinessException(ErrorCode.ORDER_CANNOT_BE_MODIFIED);
        }

        orderMapper.updateEntity(order, request);
        order = orderRepository.save(order);

        log.info("Order updated successfully: {}", id);
        return orderMapper.toResponse(order);
    }

    @Override
    @Cacheable(value = "orders", key = "#id")
    public OrderResponse getOrderById(Long id, Long userId) {
        log.debug("Fetching order by ID: {}", id);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        // Check if user owns the order
        if (!order.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED);
        }

        return orderMapper.toResponse(order);
    }

    @Override
    @Cacheable(value = "orders", key = "#orderNumber")
    public OrderResponse getOrderByNumber(String orderNumber, Long userId) {
        log.debug("Fetching order by number: {}", orderNumber);

        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        // Check if user owns the order
        if (!order.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED);
        }

        return orderMapper.toResponse(order);
    }

    @Override
    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        log.debug("Fetching all orders with pagination");
        return orderRepository.findAll(pageable)
                .map(orderMapper::toResponse);
    }

    @Override
    public Page<OrderResponse> getOrdersByUser(Long userId, Pageable pageable) {
        log.debug("Fetching orders for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return orderRepository.findByUser(user, pageable)
                .map(orderMapper::toResponse);
    }

    @Override
    public Page<OrderResponse> getOrdersByStatus(OrderStatus status, Pageable pageable) {
        log.debug("Fetching orders by status: {}", status);
        return orderRepository.findByOrderStatus(status, pageable)
                .map(orderMapper::toResponse);
    }

    @Override
    @Transactional
    @CacheEvict(value = "orders", key = "#id")
    public OrderResponse updateOrderStatus(Long id, OrderStatus status) {
        log.info("Updating order ID: {} status to: {}", id, status);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        order.setOrderStatus(status);
        order = orderRepository.save(order);

        // Send order status update email asynchronously
        try {
            emailService.sendOrderStatusUpdateEmail(order);
        } catch (Exception e) {
            log.error("Failed to send order status update email for order {}: {}", 
                    order.getOrderNumber(), e.getMessage());
        }

        log.info("Order status updated successfully: {}", id);
        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional
    @CacheEvict(value = "orders", key = "#id")
    public OrderResponse confirmOrder(Long id) {
        log.info("Confirming order ID: {}", id);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getOrderStatus().equals(OrderStatus.PENDING)) {
            throw new BusinessException(ErrorCode.INVALID_ORDER_STATUS);
        }

        order.setOrderStatus(OrderStatus.CONFIRMED);
        order = orderRepository.save(order);

        // Send order status update email asynchronously
        try {
            emailService.sendOrderStatusUpdateEmail(order);
        } catch (Exception e) {
            log.error("Failed to send order status update email for order {}: {}", 
                    order.getOrderNumber(), e.getMessage());
        }

        log.info("Order confirmed successfully: {}", id);
        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional
    @CacheEvict(value = "orders", key = "#id")
    public OrderResponse cancelOrder(Long id, String reason, Long userId) {
        log.info("Cancelling order ID: {} by user ID: {}", id, userId);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        // Check if user owns the order
        if (!order.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED);
        }

        if (order.getOrderStatus().equals(OrderStatus.CANCELLED)) {
            throw new BusinessException(ErrorCode.ORDER_ALREADY_CANCELLED);
        }


        order.setOrderStatus(OrderStatus.CANCELLED);
        order.setCancelledDate(LocalDateTime.now());
        order.setCancellationReason(reason);
        order = orderRepository.save(order);

        log.info("Order cancelled successfully: {}", id);
        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional
    @CacheEvict(value = "orders", key = "#id")
    public void deleteOrder(Long id) {
        log.info("Deleting order with ID: {}", id);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        order.setOrderStatus(OrderStatus.DELETED);
        orderRepository.save(order);

        log.info("Order deleted successfully with ID: {}", id);
    }

    private String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

}
