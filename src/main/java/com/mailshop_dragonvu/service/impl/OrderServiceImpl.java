package com.mailshop_dragonvu.service.impl;

import com.mailshop_dragonvu.dto.orders.OrderCreateDTO;
import com.mailshop_dragonvu.dto.orders.OrderFilterDTO;
import com.mailshop_dragonvu.dto.orders.OrderResponseDTO;
import com.mailshop_dragonvu.dto.orders.OrderUpdateDTO;
import com.mailshop_dragonvu.entity.OrderEntity;
import com.mailshop_dragonvu.entity.OrderItemEntity;
import com.mailshop_dragonvu.entity.UserEntity;
import com.mailshop_dragonvu.enums.OrderStatusEnum;
import com.mailshop_dragonvu.exception.BusinessException;
import com.mailshop_dragonvu.exception.ErrorCode;
import com.mailshop_dragonvu.mapper.OrderItemMapper;
import com.mailshop_dragonvu.mapper.OrderMapper;
import com.mailshop_dragonvu.repository.OrderRepository;
import com.mailshop_dragonvu.repository.UserRepository;
import com.mailshop_dragonvu.service.EmailService;
import com.mailshop_dragonvu.service.OrderService;
import com.mailshop_dragonvu.utils.Utils;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    public OrderResponseDTO createOrder(OrderCreateDTO request, Long userId) {
        log.info("Creating new order for user ID: {}", userId);

        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        OrderEntity orderEntity = orderMapper.toEntity(request);
        orderEntity.setUser(userEntity);
        orderEntity.setOrderNumber(generateOrderNumber());
        orderEntity.setOrderStatus(OrderStatusEnum.PENDING);

        // Set discount and tax amounts
        orderEntity.setDiscountAmount(request.getDiscountAmount() != null ?
                request.getDiscountAmount() : BigDecimal.ZERO);

        // Add order items
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (var itemRequest : request.getOrderItems()) {
            OrderItemEntity orderItemEntity = orderItemMapper.toEntity(itemRequest);
            
            // Set default values if not provided
            if (orderItemEntity.getDiscountAmount() == null) {
                orderItemEntity.setDiscountAmount(BigDecimal.ZERO);
            }

            orderEntity.addOrderItem(orderItemEntity);
            totalAmount = totalAmount.add(orderItemEntity.getTotalPrice());
        }

        orderEntity.setTotalAmount(totalAmount);
        orderEntity.calculateFinalAmount();

        orderEntity = orderRepository.save(orderEntity);

        // Send order confirmation email asynchronously
//        try {
//            emailService.sendOrderConfirmationEmail(orderEntity);
//        } catch (Exception e) {
//            log.error("Failed to send order confirmation email for order {}: {}",
//                    orderEntity.getOrderNumber(), e.getMessage());
//        }

        return orderMapper.toResponse(orderEntity);
    }

    @Override
    @Transactional
    @CacheEvict(value = "orders", key = "#id")
    public OrderResponseDTO updateOrder(Long id, OrderUpdateDTO request, Long userId) {

        OrderEntity orderEntity = orderRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        // Check if user owns the order or is admin
        if (!orderEntity.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED);
        }

        // Only allow updates for PENDING orders
        if (!orderEntity.getOrderStatus().equals(com.mailshop_dragonvu.enums.OrderStatusEnum.PENDING)) {
            throw new BusinessException(ErrorCode.ORDER_CANNOT_BE_MODIFIED);
        }

        orderMapper.updateEntity(orderEntity, request);
        orderEntity = orderRepository.save(orderEntity);

        return orderMapper.toResponse(orderEntity);
    }

    @Override
    @Cacheable(value = "orders", key = "{#id, #userId}")
    public OrderResponseDTO getOrderById(Long id, Long userId) {
        log.debug("Fetching order by ID: {}", id);

        OrderEntity orderEntity = orderRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        // Check if user owns the order
        if (!orderEntity.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED);
        }

        return orderMapper.toResponse(orderEntity);
    }

    @Override
    @Cacheable(value = "orders", key = "#orderNumber")
    public OrderResponseDTO getOrderByNumber(String orderNumber, Long userId) {
        log.debug("Fetching order by number: {}", orderNumber);

        OrderEntity orderEntity = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        // Check if user owns the order
        if (!orderEntity.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED);
        }

        return orderMapper.toResponse(orderEntity);
    }

    @Override
    public Page<OrderResponseDTO> search(OrderFilterDTO orderFilterDTO) {
        Sort sort = Utils.generatedSort(orderFilterDTO.getSort());
        Pageable pageable = PageRequest.of(orderFilterDTO.getPage(), orderFilterDTO.getLimit(), sort);

        Specification<OrderEntity> specification = getSearchSpecification(orderFilterDTO);

        return orderRepository.findAll(specification, pageable)
                .map(orderMapper::toResponse);
    }
    private Specification<OrderEntity> getSearchSpecification(final OrderFilterDTO req) {

        return new Specification<OrderEntity>() {

            private static final long serialVersionUID = 1L;

            @Override
            public Predicate toPredicate(Root<OrderEntity> root,
                                         CriteriaQuery<?> query,
                                         CriteriaBuilder cb) {

                List<Predicate> predicates = new ArrayList<>();

                // ORDER NUMBER
                if (Strings.isNotBlank(req.getOrderNumber())) {
                    predicates.add(cb.like(cb.lower(root.get("orderNumber")),
                            "%" + req.getOrderNumber().trim().toLowerCase() + "%"));
                }

                // USER ID
                if (req.getUserId() != null) {
                    predicates.add(cb.equal(root.get("user").get("id"), req.getUserId()));
                }

                // USER EMAIL
                if (Strings.isNotBlank(req.getUserEmail())) {
                    predicates.add(cb.like(cb.lower(root.get("user").get("email")),
                            "%" + req.getUserEmail().trim().toLowerCase() + "%"));
                }

                // ORDER STATUS
                if (Strings.isNotBlank(req.getOrderStatus())) {
                    Set<String> statuses = Arrays.stream(req.getOrderStatus().split(","))
                            .map(String::trim)
                            .collect(Collectors.toSet());
                    predicates.add(root.get("status").in(statuses));
                }

                // TOTAL AMOUNT
                if (req.getTotalAmount() != null) {
                    predicates.add(cb.equal(root.get("totalAmount"), req.getTotalAmount()));
                }

                // DISCOUNT AMOUNT
                if (req.getDiscountAmount() != null) {
                    predicates.add(cb.equal(root.get("discountAmount"), req.getDiscountAmount()));
                }

                // FINAL AMOUNT
                if (req.getFinalAmount() != null) {
                    predicates.add(cb.equal(root.get("finalAmount"), req.getFinalAmount()));
                }

                // PHONE
                if (Strings.isNotBlank(req.getPhone())) {
                    predicates.add(cb.like(cb.lower(root.get("phone")),
                            "%" + req.getPhone().trim().toLowerCase() + "%"));
                }

                // EMAIL
                if (Strings.isNotBlank(req.getEmail())) {
                    predicates.add(cb.like(cb.lower(root.get("email")),
                            "%" + req.getEmail().trim().toLowerCase() + "%"));
                }

                // NOTES
                if (Strings.isNotBlank(req.getNotes())) {
                    predicates.add(cb.like(cb.lower(root.get("notes")),
                            "%" + req.getNotes().trim().toLowerCase() + "%"));
                }

                // CREATED AT (>=)
                if (req.getCreatedAt() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), req.getCreatedAt()));
                }

                // UPDATED AT (>=)
                if (req.getUpdatedAt() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("updatedAt"), req.getUpdatedAt()));
                }

                return cb.and(predicates.toArray(new Predicate[0]));
            }
        };
    }

    @Override
    public Page<OrderResponseDTO> getOrdersByUser(Long userId, Pageable pageable) {
        log.debug("Fetching orders for user ID: {}", userId);

        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return orderRepository.findByUser(userEntity, pageable)
                .map(orderMapper::toResponse);
    }

    @Override
    public Page<OrderResponseDTO> getOrdersByStatus(OrderStatusEnum status, Pageable pageable) {
        log.debug("Fetching orders by status: {}", status);
        return orderRepository.findByOrderStatus(status, pageable)
                .map(orderMapper::toResponse);
    }

    @Override
    @Transactional
    @CacheEvict(value = "orders", key = "#id")
    public OrderResponseDTO updateOrderStatus(Long id, OrderStatusEnum status) {
        log.info("Updating order ID: {} status to: {}", id, status);

        OrderEntity orderEntity = orderRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        orderEntity.setOrderStatus(status);
        orderEntity = orderRepository.save(orderEntity);

        // Send order status update email asynchronously
        try {
            emailService.sendOrderStatusUpdateEmail(orderEntity);
        } catch (Exception e) {
            log.error("Failed to send order status update email for order {}: {}", 
                    orderEntity.getOrderNumber(), e.getMessage());
        }

        log.info("Order status updated successfully: {}", id);
        return orderMapper.toResponse(orderEntity);
    }

    @Override
    @Transactional
    @CacheEvict(value = "orders", key = "#id")
    public OrderResponseDTO confirmOrder(Long id) {
        log.info("Confirming order ID: {}", id);

        OrderEntity orderEntity = orderRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (!orderEntity.getOrderStatus().equals(com.mailshop_dragonvu.enums.OrderStatusEnum.PENDING)) {
            throw new BusinessException(ErrorCode.INVALID_ORDER_STATUS);
        }

        orderEntity.setOrderStatus(com.mailshop_dragonvu.enums.OrderStatusEnum.CONFIRMED);
        orderEntity = orderRepository.save(orderEntity);

        // Send order status update email asynchronously
        try {
            emailService.sendOrderStatusUpdateEmail(orderEntity);
        } catch (Exception e) {
            log.error("Failed to send order status update email for order {}: {}", 
                    orderEntity.getOrderNumber(), e.getMessage());
        }

        log.info("Order confirmed successfully: {}", id);
        return orderMapper.toResponse(orderEntity);
    }

    @Override
    @Transactional
    @CacheEvict(value = "orders", key = "#id")
    public OrderResponseDTO cancelOrder(Long id, String reason, Long userId) {
        log.info("Cancelling order ID: {} by user ID: {}", id, userId);

        OrderEntity orderEntity = orderRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        // Check if user owns the order
        if (!orderEntity.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED);
        }

        if (orderEntity.getOrderStatus().equals(com.mailshop_dragonvu.enums.OrderStatusEnum.CANCELLED)) {
            throw new BusinessException(ErrorCode.ORDER_ALREADY_CANCELLED);
        }


        orderEntity.setOrderStatus(com.mailshop_dragonvu.enums.OrderStatusEnum.CANCELLED);
        orderEntity.setCancelledDate(LocalDateTime.now());
        orderEntity.setCancellationReason(reason);
        orderEntity = orderRepository.save(orderEntity);

        log.info("Order cancelled successfully: {}", id);
        return orderMapper.toResponse(orderEntity);
    }

    @Override
    @Transactional
    @CacheEvict(value = "orders", key = "#id")
    public void deleteOrder(Long id) {
        log.info("Deleting order with ID: {}", id);

        OrderEntity orderEntity = orderRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        orderEntity.setOrderStatus(com.mailshop_dragonvu.enums.OrderStatusEnum.DELETED);
        orderRepository.save(orderEntity);

        log.info("Order deleted successfully with ID: {}", id);
    }

    private String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

}
