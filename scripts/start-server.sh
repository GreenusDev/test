#!/bin/bash

echo "--------------- 서버 배포 시작 -----------------"

REGION=us-east-1
ACCOUNT=245140949573
REPO=market-server
IMAGE=$ACCOUNT.dkr.ecr.$REGION.amazonaws.com/$REPO:latest

# ECR 로그인
aws ecr get-login-password --region $REGION \
| docker login --username AWS --password-stdin $ACCOUNT.dkr.ecr.$REGION.amazonaws.com

# 기존 컨테이너 제거
docker stop $REPO || true
docker rm $REPO || true

# 이미지 pull
docker pull $IMAGE

# 컨테이너 실행
docker run -d --name $REPO -p 8080:8080 $IMAGE

echo "--------------- 서버 배포 끝 -----------------"