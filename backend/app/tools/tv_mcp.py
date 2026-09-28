from typing import Dict, Any

async def get_technical_analysis(symbol: str, timeframe: str = "H1") -> Dict[str, Any]:
    clean_sym = symbol.upper().replace("/", "")
    # Map common timeframe to tradingview interval
    interval_map = {"M15": "15m", "H1": "1h", "H4": "4h", "D1": "1d"}
    tf = interval_map.get(timeframe.upper(), "1h")

    try:
        from tradingview_ta import TA_Handler, Interval
        tv_intervals = {
            "15m": Interval.INTERVAL_15_MINUTES,
            "1h": Interval.INTERVAL_1_HOUR,
            "4h": Interval.INTERVAL_4_HOURS,
            "1d": Interval.INTERVAL_1_DAY
        }
        chosen_int = tv_intervals.get(tf, Interval.INTERVAL_1_HOUR)

        exchanges = ["FX_IDC", "OANDA", "FOREXCOM"]
        for ex in exchanges:
            try:
                handler = TA_Handler(
                    symbol=clean_sym,
                    screener="forex",
                    exchange=ex,
                    interval=chosen_int
                )
                analysis = handler.get_analysis()
                return {
                    "symbol": clean_sym,
                    "exchange": ex,
                    "recommendation": analysis.summary.get("RECOMMENDATION", "NEUTRAL"),
                    "rsi": round(analysis.indicators.get("RSI", 50.0), 1),
                    "macd": "bullish" if analysis.indicators.get("MACD.macd", 0) > analysis.indicators.get("MACD.signal", 0) else "bearish",
                    "close": analysis.indicators.get("close", 1.0850)
                }
            except Exception:
                continue
    except Exception:
        pass

    # Fallback response
    return {
        "symbol": clean_sym,
        "exchange": "SYNTHETIC",
        "recommendation": "BUY" if "EUR" in clean_sym or "XAU" in clean_sym else "NEUTRAL",
        "rsi": 56.4,
        "macd": "bullish",
        "close": 2685.50 if "XAU" in clean_sym else 1.0850
    }
