from typing import Dict, Any, List
from .ohlc import fetch_ohlc

async def calc_levels(symbol: str, timeframe: str = "H1") -> Dict[str, Any]:
    candles = await fetch_ohlc(symbol, timeframe, limit=30)
    if not candles:
        return {"symbol": symbol, "support": [], "resistance": [], "pivot": 0.0}

    high = max(c["h"] for c in candles)
    low = min(c["l"] for c in candles)
    close = candles[-1]["c"]

    pivot = round((high + low + close) / 3.0, 5)
    r1 = round(2 * pivot - low, 5)
    s1 = round(2 * pivot - high, 5)
    r2 = round(pivot + (high - low), 5)
    s2 = round(pivot - (high - low), 5)

    diff = high - low
    fib382 = round(high - diff * 0.382, 5)
    fib618 = round(high - diff * 0.618, 5)

    supports = sorted(list({s for s in [s1, s2, fib618] if s < close}), reverse=True)[:2]
    resistances = sorted(list({r for r in [r1, r2, fib382] if r > close}))[:2]

    return {
        "symbol": symbol.upper(),
        "pivot": pivot,
        "support": supports if supports else [round(close * 0.995, 5), round(close * 0.990, 5)],
        "resistance": resistances if resistances else [round(close * 1.005, 5), round(close * 1.010, 5)]
    }
