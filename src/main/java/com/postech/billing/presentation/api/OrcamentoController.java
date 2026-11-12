package com.postech.billing.presentation.api;

import com.postech.billing.application.dto.OrcamentoResponse;
import com.postech.billing.application.service.OrcamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orcamentos")
@Tag(name = "Orcamentos")
public class OrcamentoController {

    private final OrcamentoService orcamentoService;

    public OrcamentoController(OrcamentoService orcamentoService) {
        this.orcamentoService = orcamentoService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get budget by id")
    public ResponseEntity<OrcamentoResponse> findById(@PathVariable String id) {
        return ResponseEntity.ok(orcamentoService.findById(id));
    }

    @GetMapping("/por-ordem/{ordemServicoId}")
    @Operation(summary = "List budgets by service order id")
    public ResponseEntity<List<OrcamentoResponse>> findByOrdemServicoId(@PathVariable String ordemServicoId) {
        return ResponseEntity.ok(orcamentoService.findByOrdemServicoId(ordemServicoId));
    }
}
