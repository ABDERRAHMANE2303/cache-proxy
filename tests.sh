#!/usr/bin/env bash
# Fires the listed GET requests at the proxy and prints: status  time  path
#
# Usage:
#   bash script.sh                 # one round, sequential
#   bash script.sh 3               # three rounds (2nd and 3rd round should be cache hits later)
#   bash script.sh 1 --parallel    # all requests of a round at once

BASE="http://localhost:8081"
ROUNDS="${1:-1}"
MODE="${2:-}"

URLS=(
  # ### Lists, no query
  "/proxy/api/payments"
  "/proxy/api/merchants"
  "/proxy/api/customers"

  # ### Single filter
  "/proxy/api/payments?status=SUCCESS"
  "/proxy/api/payments?status=FAILED"
  "/proxy/api/payments?method=CARD"
  "/proxy/api/merchants?status=ACTIVE"
  "/proxy/api/merchants?category=RESTAURANT"
  "/proxy/api/customers?status=INACTIVE"

  # ### Multiple params
  "/proxy/api/payments?status=SUCCESS&method=CARD"
  "/proxy/api/payments?method=CARD&status=SUCCESS"
  "/proxy/api/payments?minAmount=100&maxAmount=500"
  "/proxy/api/payments?status=SUCCESS&sortBy=createdAt&direction=asc"
  # "/proxy/api/payments?customerId=<customer-id>&merchantId=<merchant-id>"

  # ### By id and nested path (needs real ids)
  # "/proxy/api/payments/<payment-id>"
  # "/proxy/api/payments/<payment-id>/transactions"
  # "/proxy/api/merchants/<merchant-id>"
  # "/proxy/api/customers/<customer-id>"

  # ### Edge cases
  # "/proxy/api/payments?status=success"                      # lowercase enum, likely a 400 upstream
  "/proxy/api/payments?status="
  "/proxy/api/payments?"
  # "/proxy/api/payments/00000000-0000-0000-0000-000000000000" # unknown id, upstream error
  # "/proxy/api/payments?status=SUCCESS&status=FAILED"         # repeated key, may be a 400
  # "/proxy/"
  # "/proxy"
)

hit() {
  local out
  out=$(curl -s -o /dev/null -w "%{http_code}  %{time_total}s" "$BASE$1")
  echo "$out  $1"
}

for ((r = 1; r <= ROUNDS; r++)); do
  echo "--- round $r ---"
  for u in "${URLS[@]}"; do
    if [ "$MODE" = "--parallel" ]; then
      hit "$u" &
    else
      hit "$u"
    fi
  done
  wait
done