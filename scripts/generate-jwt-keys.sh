#!/usr/bin/env bash
set -euo pipefail

OUT_DIR="${1:-./docker/jwt-keys}"
mkdir -p "$OUT_DIR"

PRIVATE_KEY="$OUT_DIR/private.pem"
PUBLIC_KEY="$OUT_DIR/public.pem"

openssl genpkey -algorithm RSA -out "$PRIVATE_KEY" -pkeyopt rsa_keygen_bits:2048
openssl rsa -pubout -in "$PRIVATE_KEY" -out "$PUBLIC_KEY"

echo "Generated JWT keys:"
echo "  Private: $PRIVATE_KEY"
echo "  Public:  $PUBLIC_KEY"
echo
echo "Set in .env (single-line PEM, escape newlines or use a secrets manager):"
echo "  RESOS_JWT_PRIVATE_KEY=<contents of private.pem>"
echo "  RESOS_JWT_PUBLIC_KEY=<contents of public.pem>"
