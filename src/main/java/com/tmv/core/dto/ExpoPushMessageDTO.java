package com.tmv.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpoPushMessageDTO {
    private String to;
    private String title;
    private String body;
    private Map<String, String> data;
    private String sound; // "default"
    private String priority; // "high", "normal", "default"
    private Integer ttl; // Time to live in seconds
}
