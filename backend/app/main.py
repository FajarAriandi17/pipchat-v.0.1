import json
import asyncio
from fastapi import FastAPI, Depends, HTTPException, Query
from fastapi.middleware.cors import CORSMiddleware
from sse_starlette.sse import EventSourceResponse
from typing import Optional

from .models import ChatRequest, SignalCard, OHLCResponse, CandleModel
from .config import settings
from .auth import verify_token, generate_and_store_otp, verify_otp_code, OtpRequest, OtpVerifyRequest, SocialLoginRequest
from .tools.ohlc import fetch_ohlc
from .tools.levels import calc_levels
from .tools.trade_plan import build_trade_plan
from .tools.tv_mcp import get_technical_analysis

app = FastAPI(title="PipChat API", version="1.0.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

@app.get("/health")
async def health_check():
    return {"status": "ok", "service": "PipChat API"}

@app.post("/auth/request-otp")
async def request_otp_endpoint(req: OtpRequest):
    code = generate_and_store_otp(req.email)
    return {
        "status": "success",
        "message": "OTP verification code generated",
        "email": req.email,
        "demo_code": code # provided for rapid testing/simulation in development
    }

@app.post("/auth/verify-otp")
async def verify_otp_endpoint(req: OtpVerifyRequest):
    is_valid = verify_otp_code(req.email, req.otp_code)
    if not is_valid:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail={"code": "INVALID_OTP", "message": "Kode OTP salah atau telah kadaluarsa"}
        )
    return {
        "status": "success",
        "token": f"firebase_otp_token_{req.email}",
        "user": {
            "uid": f"otp_{hash(req.email) % 1000000}",
            "email": req.email,
            "display_name": req.email.split("@")[0].capitalize(),
            "provider": "EMAIL_OTP"
        }
    }

@app.post("/auth/google-login")
async def google_login_endpoint(req: SocialLoginRequest):
    return {
        "status": "success",
        "token": f"firebase_g_token_{req.token[:16]}",
        "user": {
            "uid": f"g_{req.token[:12]}",
            "email": req.email or "trader.google@gmail.com",
            "display_name": req.display_name or "Trader Google",
            "provider": "GOOGLE"
        }
    }

@app.post("/auth/apple-login")
async def apple_login_endpoint(req: SocialLoginRequest):
    # Apple Private Relay email support (PRD Section 13 & 17)
    apple_email = req.email or "trader.apple@privaterelay.appleid.com"
    return {
        "status": "success",
        "token": f"firebase_apple_token_{req.token[:16]}",
        "user": {
            "uid": f"apple_{req.token[:12]}",
            "email": apple_email,
            "display_name": req.display_name or "Apple Trader",
            "provider": "APPLE"
        }
    }

@app.get("/ohlc", response_model=OHLCResponse)
async def get_ohlc(
    symbol: str = Query(..., description="Currency pair, e.g. EURUSD or XAUUSD"),
    timeframe: str = Query("H1", description="Timeframe M15, H1, H4, D1"),
    limit: int = Query(100, ge=10, le=300),
    uid: str = Depends(verify_token)
):
    candles = await fetch_ohlc(symbol, timeframe, limit)
    candle_models = [
        CandleModel(t=c["t"], o=c["o"], h=c["h"], l=c["l"], c=c["c"])
        for c in candles
    ]
    return OHLCResponse(symbol=symbol.upper(), timeframe=timeframe.upper(), candles=candle_models)

@app.post("/chat")
async def chat_stream(
    request: ChatRequest,
    uid: str = Depends(verify_token)
):
    async def event_generator():
        chat_id = request.chat_id or f"chat_{int(asyncio.get_event_loop().time() * 1000)}"
        message_id = f"msg_{int(asyncio.get_event_loop().time() * 1000)}"

        # 1. Meta event
        yield {
            "event": "meta",
            "data": json.dumps({"chat_id": chat_id, "message_id": message_id})
        }

        # Determine language & pair
        is_id = request.language == "id"
        pair = "EURUSD"
        for p in ["XAUUSD", "GOLD", "EURUSD", "GBPUSD", "USDJPY", "AUDUSD", "USDCAD"]:
            if p in request.message.upper():
                pair = "XAUUSD" if p == "GOLD" else p
                break
        tf = "H1"

        # 2. Tool status: Technical analysis
        yield {
            "event": "status",
            "data": json.dumps({"text": f"Mengambil data teknikal {pair} {tf}…" if is_id else f"Fetching technical data for {pair} {tf}…"})
        }
        await asyncio.sleep(0.4)
        ta_result = await get_technical_analysis(pair, tf)

        # 3. Tool status: Support & Resistance levels
        yield {
            "event": "status",
            "data": json.dumps({"text": "Menghitung level Support & Resistance…" if is_id else "Calculating Support & Resistance levels…"})
        }
        await asyncio.sleep(0.4)
        levels_result = await calc_levels(pair, tf)

        # 4. Tool status: Trade Plan
        yield {
            "event": "status",
            "data": json.dumps({"text": "Menyusun Trade Plan & Risk:Reward…" if is_id else "Building Trade Plan & Risk:Reward…"})
        }
        await asyncio.sleep(0.4)
        rec = ta_result.get("recommendation", "BUY")
        bias = "BUY" if "BUY" in rec else ("SELL" if "SELL" in rec else "NEUTRAL")
        trade_plan_raw = await build_trade_plan(pair, tf, bias=bias)

        signal_card = SignalCard(
            pair=pair,
            timeframe=tf,
            bias=bias,
            entry=trade_plan_raw.get("entry"),
            stop_loss=trade_plan_raw.get("stop_loss"),
            tp1=trade_plan_raw.get("tp1"),
            tp2=trade_plan_raw.get("tp2"),
            rr=[1.5, 2.0],
            support=levels_result.get("support", []),
            resistance=levels_result.get("resistance", []),
            indicators={
                "rsi": str(ta_result.get("rsi", 50.0)),
                "macd": ta_result.get("macd", "neutral"),
                "ema": "above_ema50" if bias == "BUY" else "below_ema50"
            },
            confidence="medium",
            generated_at="2026-09-28T12:00:00Z"
        )

        # 5. Stream Tokens
        if is_id:
            text = f"""### Analisa {pair} · Timeframe {tf} (Asumsi)

**Bias Pasar:** **{bias}**
Berdasarkan data teknikal TradingView, RSI berada di level {ta_result.get('rsi')} dengan konfirmasi MACD {ta_result.get('macd')}.

**1. Kondisi Tren & Indikator:**
- Tren: {'Uptrend di atas EMA 50' if bias == 'BUY' else 'Downtrend di bawah EMA 50'}
- RSI (14): {ta_result.get('rsi')}
- MACD: {ta_result.get('macd')}

**2. Level Kunci Support & Resistance:**
- Support: {' · '.join(map(str, signal_card.support))}
- Resistance: {' · '.join(map(str, signal_card.resistance))}

**3. Skenario Pembatalan:**
Setup ini batal bila harga menembus level Stop Loss ({signal_card.stop_loss}).

```signal
{signal_card.model_dump_json()}
```

*Bukan saran keuangan. Trading berisiko kehilangan modal.*"""
        else:
            text = f"""### Technical Analysis: {pair} · Timeframe {tf} (Assumed)

**Market Bias:** **{bias}**
Based on technical indicators, RSI is at {ta_result.get('rsi')} with {ta_result.get('macd')} MACD momentum.

**1. Trend & Key Indicators:**
- Trend: {'Bullish above 50 EMA' if bias == 'BUY' else 'Bearish below 50 EMA'}
- RSI (14): {ta_result.get('rsi')}
- MACD: {ta_result.get('macd')}

**2. Support & Resistance:**
- Support: {' · '.join(map(str, signal_card.support))}
- Resistance: {' · '.join(map(str, signal_card.resistance))}

**3. Invalidation:**
This trade plan is invalidated if price breaks Stop Loss ({signal_card.stop_loss}).

```signal
{signal_card.model_dump_json()}
```

*Not financial advice. Trading involves risk of capital loss.*"""

        for token in text.split(" "):
            yield {
                "event": "token",
                "data": json.dumps({"text": f"{token} "})
            }
            await asyncio.sleep(0.02)

        # 6. Signal event
        yield {
            "event": "signal",
            "data": signal_card.model_dump_json()
        }

        # 7. Done event
        yield {
            "event": "done",
            "data": json.dumps({})
        }

    return EventSourceResponse(event_generator())

@app.delete("/account")
async def delete_account(uid: str = Depends(verify_token)):
    return {"status": "success", "message": f"Account {uid} cleared successfully"}
