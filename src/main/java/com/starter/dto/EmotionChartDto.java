package com.starter.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class EmotionChartDto {
    private String emotionLabel;
    private List<Integer> emotionData;
    private String borderColor;
    private String backgroundColor;
}
