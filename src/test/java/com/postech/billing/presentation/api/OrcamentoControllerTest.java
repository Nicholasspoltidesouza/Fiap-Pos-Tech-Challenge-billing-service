package com.postech.billing.presentation.api;

import com.postech.billing.application.dto.OrcamentoResponse;
import com.postech.billing.application.service.OrcamentoService;
import com.postech.billing.domain.model.StatusOrcamento;
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
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class OrcamentoControllerTest {

    @Mock
    private OrcamentoService orcamentoService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new OrcamentoController(orcamentoService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldGetBudgetById() throws Exception {
        when(orcamentoService.findById("orc-1")).thenReturn(new OrcamentoResponse(
                "orc-1", "os-1", "cli-1", BigDecimal.TEN, List.of(), StatusOrcamento.ENVIADO, Instant.now()
        ));

        mockMvc.perform(get("/api/orcamentos/orc-1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("orc-1"));
    }

    @Test
    void shouldGetBudgetsByOrdem() throws Exception {
        when(orcamentoService.findByOrdemServicoId("os-1")).thenReturn(List.of());

        mockMvc.perform(get("/api/orcamentos/por-ordem/os-1"))
                .andExpect(status().isOk());
    }
}
