package com.postech.billing.presentation.api;

import com.postech.billing.application.dto.CheckoutResponse;
import com.postech.billing.application.dto.PagamentoResponse;
import com.postech.billing.application.dto.WebhookNotificationRequest;
import com.postech.billing.application.service.PagamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pagamentos")
@Tag(name = "Pagamentos", description = "Mercado Pago checkout and payment verification (MongoDB)")
public class PagamentoController {

    private final PagamentoService pagamentoService;

    public PagamentoController(PagamentoService pagamentoService) {
        this.pagamentoService = pagamentoService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get payment by id", description = "Reads MongoDB collection pagamentos")
    public ResponseEntity<PagamentoResponse> findById(@PathVariable String id) {
        return ResponseEntity.ok(pagamentoService.findById(id));
    }

    @PostMapping("/{orcamentoId}/checkout")
    @Operation(summary = "Create Mercado Pago checkout for a budget")
    public ResponseEntity<CheckoutResponse> checkout(@PathVariable String orcamentoId) {
        return ResponseEntity.ok(pagamentoService.criarCheckout(orcamentoId));
    }

    @PostMapping("/webhook")
    @Operation(
            summary = "Mercado Pago payment webhook",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(examples = {
                            @ExampleObject(
                                    name = "PagamentoAprovado",
                                    summary = "Notificação MP aprovada",
                                    value = """
                                            {
                                              "action": "payment.updated",
                                              "type": "payment",
                                              "data": { "id": "mock-pref-123" }
                                            }
                                            """),
                            @ExampleObject(
                                    name = "PagamentoRecusado",
                                    summary = "Notificação MP recusada",
                                    value = """
                                            {
                                              "action": "payment.updated",
                                              "type": "payment",
                                              "data": { "id": "mock-rejected-456" }
                                            }
                                            """)
                    })))
    public ResponseEntity<Void> webhook(@RequestBody WebhookNotificationRequest request) {
        pagamentoService.processWebhook(request);
        return ResponseEntity.ok().build();
    }
}
