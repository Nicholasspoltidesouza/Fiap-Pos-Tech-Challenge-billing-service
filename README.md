# Billing Service — Orçamento e Pagamento

Microsserviço Spring Boot da **Fase 4** do FIAP Tech Challenge. Responsável por gerar orçamentos, integrar pagamentos com **Mercado Pago** e publicar/consumir eventos via **RabbitMQ**, persistindo dados em **MongoDB** (requisito NoSQL).

## Stack

- Java 25 + Spring Boot 4.0.3 (Maven)
- Spring Web, Validation, Data MongoDB, AMQP, Actuator, Prometheus
- springdoc-openapi (Swagger UI)
- JaCoCo (mínimo **80%**), JUnit 5, Mockito, Cucumber BDD
- Docker + Kubernetes + GitHub Actions CI/CD

## Porta

`8082`

## Responsabilidades

1. Gerar e armazenar orçamentos
2. Enviar orçamento para aprovação (evento `orcamento.gerado`)
3. Registrar e verificar pagamentos via Mercado Pago
4. Publicar `pagamento.aprovado` / `pagamento.falhou`
5. Compensação: cancelar orçamento/pagamento em eventos `compensar.*`

## Contratos RabbitMQ

- Exchange topic: `oficina.events`

| Fila | Routing key (bind) |
|------|--------------------|
| `billing.orcamento.gerar` | `orcamento.gerar` |
| `billing.pagamento.solicitar` | `pagamento.solicitar` |
| `billing.compensar.orcamento` | `compensar.orcamento` |
| `billing.compensar.pagamento` | `compensar.pagamento` |

Publica: `orcamento.gerado`, `pagamento.aprovado`, `pagamento.falhou`

Envelope:

```json
{
  "eventId": "uuid",
  "eventType": "...",
  "ocorridoEm": "ISO-8601",
  "ordemServicoId": "uuid",
  "clienteId": "uuid",
  "payload": {}
}
```

## APIs REST

| Método | Path | Descrição |
|--------|------|-----------|
| GET | `/api/orcamentos/{id}` | Consulta orçamento |
| GET | `/api/orcamentos/por-ordem/{ordemServicoId}` | Orçamentos por OS |
| POST | `/api/pagamentos/{orcamentoId}/checkout` | Cria preference/init_point |
| GET | `/api/pagamentos/{id}` | Consulta pagamento |
| POST | `/api/pagamentos/webhook` | Webhook Mercado Pago |

Swagger: `http://localhost:8082/swagger-ui.html`

## Mercado Pago

- Interface: `MercadoPagoGateway`
- Implementação HTTP real + **mock** (`mercado-pago.mock-enabled=true`, padrão local)
- Variáveis: `MERCADO_PAGO_ACCESS_TOKEN`, `MERCADO_PAGO_BASE_URL`

## MongoDB

- Collection `orcamentos`
- Collection `pagamentos`

## Como rodar local

1. Suba MongoDB (`27017`) e RabbitMQ (`5672`)
2. Copie `.env.example` para `.env` (opcional)
3. Execute:

```bash
./mvnw spring-boot:run
```

Com mock do Mercado Pago habilitado (padrão), o checkout retorna URL simulada e o webhook aprova o pagamento.

## Testes

```bash
./mvnw verify
```

Inclui testes unitários e feature Cucumber do fluxo de pagamento (`src/test/resources/features/pagamento.feature`).

## Docker

```bash
docker build -t billing-service:latest .
docker run --rm -p 8082:8082 \
  -e MONGODB_URI=mongodb://host.docker.internal:27017/billing_db \
  -e RABBITMQ_HOST=host.docker.internal \
  -e MERCADO_PAGO_MOCK_ENABLED=true \
  billing-service:latest
```

## Kubernetes

```bash
kubectl apply -f k8s/
```

## Sonar

- Project key: `billing-service`

```bash
./mvnw sonar:sonar
```

## Postman

Importe `postman/Billing-Service.postman_collection.json`.

## Pacote

`com.postech.billing`
