package com.toneatti.catalogo.weeklyschedule.infrastructure.web.controller;

import com.toneatti.catalogo.weeklyschedule.application.service.WeeklyScheduleService;
import com.toneatti.catalogo.weeklyschedule.infrastructure.web.dto.WeeklyScheduleResponse;
import com.toneatti.catalogo.weeklyschedule.infrastructure.web.mapper.WeeklyScheduleDtoMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Weekly schedules")
@RestController
@RequestMapping("/weekly-schedules")
@RequiredArgsConstructor
public class WeeklyScheduleController {

    private final WeeklyScheduleService weeklyScheduleService;
    private final WeeklyScheduleDtoMapper weeklyScheduleDtoMapper;

    @Operation(summary = "List the enabled weekly schedules of a professional")
    @GetMapping
    public ResponseEntity<List<WeeklyScheduleResponse>> getWeeklySchedulesByProfessional(@RequestParam Long professionalId) {
        return ResponseEntity.ok(weeklyScheduleService.getWeeklySchedulesByProfessional(professionalId).stream()
                .map(weeklyScheduleDtoMapper::toResponse)
                .toList());
    }
}
