package com.example.demo;

import com.example.demo.assistant.StockAdvisorAssistant;
import com.example.demo.model.StockHoldingDetails;
import com.example.demo.model.StockOrder;
import com.example.demo.service.StockOrderService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
public class StockAdvisorController {

    private final StockAdvisorAssistant assistant;
    private final StockOrderService stockOrderService;

    @GetMapping("/chat")
    public String chat(String userMessage) {
        return assistant.chat(userMessage);
    }

    // 直接获取所有订单记录
    @GetMapping("/api/orders")
    public List<StockOrder> getAllOrders() {
        return stockOrderService.getAllOrders();
    }

    // 直接获取当前持仓
    @GetMapping("/api/portfolio")
    public List<StockHoldingDetails> getPortfolio() {
        return stockOrderService.getStockHoldingDetails();
    }

    // 获取最近的订单记录（最近10条）
    @GetMapping("/api/orders/recent")
    public List<StockOrder> getRecentOrders() {
        return stockOrderService.getAllOrders().stream()
                .sorted((a, b) -> b.createdAt().compareTo(a.createdAt()))
                .limit(10)
                .toList();
    }
}
