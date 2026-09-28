from typing import Optional, List, Dict, Any, Literal
from pydantic import BaseModel, Field, field_validator

class ChatRequest(BaseModel):
    chat_id: Optional[str] = None
    message: str = Field(..., min_length=1, max_length=2000)
    language: Literal["id", "en"] = "id"
    style: Optional[Literal["scalping", "day", "swing"]] = "day"
    risk_percent: Optional[float] = 1.5

class SignalCard(BaseModel):
    pair: str
    timeframe: str = "H1"
    bias: Literal["BUY", "SELL", "NEUTRAL"]
    entry: Optional[float] = None
    stop_loss: Optional[float] = None
    tp1: Optional[float] = None
    tp2: Optional[float] = None
    rr: List[float] = Field(default_factory=lambda: [1.5, 2.0])
    support: List[float] = Field(default_factory=list)
    resistance: List[float] = Field(default_factory=list)
    indicators: Dict[str, Any] = Field(default_factory=dict)
    confidence: Literal["low", "medium", "high", "rendah", "sedang", "tinggi"] = "medium"
    generated_at: str = ""

    @field_validator("stop_loss")
    def validate_price_order(cls, v, info):
        values = info.data
        bias = values.get("bias")
        entry = values.get("entry")
        tp1 = values.get("tp1")
        tp2 = values.get("tp2")

        if bias == "BUY" and entry is not None and v is not None and tp1 is not None and tp2 is not None:
            if not (v < entry < tp1 < tp2):
                raise ValueError("BUY setup must satisfy: stop_loss < entry < tp1 < tp2")
        elif bias == "SELL" and entry is not None and v is not None and tp1 is not None and tp2 is not None:
            if not (v > entry > tp1 > tp2):
                raise ValueError("SELL setup must satisfy: stop_loss > entry > tp1 > tp2")
        return v

class CandleModel(BaseModel):
    t: int
    o: float
    h: float
    l: float
    c: float

class OHLCResponse(BaseModel):
    symbol: str
    timeframe: str
    candles: List[CandleModel]
