#!/bin/bash
set -e

# Configuration
SERVICE_NAME="pipchat-api"
REGION="asia-southeast1"

echo "=== Deploying PipChat API to Google Cloud Run ==="

# Check gcloud authentication
if ! command -v gcloud &> /dev/null; then
    echo "Error: gcloud CLI is not installed or not in PATH."
    echo "Install gcloud SDK from: https://cloud.google.com/sdk/docs/install"
    exit 1
fi

PROJECT_ID=$(gcloud config get-value project 2>/dev/null)
if [ -z "$PROJECT_ID" ]; then
    echo "Please set your Google Cloud project:"
    echo "  gcloud config set project YOUR_PROJECT_ID"
    exit 1
fi

echo "Project: $PROJECT_ID"
echo "Service: $SERVICE_NAME"
echo "Region:  $REGION"

# Enable Cloud Run & Cloud Build APIs if not already enabled
echo "Enabling necessary APIs..."
gcloud services enable run.googleapis.com cloudbuild.googleapis.com

# Deploy directly using source deploy
echo "Building and deploying to Cloud Run..."
gcloud run deploy "$SERVICE_NAME" \
    --source . \
    --region "$REGION" \
    --platform managed \
    --allow-unauthenticated \
    --port 8080 \
    --memory 512Mi \
    --cpu 1 \
    --set-env-vars "ALLOWED_ORIGINS=*"

SERVICE_URL=$(gcloud run services describe "$SERVICE_NAME" --region "$REGION" --format 'value(status.url)')

echo ""
echo "=== Deployment Successful! ==="
echo "PipChat Backend URL: $SERVICE_URL"
echo "Health Check: $SERVICE_URL/health"
