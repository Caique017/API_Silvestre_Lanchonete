#!/bin/bash

# =============================================================================
# LocalStack init script
# Executado automaticamente quando o LocalStack termina de subir
# Cria todos os recursos AWS necessários para desenvolvimento local
# =============================================================================

echo "⚙️  Iniciando criação dos recursos AWS locais..."

# URL base do LocalStack
ENDPOINT="http://localhost:4566"

# ---------------------------------------------------------------------------
# SQS — Fila de pedidos
# Usada pelo order-service para publicar eventos de pedido criado
# ---------------------------------------------------------------------------

echo "📬 Criando fila SQS: order-created"
aws --endpoint-url=$ENDPOINT sqs create-queue \
    --queue-name order-created \
    --region us-east-1

# ---------------------------------------------------------------------------
# S3 — Bucket de imagens dos produtos
# Usado pelo product-service para upload de imagens
# ---------------------------------------------------------------------------

echo "🪣 Criando bucket S3: silvestre-produtos"
aws --endpoint-url=$ENDPOINT s3 mb \
    s3://silvestre-produtos \
    --region us-east-1

# Configura CORS no bucket para permitir uploads do frontend
aws --endpoint-url=$ENDPOINT s3api put-bucket-cors \
    --bucket silvestre-produtos \
    --cors-configuration '{
        "CORSRules": [{
            "AllowedOrigins": ["*"],
            "AllowedMethods": ["GET", "PUT", "POST"],
            "AllowedHeaders": ["*"]
        }]
    }'

# ---------------------------------------------------------------------------
# Verificação final
# ---------------------------------------------------------------------------

echo ""
echo "✅ Recursos criados com sucesso!"
echo ""
echo "📋 Filas SQS disponíveis:"
aws --endpoint-url=$ENDPOINT sqs list-queues --region us-east-1

echo ""
echo "📋 Buckets S3 disponíveis:"
aws --endpoint-url=$ENDPOINT s3 ls