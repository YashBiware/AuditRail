#!/usr/bin/env bash
# =============================================================================
# AuditRail - Zero-Setup One-Click Orchestrator (Linux / macOS)
# =============================================================================

set -e

echo "==============================================================================="
echo "           AuditRail Enterprise Tamper-Evident Platform Launcher"
echo "==============================================================================="

if ! command -v docker &> /dev/null; then
    echo " [ERROR] Docker is not installed or not in PATH."
    exit 1
fi

echo " [1/3] Spinning up containerized ecosystem via Docker Compose..."
docker compose up -d --build

echo " [2/3] Waiting for services to become healthy..."
sleep 10

echo " [3/3] Platform is active!"
echo "==============================================================================="
echo "   - Editorial UI Console:     http://localhost:5173"
echo "   - Backend API:              http://localhost:8080"
echo "   - MinIO S3 Object Lock:     http://localhost:9001"
echo "==============================================================================="

# Open browser if on macOS or Linux desktop
if [[ "$OSTYPE" == "darwin"* ]]; then
    open http://localhost:5173
elif which xdg-open > /dev/null; then
    xdg-open http://localhost:5173
fi
