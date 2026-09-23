package com.stockly.sales;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stockly.auth.Role;
import com.stockly.auth.UserAccount;
import com.stockly.common.exception.BusinessException;
import com.stockly.product.Product;
import com.stockly.product.ProductRepository;
import com.stockly.sales.dto.SalesOrderLineRequest;
import com.stockly.sales.dto.SalesOrderRequest;
import com.stockly.sales.dto.SalesOrderResponse;
import com.stockly.stock.MovementType;
import com.stockly.stock.StockService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SalesOrderServiceTest {

    @Mock
    private SalesOrderRepository salesOrderRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private StockService stockService;

    @InjectMocks
    private SalesOrderService salesOrderService;

    private UserAccount user;
    private Product product;

    @BeforeEach
    void setUp() {
        user = new UserAccount();
        user.setId(1L);
        user.setEmail("user@stockly.local");
        user.setRole(Role.USER);

        product = new Product();
        product.setId(9L);
        product.setSku("WIDGET");
        product.setUnitPrice(new BigDecimal("12.50"));
        product.setStockQuantity(20);
    }

    @Test
    void createPersistsDraft() {
        when(productRepository.findById(9L)).thenReturn(Optional.of(product));
        when(salesOrderRepository.save(any(SalesOrder.class))).thenAnswer(invocation -> {
            SalesOrder order = invocation.getArgument(0);
            order.setId(42L);
            return order;
        });

        SalesOrderResponse response = salesOrderService.create(
                new SalesOrderRequest(List.of(new SalesOrderLineRequest(9L, 2))),
                user);

        assertThat(response.id()).isEqualTo(42L);
        assertThat(response.status()).isEqualTo(SalesOrderStatus.DRAFT);
        assertThat(response.lines()).hasSize(1);
        verify(stockService, never()).applyAndRecord(any(), any(), any(Integer.class), any(), any(Boolean.class));
    }

    @Test
    void confirmAppliesSaleStrategy() {
        SalesOrder order = draftOrder();
        when(salesOrderRepository.findById(5L)).thenReturn(Optional.of(order));
        when(productRepository.findById(9L)).thenReturn(Optional.of(product));

        SalesOrderResponse response = salesOrderService.confirm(5L);

        assertThat(response.status()).isEqualTo(SalesOrderStatus.CONFIRMED);
        verify(stockService).applyAndRecord(eq(product), eq(MovementType.SALE), eq(3), eq("SO-5"), eq(true));
    }

    @Test
    void confirmRejectedWhenNotDraft() {
        SalesOrder order = draftOrder();
        order.setStatus(SalesOrderStatus.CONFIRMED);
        when(salesOrderRepository.findById(5L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> salesOrderService.confirm(5L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("DRAFT");
    }

    private SalesOrder draftOrder() {
        SalesOrder order = new SalesOrder();
        order.setId(5L);
        order.setCreatedBy(user);
        order.setStatus(SalesOrderStatus.DRAFT);
        SalesOrderLine line = new SalesOrderLine();
        line.setProduct(product);
        line.setQuantity(3);
        line.setUnitPrice(product.getUnitPrice());
        order.addLine(line);
        return order;
    }
}
