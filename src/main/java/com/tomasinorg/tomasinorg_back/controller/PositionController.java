package com.tomasinorg.tomasinorg_back.controller;

import com.tomasinorg.tomasinorg_back.dto.CreatePositionRequest;
import com.tomasinorg.tomasinorg_back.dto.PositionDto;
import com.tomasinorg.tomasinorg_back.dto.UpdatePositionRequest;
import com.tomasinorg.tomasinorg_back.service.PositionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/orgs")
@RequiredArgsConstructor
@Slf4j
public class PositionController {

    private final PositionService positionService;

    @PostMapping("/{orgId}/positions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PositionDto> createPosition(
            @PathVariable Long orgId,
            @Valid @RequestBody CreatePositionRequest request) {
        try {
            PositionDto position = positionService.createPosition(orgId, request);
            return ResponseEntity.ok(position);
        } catch (RuntimeException e) {
            log.error("Error creating position for organization {}: {}", orgId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{orgId}/positions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PositionDto>> getOrganizationPositions(@PathVariable Long orgId) {
        List<PositionDto> positions = positionService.getOrganizationPositions(orgId);
        return ResponseEntity.ok(positions);
    }

    @GetMapping("/{orgId}/positions/{positionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PositionDto> getPosition(
            @PathVariable Long orgId,
            @PathVariable Long positionId) {
        return positionService.getPositionById(positionId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{orgId}/positions/{positionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PositionDto> updatePosition(
            @PathVariable Long orgId,
            @PathVariable Long positionId,
            @Valid @RequestBody UpdatePositionRequest request) {
        try {
            PositionDto position = positionService.updatePosition(positionId, request);
            return ResponseEntity.ok(position);
        } catch (RuntimeException e) {
            log.error("Error updating position {} for organization {}: {}", positionId, orgId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{orgId}/positions/{positionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deletePosition(
            @PathVariable Long orgId,
            @PathVariable Long positionId) {
        try {
            positionService.deletePosition(positionId);
            return ResponseEntity.ok(Map.of("message", "Position deleted successfully"));
        } catch (RuntimeException e) {
            log.error("Error deleting position {} from organization {}: {}", positionId, orgId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
