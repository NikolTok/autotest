#!/bin/bash

set -e

IMAGE_NAME="${IMAGE_NAME:-nbank-tests:latest}"
PROFILE="${1:-}"

COMPOSE_FILE="${COMPOSE_FILE:-./docker-compose.yml}"
TIMESTAMP=$(date +"%Y%m%d_%H%M")
OUTPUT_DIR="./test-output/$TIMESTAMP"

# ===== ЦВЕТА ДЛЯ КОНСОЛИ =====
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'

log_info()    { echo -e "${BLUE}[INFO]${NC}  $1"; }
log_success() { echo -e "${GREEN}[OK]${NC}    $1"; }
log_warn()    { echo -e "${YELLOW}[WARN]${NC}  $1"; }
log_error()   { echo -e "${RED}[ERROR]${NC} $1"; }

cleanup() {
    local exit_code=$?
    echo ""
    log_info "Останавливаю Docker Compose окружение..."

    docker compose -f "$COMPOSE_FILE" down --remove-orphans || true

    if [ $exit_code -eq 0 ]; then
        log_success "Тесты завершены успешно"
        log_info "Отчёт: $OUTPUT_DIR/report/surefire-report.html"
        log_info "Логи:  $OUTPUT_DIR/logs/run.log"
    else
        log_error "Тесты завершились с ошибкой (exit code: $exit_code)"
        log_info  "Логи: $OUTPUT_DIR/logs/run.log"
    fi

    echo ""
    log_info "Окружение остановлено"

    exit $exit_code
}

trap cleanup EXIT

echo ""
log_info "========================================"
log_info "  ЗАПУСК ТЕСТОВ В DOCKER COMPOSE"
log_info "========================================"
echo ""

log_info "Создаю директории для результатов..."
mkdir -p "$OUTPUT_DIR/logs"
mkdir -p "$OUTPUT_DIR/results"
mkdir -p "$OUTPUT_DIR/report"
log_success "Директории созданы: $OUTPUT_DIR"

if ! docker image inspect "$IMAGE_NAME" > /dev/null 2>&1; then
    log_warn "⚠️  Образ $IMAGE_NAME не найден. Собираю..."
    docker build -t "$IMAGE_NAME" .
    log_success "Образ собран: $IMAGE_NAME"
else
    log_success "Образ найден: $IMAGE_NAME"
fi

echo ""
log_info "Поднимаю Docker Compose окружение..."

docker compose -f "$COMPOSE_FILE" up -d --remove-orphans

log_success "Окружение поднято"
echo ""

log_info "Жду готовности сервисов..."

MAX_WAIT=60
WAITED=0
INTERVAL=2

# Ждём API (порт 4111)
while ! curl -s -o /dev/null -w "%{http_code}" http://localhost:4111 2>/dev/null | grep -qE "^[2-4]"; do
    if [ $WAITED -ge $MAX_WAIT ]; then
        log_error "API не поднялся за ${MAX_WAIT}s"
        exit 1
    fi
    sleep $INTERVAL
    WAITED=$((WAITED + INTERVAL))
    echo -n "."
done
echo ""
log_success "API готов (http://localhost:4111)"

# Ждём UI (порт 3000)
WAITED=0
while ! curl -s -o /dev/null -w "%{http_code}" http://localhost:3000 2>/dev/null | grep -qE "^[2-4]"; do
    if [ $WAITED -ge $MAX_WAIT ]; then
        log_error "UI не поднялся за ${MAX_WAIT}s"
        exit 1
    fi
    sleep $INTERVAL
    WAITED=$((WAITED + INTERVAL))
    echo -n "."
done
echo ""
log_success "UI готов (http://localhost:3000)"

# Ждём Selenoid (порт 4444)
WAITED=0
while ! curl -s -o /dev/null -w "%{http_code}" http://localhost:4444/status 2>/dev/null | grep -qE "^200"; do
    if [ $WAITED -ge $MAX_WAIT ]; then
        log_error "Selenoid не поднялся за ${MAX_WAIT}s"
        exit 1
    fi
    sleep $INTERVAL
    WAITED=$((WAITED + INTERVAL))
    echo -n "."
done
echo ""
log_success "Selenoid готов (http://localhost:4444)"

echo ""
log_info "Запускаю тесты в контейнере..."
echo ""

# Формируем команду для запуска тестов
if [ -n "$PROFILE" ]; then
    log_info "Профиль: $PROFILE"
    TEST_CMD="mvn test -P $PROFILE"
else
    log_info "Профиль: все тесты"
    TEST_CMD="mvn test"
fi

docker run --rm \
    --network nbank-network \
    -v "$(pwd)/$OUTPUT_DIR/logs":/app/logs \
    -v "$(pwd)/$OUTPUT_DIR/results":/app/target/surefire-reports \
    -v "$(pwd)/$OUTPUT_DIR/report":/app/target/site \
    -e APIBASEURL=http://localhost:4111 \
    -e UIBASEURL=http://localhost:3000 \
    -e SELENOID_URL=http://localhost:4444 \
    -e SELENOID_UI_URL=http://localhost:8080 \
    "$IMAGE_NAME" \
    /bin/bash -c "mkdir -p /app/logs && $TEST_CMD 2>&1 | tee /app/logs/run.log ; mvn -DskipTests=true surefire-report:report 2>&1 | tee -a /app/logs/run.log"

TEST_EXIT_CODE=$?

if [ $TEST_EXIT_CODE -ne 0 ]; then
    log_error "Тесты упали (exit code: $TEST_EXIT_CODE)"
    exit $TEST_EXIT_CODE
fi

echo ""
log_success "Все тесты прошли успешно!"
log_info "Отчёт: $OUTPUT_DIR/report/surefire-report.html"
log_info "Логи:  $OUTPUT_DIR/logs/run.log"
