# PipChat Backend — Google Cloud Run Deployment

Backend FastAPI untuk **PipChat** dirancang khusus untuk deployment serverless di **Google Cloud Run (Python 3.12)** sesuai spesifikasi PRD.

---

## 1. Quick Deploy via Google Cloud CLI (`gcloud`)

Dari folder `backend/`:

```bash
# 1. Login ke Google Cloud
gcloud auth login

# 2. Pilih Project Anda
gcloud config set project YOUR_PROJECT_ID

# 3. Jalankan script deploy otomatis
./deploy.sh
```

Atau jalankan perintah deployment langsung satu baris:

```bash
gcloud run deploy pipchat-api \
  --source . \
  --region asia-southeast1 \
  --platform managed \
  --allow-unauthenticated \
  --port 8080 \
  --memory 512Mi \
  --cpu 1 \
  --set-env-vars "ALLOWED_ORIGINS=*,DAILY_LIMIT=20"
```

---

## 2. Deploy via Google Cloud Console (Web GUI)

1. Buka [Google Cloud Run Console](https://console.cloud.google.com/run).
2. Klik **Create Service**.
3. Pilih opsi **Continuously deploy from a repository** (atau pilih **Deploy one revision from an existing container image** / Cloud Build).
4. Konfigurasi Service:
   - **Service name**: `pipchat-api`
   - **Region**: `asia-southeast1` (Jakarta/Singapura)
   - **Authentication**: Pilih **Allow unauthenticated invocations** (agar aplikasi mobile dapat memanggil endpoint chat/ohlc).
   - **Container port**: `8080`
   - **Memory**: `512 MiB`
   - **CPU**: `1 vCPU`
5. Di bagian **Environment variables**, tambahkan:
   - `GEMINI_API_KEY`: *(Diambil dari Google AI Studio)*
   - `GEMINI_MODEL`: `gemini-1.5-flash`
   - `DAILY_LIMIT`: `20`
   - `ALLOWED_ORIGINS`: `*`
6. Klik **Create**. Dalam 1–2 menit, service URL Anda akan aktif (contoh: `https://pipchat-api-xxx-as.a.run.app`).

---

## 3. Endpoints yang Tersedia

| Method | Endpoint | Keterangan |
|---|---|---|
| `GET` | `/health` | Health check service (`{"status":"ok"}`) |
| `POST` | `/chat` | SSE Streaming analisa AI Forex & Trade Plan |
| `GET` | `/ohlc` | Data candlestick OHLC (Yahoo/Forex) |
| `POST` | `/auth/request-otp` | Pengiriman kode verifikasi 6 digit |
| `POST` | `/auth/verify-otp` | Verifikasi kode OTP |
| `POST` | `/auth/google-login` | OAuth handler Google |
| `POST` | `/auth/apple-login` | OAuth handler Sign in with Apple |
| `DELETE` | `/account` | Hapus akun & data privasi pengguna |

---

## 4. Pengujian Service Aktif

```bash
# Health check
curl -X GET https://YOUR_SERVICE_URL/health

# Cek Candlestick OHLC
curl -X GET "https://YOUR_SERVICE_URL/ohlc?symbol=EURUSD&timeframe=H1&limit=50"
```
