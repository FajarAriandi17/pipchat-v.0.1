import pytest
from app.tools.trade_plan import build_trade_plan
from app.models import SignalCard

@pytest.mark.asyncio
async def test_build_trade_plan_buy():
    plan = await build_trade_plan("EURUSD", "H1", bias="BUY")
    assert plan["bias"] == "BUY"
    assert plan["entry"] is not None
    assert plan["stop_loss"] is not None
    assert plan["tp1"] is not None
    assert plan["tp2"] is not None
    # Check BUY order
    assert plan["stop_loss"] < plan["entry"] < plan["tp1"] < plan["tp2"]

    # Verify SignalCard validation passes
    card = SignalCard(**plan)
    assert card.bias == "BUY"

@pytest.mark.asyncio
async def test_build_trade_plan_sell():
    plan = await build_trade_plan("EURUSD", "H1", bias="SELL")
    assert plan["bias"] == "SELL"
    assert plan["entry"] is not None
    assert plan["stop_loss"] is not None
    assert plan["tp1"] is not None
    assert plan["tp2"] is not None
    # Check SELL order
    assert plan["stop_loss"] > plan["entry"] > plan["tp1"] > plan["tp2"]

    # Verify SignalCard validation passes
    card = SignalCard(**plan)
    assert card.bias == "SELL"
