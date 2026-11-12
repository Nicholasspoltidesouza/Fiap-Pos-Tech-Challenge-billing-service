package com.postech.billing.presentation.api;

import com.postech.billing.application.dto.CheckoutResponse;
import com.postech.billing.application.dto.PagamentoResponse;
import com.postech.billing.application.service.PagamentoService;
import com.postech.billing.domain.model.StatusPagamento;
import com.postech.billing.presentation.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PagamentoControllerTest {

    @Mock
    private PagamentoService pagamentoService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new PagamentoController(pagamentoService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldGetPayment() throws Exception {
        when(pagamentoService.findById("pay-1")).thenReturn(new PagamentoResponse(
                "pay-1", "orc-1", "os-1", BigDecimal.TEN, StatusPagamento.PENDENTE, "pref", "url", Instant.now()
        ));

        mockMvc.perform(get("/api/pagamentos/pay-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("pay-1"));
    }

    @Test
    void shouldCreateCheckout() throws Exception {
        when(pagamentoService.criarCheckout("orc-1"))
                .thenReturn(new CheckoutResponse("pay-1", "pref-1", "https://init", "PENDENTE"));

        mockMvc.perform(post("/api/pagamentos/orc-1/checkout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.initPoint").value("https://init"));
    }

    @Test
    void shouldAcceptWebhook() throws Exception {
        doNothing().when(pagamentoService).processWebhook(any());

        mockMvc.perform(post("/api/pagamentos/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"payment\",\"data\":{\"id\":\"123\"}}"))
                .andExpect(status().isOk());
    }
}
