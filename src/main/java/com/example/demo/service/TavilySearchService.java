package com.example.demo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;

@Service
@AllArgsConstructor
@Slf4j
public class TavilySearchService {

  private final RestClient tavilyRestClient;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Tool("Search for real-time information about stocks, companies, financial news, and market data")
  public String searchStockInformation(
      @P("Search query about stocks, companies, or financial information") String query) {
    log.info("Searching for stock information: {}", query);

    try {
      Map<String, Object> requestBody = new HashMap<>();
      requestBody.put("query", query);
      requestBody.put("search_depth", "basic");
      requestBody.put("include_answer", true);
      requestBody.put("include_images", false);
      requestBody.put("include_raw_content", false);
      requestBody.put("max_results", 5);
      requestBody.put("topic", "finance");

      String response = tavilyRestClient.post()
          .uri("/search")
          .body(requestBody)
          .retrieve()
          .body(String.class);

      return formatSearchResults(response);

    } catch (Exception e) {
      log.error("Error searching for stock information: {}", e.getMessage());
      return "Sorry, I couldn't retrieve the requested information at the moment. Please try again later.";
    }
  }

  @Tool("Get stock price and market data")
  public String getStockPriceData(@P("Stock symbol") String stockSymbol) {
    log.info("Getting stock price data for: {}", stockSymbol);

    String query = stockSymbol + " current stock price market data trading volume";
    return searchStockInformation(query);
  }

  private String formatSearchResults(String jsonResponse) {
    try {
      JsonNode root = objectMapper.readTree(jsonResponse);
      StringBuilder result = new StringBuilder();

      // Add answer if available
      if (root.has("answer")) {
        result.append("Answer: ").append(root.get("answer").asText()).append("\n\n");
      }

      // Add search results
      if (root.has("results") && root.get("results").isArray()) {
        result.append("Search Results:\n");
        int count = 1;
        for (JsonNode resultNode : root.get("results")) {
          result.append(count).append(". ").append(resultNode.get("title").asText()).append("\n");
          result.append("   ").append(resultNode.get("url").asText()).append("\n");
          if (resultNode.has("content")) {
            String content = resultNode.get("content").asText();
            // Limit content length
            if (content.length() > 200) {
              content = content.substring(0, 200) + "...";
            }
            result.append("   ").append(content).append("\n");
          }
          result.append("\n");
          count++;
        }
      }

      return result.toString();

    } catch (Exception e) {
      log.error("Error formatting search results: {}", e.getMessage());
      return "Search completed but couldn't format results properly.";
    }
  }
}
