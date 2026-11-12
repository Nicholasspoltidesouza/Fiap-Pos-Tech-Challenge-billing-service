package com.postech.billing.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI billingOpenApi() {
        return new OpenAPI()
                .addServersItem(new Server().url("http://localhost:8082").description("Local"))
                .info(new Info()
                        .title("Billing Service API")
                        .description("""
                                Orçamento e Pagamento (FIAP Fase 4).

                                ## Banco
                                MongoDB collections: orcamentos, pagamentos.

                                ## Exemplos de execução
                                1. Após criar OS no OS Service, consulte GET /api/orcamentos/por-ordem/{ordemServicoId}
                                2. POST /api/pagamentos/{orcamentoId}/checkout — gera preferência Mercado Pago
                                3. POST /api/pagamentos/webhook — simula notificação MP (quando mock auto-approve estiver off)

                                Em local, MERCADO_PAGO_AUTO_APPROVE=true aprova ao receber pagamento.solicitar.
                                """)
                        .version("1.0.0")
                        .contact(new Contact().name("FIAP Pós Tech").url("https://github.com/Nicholasspoltidesouza")));
    }
}
