from typing import Dict, Any
from .ohlc import fetch_ohlc
from .levels import calc_levels

def calculate_atr(candles, period: int = 14) -> float:
    if len(candles) < 2:
        return 0.0015
    trs = []
    for i in range(1, len(candles)):
        c = candles[i]
        prev_c = candles[i - 1]["c"]
        tr = max(c["h"] - c["l"], abs(c["h"] - prev_c), abs(c["l"] - prev_c))
        trs.append(tr)
    recent = trs[-period:]
    return sum(recent) / len(recent) if recent else 0.0015

async def build_trade_plan(symbol: str, timeframe: str = "H1", bias: str = "NEUTRAL") -> Dict[str, Any]:
    bias = bias.upper()
    candles = await fetch_ohlc(symbol, timeframe, limit=30)
    levels = await calc_levels(symbol, timeframe)

    if not candles or bias == "NEUTRAL":
        return {
            "symbol": symbol.upper(),
            "timeframe": timeframe.upper(),
            "bias": "NEUTRAL",
            "entry": None,
            "stop_loss": None,
            "tp1": None,
            "tp2": None,
            "rr": [1.5, 2.0],
            "support": levels.get("support", []),
            "resistance": levels.get("resistance", [])
        }

    close = candles[-1]["c"]
    decimals = 3 if "JPY" in symbol.upper() else (2 if "XAU" in symbol.upper() else 5)
    atr = max(calculate_atr(candles), 0.0015)
    sl_dist = atr * 1.5

    if bias == "BUY":
        entry = round(close, decimals)
        sl = round(entry - sl_dist, decimals)
        risk = entry - sl
        tp1 = round(entry + (risk * 1.5), decimals)
        tp2 = round(entry + (risk * 2.0), decimals)
    else:  # SELL
        entry = round(close, decimals)
        sl = round(entry + sl_dist, decimals)
        risk = sl - entry
        tp1 = round(entry - (risk * 1.5), decimals)
        tp2 = round(entry - (risk * 2.0), decimals)

    return {
        "symbol": symbol.upper(),
        "timeframe": timeframe.upper(),
        "bias": bias,
        "entry": entry,
        "stop_loss": sl,
        "tp1": tp1,
        "tp2": tp2,
        "rr": [1.5, 2.0],
        "support": levels.get("support", []),
        "resistance": levels.get("resistance", [])
    }
