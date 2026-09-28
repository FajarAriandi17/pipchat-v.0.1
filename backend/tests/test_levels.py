import pytest
import asyncio
from app.tools.levels import calc_levels

@pytest.mark.asyncio
async def test_calc_levels_eurusd():
    levels = await calc_levels("EURUSD", "H1")
    assert "pivot" in levels
    assert "support" in levels
    assert "resistance" in levels
    assert len(levels["support"]) >= 1
    assert len(levels["resistance"]) >= 1
    assert levels["pivot"] > 0

@pytest.mark.asyncio
async def test_calc_levels_xauusd():
    levels = await calc_levels("XAUUSD", "H1")
    assert levels["symbol"] == "XAUUSD"
    assert len(levels["support"]) >= 1
    assert len(levels["resistance"]) >= 1
