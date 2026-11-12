package com.postech.billing.application.service;

import com.postech.billing.application.dto.DomainEvent;
import com.postech.billing.application.dto.OrcamentoResponse;
import com.postech.billing.application.port.EventPublisherPort;
import com.postech.billing.application.port.OrcamentoRepositoryPort;
import com.postech.billing.domain.exception.BusinessException;
import com.postech.billing.domain.exception.ResourceNotFoundException;
import com.postech.billing.domain.model.Orcamento;
import com.postech.billing.domain.model.OrcamentoItem;
import com.postech.billing.domain.model.StatusOrcamento;
import com.postech.billing.infrastructure.config.BillingRabbitProperties;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrcamentoService {

    private final OrcamentoRepositoryPort orcamentoRepository;
    private final EventPublisherPort eventPublisher;
    private final BillingRabbitProperties rabbitProperties;

    public OrcamentoService(
            OrcamentoRepositoryPort orcamentoRepository,
            EventPublisherPort eventPublisher,
            BillingRabbitProperties rabbitProperties
    ) {
        this.orcamentoRepository = orcamentoRepository;
        this.eventPublisher = eventPublisher;
        this.rabbitProperties = rabbitProperties;
    }

    public Orcamento gerarOrcamento(DomainEvent event) {
        if (event.ordemServicoId() == null || event.ordemServicoId().isBlank()) {
            throw new BusinessException("ordemServicoId is required");
        }

        List<OrcamentoItem> itens = extractItens(event.payload());
        if (itens.isEmpty()) {
            BigDecimal valorEstimado = extractValorEstimado(event.payload());
            itens = List.of(new OrcamentoItem(
                    "Diagnostico e servicos estimados",
                    "SERVICO",
                    1,
                    valorEstimado,
                    valorEstimado
            ));
        }
        Orcamento orcamento = Orcamento.create(event.ordemServicoId(), event.clienteId(), itens);
        orcamento.markAsEnviado();
        Orcamento saved = orcamentoRepository.save(orcamento);

        Map<String, Object> payload = new HashMap<>();
        payload.put("orcamentoId", saved.getId());
        payload.put("valorTotal", saved.getValorTotal());
        payload.put("itens", saved.getItens());
        payload.put("status", saved.getStatus().name());

        DomainEvent published = DomainEvent.of(
                "OrcamentoGerado",
                saved.getOrdemServicoId(),
                saved.getClienteId(),
                payload
        );
        eventPublisher.publish(rabbitProperties.getRoutingKeys().getOrcamentoGerado(), published);
        return saved;
    }

    public OrcamentoResponse findById(String id) {
        return OrcamentoResponse.from(findEntityById(id));
    }

    public List<OrcamentoResponse> findByOrdemServicoId(String ordemServicoId) {
        return orcamentoRepository.findByOrdemServicoId(ordemServicoId).stream()
                .map(OrcamentoResponse::from)
                .toList();
    }

    public Orcamento findEntityById(String id) {
        return orcamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found: " + id));
    }

    public Orcamento save(Orcamento orcamento) {
        return orcamentoRepository.save(orcamento);
    }

    public void cancelar(String orcamentoId) {
        Orcamento orcamento = findEntityById(orcamentoId);
        orcamento.cancelar();
        orcamentoRepository.save(orcamento);
    }

    public void cancelarPorOrdemServico(String ordemServicoId) {
        List<Orcamento> orcamentos = orcamentoRepository.findByOrdemServicoId(ordemServicoId);
        for (Orcamento orcamento : orcamentos) {
            if (orcamento.getStatus() != StatusOrcamento.CANCELADO) {
                orcamento.cancelar();
                orcamentoRepository.save(orcamento);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private List<OrcamentoItem> extractItens(Map<String, Object> payload) {
        if (payload == null || !payload.containsKey("itens")) {
            return List.of();
        }
        Object raw = payload.get("itens");
        if (!(raw instanceof List<?> list)) {
            return List.of();
        }
        List<OrcamentoItem> itens = new ArrayList<>();
        for (Object entry : list) {
            if (entry instanceof Map<?, ?> map) {
                Map<String, Object> itemMap = (Map<String, Object>) map;
                BigDecimal valorUnitario = toBigDecimal(itemMap.get("valorUnitario"));
                Integer quantidade = toInteger(itemMap.get("quantidade"));
                BigDecimal valorTotal = itemMap.containsKey("valorTotal")
                        ? toBigDecimal(itemMap.get("valorTotal"))
                        : valorUnitario.multiply(BigDecimal.valueOf(quantidade));
                itens.add(new OrcamentoItem(
                        stringValue(itemMap.get("descricao")),
                        stringValue(itemMap.get("tipo")),
                        quantidade,
                        valorUnitario,
                        valorTotal
                ));
            }
        }
        return itens;
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private Integer toInteger(Object value) {
        if (value == null) {
            return 1;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        return Integer.parseInt(String.valueOf(value));
    }

    private BigDecimal extractValorEstimado(Map<String, Object> payload) {
        if (payload == null) {
            return new BigDecimal("150.00");
        }
        if (payload.containsKey("valorEstimado")) {
            return toBigDecimal(payload.get("valorEstimado"));
        }
        if (payload.containsKey("valorOrcamento")) {
            return toBigDecimal(payload.get("valorOrcamento"));
        }
        return new BigDecimal("150.00");
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal;
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        return new BigDecimal(String.valueOf(value));
    }
}
