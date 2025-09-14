package com.example.demo.service;

import com.example.demo.model.OrderType;
import com.example.demo.model.StockHoldingDetails;
import com.example.demo.model.StockOrder;
import com.example.demo.repository.StockOrderRepository;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class StockOrderService {
    private final StockOrderRepository stockOrderRepository;

    @Tool("Create a stock order to buy or sell stocks with specified price")
    public String createOrder(
            @P("Stock symbol (e.g., AAPL, MSFT)") String symbol,
            @P("Number of shares to buy or sell") Integer quantity,
            @P("Price per share") BigDecimal price,
            @P("Order type: BUY or SELL") String orderType) {

        log.info("Creating order: {} {} shares of {} at ${}", orderType, quantity, symbol, price);

        try {
            OrderType type = OrderType.valueOf(orderType.toUpperCase());

            // Check if the user has enough shares to sell
            if (type == OrderType.SELL) {
                int currentHolding = getCurrentHoldingQuantity(symbol.toUpperCase());
                if (currentHolding < quantity) {
                    return String.format(
                            "Error: Cannot sell %d shares of %s. You only have %d shares in your portfolio.",
                            quantity, symbol, currentHolding);
                }
            }

            StockOrder newOrder = new StockOrder(
                    null,
                    "testuser",
                    symbol.toUpperCase(),
                    quantity,
                    price,
                    type,
                    LocalDateTime.now());

            StockOrder savedOrder = stockOrderRepository.save(newOrder);
            return String.format("Order created successfully! Order ID: %d, %s %d shares of %s at $%s",
                    savedOrder.id(), orderType, quantity, symbol, price);

        } catch (IllegalArgumentException e) {
            return "Error: Invalid order type. Please use 'BUY' or 'SELL'.";
        } catch (Exception e) {
            log.error("Error creating order: {}", e.getMessage());
            return "Error creating order: " + e.getMessage();
        }
    }

    @Tool("Get all stock orders")
    public List<StockOrder> getAllOrders() {
        return stockOrderRepository.findAll();
    }

    @Tool("Get current stock holdings summary")
    public List<StockHoldingDetails> getStockHoldingDetails() {
        return stockOrderRepository.findAll().stream()
                .collect(Collectors.groupingBy(StockOrder::symbol,
                        Collectors.summingInt(
                                order -> order.orderType() == OrderType.BUY ? order.quantity() : -order.quantity())))
                .entrySet().stream()
                .filter(entry -> entry.getValue() > 0) // filter out stocks with 0 or negative quantity
                .map(entry -> new StockHoldingDetails(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    // get the current holding quantity of a stock
    private int getCurrentHoldingQuantity(String symbol) {
        return stockOrderRepository.findAll().stream()
                .filter(order -> order.symbol().equals(symbol))
                .mapToInt(order -> order.orderType() == OrderType.BUY ? order.quantity() : -order.quantity())
                .sum();
    }
}
