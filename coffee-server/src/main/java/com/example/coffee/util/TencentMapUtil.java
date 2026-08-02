package com.example.coffee.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Slf4j
@Component
public class TencentMapUtil {

    private static final String GEOCODER_URL = "https://apis.map.qq.com/ws/geocoder/v1/";
    private static final String PLACE_SEARCH_URL = "https://apis.map.qq.com/ws/place/v1/search";
    private static final String DISTANCE_URL = "https://apis.map.qq.com/ws/distance/v1/matrix";

    @Value("${tencent.map.key}")
    private String mapKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public TencentMapUtil() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 逆地址解析：经纬度 -> 地址信息
     */
    public String reverseGeocode(BigDecimal lat, BigDecimal lng) {
        try {
            String url = String.format("%s?location=%s,%s&key=%s",
                    GEOCODER_URL, lat.toPlainString(), lng.toPlainString(), mapKey);
            String resp = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(resp);
            if (root.path("status").asInt() == 0) {
                return root.path("result").path("address").asText();
            }
            log.warn("Tencent geocoder failed: {}", root.path("message").asText());
        } catch (Exception e) {
            log.error("Tencent geocoder error", e);
        }
        return null;
    }

    /**
     * 地点搜索：搜索周边关键词
     */
    public JsonNode placeSearch(String keyword, BigDecimal lat, BigDecimal lng, int radius) {
        try {
            String boundary = String.format("nearby(%s,%s,%d)",
                    lat.toPlainString(), lng.toPlainString(), radius);
            String url = String.format("%s?keyword=%s&boundary=%s&key=%s&page_size=20",
                    PLACE_SEARCH_URL, keyword, boundary, mapKey);
            String resp = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(resp);
            if (root.path("status").asInt() == 0) {
                return root.path("data");
            }
            log.warn("Tencent place search failed: {}", root.path("message").asText());
        } catch (Exception e) {
            log.error("Tencent place search error", e);
        }
        return null;
    }

    /**
     * 步行距离计算：两点间的步行距离（米）
     */
    public Double walkingDistance(BigDecimal fromLat, BigDecimal fromLng,
                                   BigDecimal toLat, BigDecimal toLng) {
        try {
            String from = String.format("%s,%s", fromLat.toPlainString(), fromLng.toPlainString());
            String to = String.format("%s,%s", toLat.toPlainString(), toLng.toPlainString());
            String url = String.format("%s?mode=walking&from=%s&to=%s&key=%s",
                    DISTANCE_URL, from, to, mapKey);
            String resp = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(resp);
            if (root.path("status").asInt() == 0) {
                JsonNode elements = root.path("result").path("rows").path(0).path("elements");
                if (elements.isArray() && elements.size() > 0) {
                    return elements.path(0).path("distance").asDouble();
                }
            }
            log.warn("Tencent distance matrix failed: {}", root.path("message").asText());
        } catch (Exception e) {
            log.error("Tencent distance matrix error", e);
        }
        return null;
    }

    /**
     * 验证地图Key是否可用
     */
    public boolean validateKey() {
        try {
            String url = String.format("%s?location=22.5483,113.9434&key=%s", GEOCODER_URL, mapKey);
            String resp = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(resp);
            return root.path("status").asInt() == 0;
        } catch (Exception e) {
            log.error("Tencent map key validation failed", e);
            return false;
        }
    }
}
