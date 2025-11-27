package com.mailshop_dragonvu.service.impl;

import com.mailshop_dragonvu.dto.orders.*;
import com.mailshop_dragonvu.dto.products.ProductResponseDTO;
import com.mailshop_dragonvu.entity.OrderEntity;
import com.mailshop_dragonvu.entity.OrderItemEntity;
import com.mailshop_dragonvu.entity.ProductItemEntity;
import com.mailshop_dragonvu.entity.UserEntity;
import com.mailshop_dragonvu.enums.OrderStatusEnum;
import com.mailshop_dragonvu.exception.BusinessException;
import com.mailshop_dragonvu.exception.ErrorCode;
import com.mailshop_dragonvu.mapper.OrderItemMapper;
import com.mailshop_dragonvu.mapper.OrderMapper;
import com.mailshop_dragonvu.repository.OrderRepository;
import com.mailshop_dragonvu.repository.UserRepository;
import com.mailshop_dragonvu.service.*;
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

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final ProductItemService productItemService;
    private  final ProductService productService;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final EmailService emailService;
    private final WalletService walletService;

    @Override
    @Transactional
    @CacheEvict(value = "orders", allEntries = true)
    public ClientOrderCreateResponseDTO createOrder(OrderCreateDTO request, Long userId) {
        List<String> accountDataList = new ArrayList<>();

        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        ProductResponseDTO productResponseDTO =  productService.getProductById(request.getProductId());

        OrderEntity order = new OrderEntity().builder()
                .user(userEntity)
                .orderNumber(generateOrderNumber())
                .productId(request.getProductId())
                .productName(productResponseDTO.getName())
                .orderStatus(OrderStatusEnum.COMPLETED)
                .build();

        List<ProductItemEntity> productItems = productItemService.getRandomUnsoldItems(request.getProductId(), request.getQuantity());

        for (var productItem : productItems) {
            OrderItemEntity orderItemEntity = new OrderItemEntity().builder()
                    .order(order)
                    .productItem(productItem)
                    .build();
            //Đánh dấu đã bán
            productItem.markSold(userId);
            order.addOrderItem(orderItemEntity);

            accountDataList.add(productItem.getAccountData());
        }

        //Tính toán tiền
        order.calculationTotalAmount();

//        WalletResponse walletResponse = walletService.getUserWallet(userId);
//        if (walletResponse.getBalance() < order.getTotalAmount()) {
//            throw new BusinessException(ErrorCode.INSUFFICIENT_BALANCE);
//        }

         orderRepository.save(order);

        return ClientOrderCreateResponseDTO.builder()
                .accountData(accountDataList)
                .build();
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
    @Transactional
    @CacheEvict(value = "orders", key = "#id")
    public void deleteOrder(Long id) {

        OrderEntity orderEntity = orderRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        orderEntity.setOrderStatus(com.mailshop_dragonvu.enums.OrderStatusEnum.DELETED);
        orderRepository.save(orderEntity);
    }

    private String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

}
