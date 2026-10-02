#!/bin/bash

set -e

if [ -f ".env" ]; then
    echo "Загружаю переменные из .env..."
    set -a
    source .env
    set +a
fi

IMAGE_NAME="nbank-tests"
DOCKERHUB_USERNAME="${DOCKERHUB_USERNAME:-nikoltok}"
TAG="${1:-latest}"

if [ -z "${DOCKERHUB_TOKEN}" ]; then
    echo "Ошибка: переменная DOCKERHUB_TOKEN не задана."
    echo ""
    echo "Варианты:"
    echo "  1. Создайте .env файл в корне проекта:"
    echo "     DOCKERHUB_USERNAME=nikoltok"
    echo "     DOCKERHUB_TOKEN=ваш-токен"
    echo ""
    echo "  2. Или установите через export:"
    echo "     export DOCKERHUB_TOKEN=\"ваш-токен\""
    exit 1
fi

if ! docker image inspect "${IMAGE_NAME}:latest" > /dev/null 2>&1; then
    echo "Локальный образ ${IMAGE_NAME}:latest не найден."
    echo "Собираю образ..."
    docker build -t "${IMAGE_NAME}" .
fi

echo ""
echo "Логин в Docker Hub как ${DOCKERHUB_USERNAME}..."
echo "${DOCKERHUB_TOKEN}" | docker login --username "${DOCKERHUB_USERNAME}" --password-stdin

FULL_IMAGE="${DOCKERHUB_USERNAME}/${IMAGE_NAME}:${TAG}"
echo ""
echo "Тегирую образ: ${IMAGE_NAME}:latest → ${FULL_IMAGE}"
docker tag "${IMAGE_NAME}:latest" "${FULL_IMAGE}"

echo ""
echo "Пуш образа ${FULL_IMAGE}..."
docker push "${FULL_IMAGE}"

echo ""
echo "Образ успешно запушен в Docker Hub!"
echo ""
echo "Скачать образ:"
echo "docker pull ${FULL_IMAGE}"
echo ""
echo "Репозиторий:"
echo "https://hub.docker.com/r/${DOCKERHUB_USERNAME}/${IMAGE_NAME}"
echo ""