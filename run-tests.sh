IMAGE_NAME=nbank-tests
TEST_PROFILE=${1:-api}
TIMESTAMP=$(date +"%Y%m%d_%H%M")
TEST_OUTPUT_DIR=./test-output/$TIMESTAMP

echo "Сборка запущена"
docker build -t $IMAGE_NAME .

mkdir -p "$TEST_OUTPUT_DIR/logs"
mkdir -p "$TEST_OUTPUT_DIR/results"
mkdir -p "$TEST_OUTPUT_DIR/report"

echo "Тесты запущены"
docker run --rm \
-v "$TEST_OUTPUT_DIR/logs":/app/logs \
-v "$TEST_OUTPUT_DIR/results":/app/target/surefire-reports \
-v "$TEST_OUTPUT_DIR/report":/app/target/site \
-e TEST_PROFILE="$TEST_PROFILE" \
-e APIBASEURL=http://localhost:4111 \
-e UIBASEURL=http://172.31.80.1:3000 \
$IMAGE_NAME

echo "Тесты завершены"
echo "Отчёт: $TEST_OUTPUT_DIR/report/surefire-report.html"
echo "Логи:  $TEST_OUTPUT_DIR/logs/run.log"