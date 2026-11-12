package com.postech.billing.application.service;

import com.postech.billing.application.dto.DomainEvent;
import org.springframework.stereotype.Service;

@Service
public class CompensationService {

    private final OrcamentoService orcamentoService;
    private final PagamentoService pagamentoService;

    public CompensationService(OrcamentoService orcamentoService, PagamentoService pagamentoService) {
        this.orcamentoService = orcamentoService;
        this.pagamentoService = pagamentoService;
    }

    public void compensarOrcamento(DomainEvent event) {
        String orcamentoId = extract(event, "orcamentoId");
        if (orcamentoId != null && !orcamentoId.isBlank()) {
            orcamentoService.cancelar(orcamentoId);
            return;
        }
        if (event.ordemServicoId() != null && !event.ordemServicoId().isBlank()) {
            orcamentoService.cancelarPorOrdemServico(event.ordemServicoId());
        }
    }

    public void compensarPagamento(DomainEvent event) {
        String pagamentoId = extract(event, "pagamentoId");
        if (pagamentoId != null && !pagamentoId.isBlank()) {
            pagamentoService.cancelar(pagamentoId);
            return;
        }
        if (event.ordemServicoId() != null && !event.ordemServicoId().isBlank()) {
            pagamentoService.cancelarPorOrdemServico(event.ordemServicoId());
        }
    }

    private String extract(DomainEvent event, String key) {
        if (event.payload() == null || !event.payload().containsKey(key) || event.payload().get(key) == null) {
            return null;
        }
        return String.valueOf(event.payload().get(key));
    }
}
