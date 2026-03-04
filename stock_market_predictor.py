#!/usr/bin/env python3
"""
Stock Market Predictor
----------------------
Fetches real-time stock data and uses technical indicators + a Random Forest
classifier to generate directional signals (BUY / SELL / HOLD).

DISCLAIMER: This tool is for educational purposes only. Market predictions are
probabilistic, not guaranteed. Do NOT use this as sole financial advice.

Usage:
    python stock_market_predictor.py              # interactive menu
    python stock_market_predictor.py AAPL         # analyse a single ticker
    python stock_market_predictor.py AAPL MSFT TSLA  # analyse multiple tickers
"""

import sys
import warnings
from datetime import datetime, timedelta

import numpy as np
import pandas as pd
import yfinance as yf
from ta import add_all_ta_features
from ta.momentum import RSIIndicator, StochasticOscillator
from ta.trend import MACD, EMAIndicator, SMAIndicator
from ta.volatility import BollingerBands, AverageTrueRange
from sklearn.ensemble import RandomForestClassifier, GradientBoostingClassifier
from sklearn.preprocessing import StandardScaler
from sklearn.model_selection import TimeSeriesSplit
from sklearn.metrics import accuracy_score
from rich.console import Console
from rich.table import Table
from rich.panel import Panel
from rich.text import Text
from rich import box

warnings.filterwarnings("ignore")

console = Console()

# ──────────────────────────────────────────────────────────────
# Data fetching
# ──────────────────────────────────────────────────────────────

def fetch_stock_data(ticker: str, period: str = "2y") -> pd.DataFrame:
    """Download OHLCV data from Yahoo Finance."""
    console.print(f"  [dim]Fetching {ticker} data...[/dim]")
    stock = yf.Ticker(ticker)
    df = stock.history(period=period, auto_adjust=True)
    if df.empty:
        raise ValueError(f"No data returned for ticker '{ticker}'. Check the symbol.")
    df.index = df.index.tz_localize(None)  # strip timezone for clean display
    return df


def fetch_realtime_price(ticker: str) -> dict:
    """Return the latest price info (fast_info from yfinance)."""
    stock = yf.Ticker(ticker)
    info = stock.fast_info
    return {
        "last_price":    getattr(info, "last_price",    None),
        "previous_close": getattr(info, "previous_close", None),
        "day_high":      getattr(info, "day_high",      None),
        "day_low":       getattr(info, "day_low",       None),
        "volume":        getattr(info, "last_volume",   None),
        "market_cap":    getattr(info, "market_cap",    None),
        "currency":      getattr(info, "currency",      "USD"),
    }


# ──────────────────────────────────────────────────────────────
# Feature engineering
# ──────────────────────────────────────────────────────────────

FEATURE_COLS = [
    "rsi", "macd", "macd_signal", "macd_diff",
    "bb_upper", "bb_lower", "bb_pct",
    "ema_9", "ema_21", "ema_50",
    "sma_20", "sma_50", "sma_200",
    "atr",
    "stoch_k", "stoch_d",
    "volume_change",
    "return_1d", "return_3d", "return_5d",
    "volatility_10d",
    "price_vs_sma20", "price_vs_sma50",
]


def add_features(df: pd.DataFrame) -> pd.DataFrame:
    df = df.copy()
    close = df["Close"]
    high  = df["High"]
    low   = df["Low"]
    vol   = df["Volume"]

    # Momentum
    df["rsi"] = RSIIndicator(close, window=14).rsi()

    # MACD
    macd_ind = MACD(close)
    df["macd"]        = macd_ind.macd()
    df["macd_signal"] = macd_ind.macd_signal()
    df["macd_diff"]   = macd_ind.macd_diff()

    # Bollinger Bands
    bb = BollingerBands(close, window=20, window_dev=2)
    df["bb_upper"] = bb.bollinger_hband()
    df["bb_lower"] = bb.bollinger_lband()
    df["bb_pct"]   = bb.bollinger_pband()

    # Moving averages
    df["ema_9"]   = EMAIndicator(close, window=9).ema_indicator()
    df["ema_21"]  = EMAIndicator(close, window=21).ema_indicator()
    df["ema_50"]  = EMAIndicator(close, window=50).ema_indicator()
    df["sma_20"]  = SMAIndicator(close, window=20).sma_indicator()
    df["sma_50"]  = SMAIndicator(close, window=50).sma_indicator()
    df["sma_200"] = SMAIndicator(close, window=200).sma_indicator()

    # ATR (volatility proxy)
    df["atr"] = AverageTrueRange(high, low, close, window=14).average_true_range()

    # Stochastic
    stoch = StochasticOscillator(high, low, close)
    df["stoch_k"] = stoch.stoch()
    df["stoch_d"] = stoch.stoch_signal()

    # Volume change
    df["volume_change"] = vol.pct_change()

    # Returns
    df["return_1d"] = close.pct_change(1)
    df["return_3d"] = close.pct_change(3)
    df["return_5d"] = close.pct_change(5)

    # Rolling volatility
    df["volatility_10d"] = close.pct_change().rolling(10).std()

    # Price vs moving averages (distance, normalised)
    df["price_vs_sma20"] = (close - df["sma_20"]) / df["sma_20"]
    df["price_vs_sma50"] = (close - df["sma_50"]) / df["sma_50"]

    # Target: 1 if price is higher 5 trading days from now, else 0
    df["target"] = (close.shift(-5) > close).astype(int)

    return df


# ──────────────────────────────────────────────────────────────
# Model
# ──────────────────────────────────────────────────────────────

def train_model(df: pd.DataFrame):
    """
    Train an ensemble of Random Forest + Gradient Boosting on historical data.
    Uses TimeSeriesSplit to avoid look-ahead bias.
    Returns (model, scaler, last_cv_accuracy).
    """
    clean = df[FEATURE_COLS + ["target"]].dropna()

    X = clean[FEATURE_COLS].values
    y = clean["target"].values

    # Only use data up to the most recent labelled bar
    # (last 5 rows have NaN target because shift(-5) looks into the future)
    X = X[:-5]
    y = y[:-5]

    if len(X) < 60:
        raise ValueError("Not enough historical data to train a model (need at least 60 bars).")

    scaler = StandardScaler()
    X_scaled = scaler.fit_transform(X)

    # Cross-validate with time-series aware splits
    tscv = TimeSeriesSplit(n_splits=5)
    rf  = RandomForestClassifier(n_estimators=200, max_depth=8, random_state=42, n_jobs=-1)
    gb  = GradientBoostingClassifier(n_estimators=150, max_depth=4, learning_rate=0.05, random_state=42)

    cv_scores = []
    for train_idx, val_idx in tscv.split(X_scaled):
        rf.fit(X_scaled[train_idx], y[train_idx])
        preds = rf.predict(X_scaled[val_idx])
        cv_scores.append(accuracy_score(y[val_idx], preds))

    cv_acc = np.mean(cv_scores)

    # Retrain both models on all available data
    rf.fit(X_scaled, y)
    gb.fit(X_scaled, y)

    return rf, gb, scaler, cv_acc


def predict(rf, gb, scaler, df: pd.DataFrame) -> dict:
    """
    Produce a signal for the NEXT 5 trading days using the latest bar's features.
    Returns probability and signal string.
    """
    latest = df[FEATURE_COLS].dropna().iloc[-1:].values
    latest_scaled = scaler.transform(latest)

    prob_rf = rf.predict_proba(latest_scaled)[0][1]   # P(up)
    prob_gb = gb.predict_proba(latest_scaled)[0][1]

    prob_ensemble = 0.5 * prob_rf + 0.5 * prob_gb

    if prob_ensemble >= 0.60:
        signal = "BUY"
        color  = "green"
    elif prob_ensemble <= 0.40:
        signal = "SELL"
        color  = "red"
    else:
        signal = "HOLD"
        color  = "yellow"

    return {
        "signal":        signal,
        "color":         color,
        "prob_up":       prob_ensemble,
        "prob_rf":       prob_rf,
        "prob_gb":       prob_gb,
    }


# ──────────────────────────────────────────────────────────────
# Display helpers
# ──────────────────────────────────────────────────────────────

def fmt_price(value, currency="USD"):
    if value is None:
        return "N/A"
    return f"{currency} {value:,.2f}"


def fmt_pct(value):
    if value is None:
        return "N/A"
    sign = "+" if value >= 0 else ""
    color = "green" if value >= 0 else "red"
    return f"[{color}]{sign}{value:.2f}%[/{color}]"


def print_header():
    console.print()
    console.print(Panel.fit(
        "[bold cyan]Stock Market Predictor[/bold cyan]\n"
        "[dim]Real-time data · Technical Indicators · ML Signal[/dim]\n"
        "[dim red]DISCLAIMER: For educational purposes only. Not financial advice.[/dim red]",
        border_style="cyan",
        box=box.DOUBLE_EDGE,
    ))
    console.print()


def analyse_ticker(ticker: str):
    ticker = ticker.upper().strip()
    console.rule(f"[bold]{ticker}[/bold]")

    # ── 1. Fetch data ──────────────────────────────────────────
    try:
        df = fetch_stock_data(ticker)
        rt = fetch_realtime_price(ticker)
    except Exception as e:
        console.print(f"  [red]Error fetching data: {e}[/red]")
        return

    # ── 2. Feature engineering ─────────────────────────────────
    df_feat = add_features(df)

    # ── 3. Train model ─────────────────────────────────────────
    try:
        rf, gb, scaler, cv_acc = train_model(df_feat)
    except ValueError as e:
        console.print(f"  [red]Model error: {e}[/red]")
        return

    # ── 4. Predict ─────────────────────────────────────────────
    result = predict(rf, gb, scaler, df_feat)

    # ── 5. Latest indicator snapshot ───────────────────────────
    snap = df_feat[FEATURE_COLS].dropna().iloc[-1]

    last_price = rt["last_price"] or df["Close"].iloc[-1]
    prev_close = rt["previous_close"] or df["Close"].iloc[-2]
    currency   = rt["currency"] or "USD"
    day_change_pct = ((last_price - prev_close) / prev_close * 100) if prev_close else None

    # ── 6. Print price summary ─────────────────────────────────
    price_table = Table(show_header=False, box=box.SIMPLE, padding=(0, 2))
    price_table.add_column("Key",   style="dim")
    price_table.add_column("Value", style="bold")

    price_table.add_row("Last Price",   fmt_price(last_price, currency))
    price_table.add_row("Day Change",   fmt_pct(day_change_pct))
    price_table.add_row("Day High",     fmt_price(rt["day_high"], currency))
    price_table.add_row("Day Low",      fmt_price(rt["day_low"], currency))
    volume = rt["volume"]
    price_table.add_row("Volume",       f"{volume:,}" if volume else "N/A")
    mcap = rt["market_cap"]
    price_table.add_row("Market Cap",   f"{currency} {mcap/1e9:.2f}B" if mcap else "N/A")
    price_table.add_row("As of",        datetime.utcnow().strftime("%Y-%m-%d %H:%M UTC"))

    console.print(price_table)

    # ── 7. Print indicators ────────────────────────────────────
    ind_table = Table(title="Technical Indicators", box=box.ROUNDED, title_style="bold magenta")
    ind_table.add_column("Indicator", style="dim")
    ind_table.add_column("Value",     justify="right")
    ind_table.add_column("Signal",    justify="center")

    def rsi_signal(v):
        if v >= 70: return "[red]Overbought[/red]"
        if v <= 30: return "[green]Oversold[/green]"
        return "[yellow]Neutral[/yellow]"

    def macd_signal_str(diff):
        if diff > 0: return "[green]Bullish[/green]"
        if diff < 0: return "[red]Bearish[/red]"
        return "[yellow]Neutral[/yellow]"

    def bb_signal(pct):
        if pct >= 0.8: return "[red]Near Upper[/red]"
        if pct <= 0.2: return "[green]Near Lower[/green]"
        return "[yellow]Mid-range[/yellow]"

    def ma_cross_signal(price, short_ma, long_ma):
        if short_ma > long_ma: return "[green]Bullish[/green]"
        if short_ma < long_ma: return "[red]Bearish[/red]"
        return "[yellow]Neutral[/yellow]"

    ind_table.add_row("RSI (14)",        f"{snap['rsi']:.1f}",          rsi_signal(snap["rsi"]))
    ind_table.add_row("MACD Diff",       f"{snap['macd_diff']:.4f}",    macd_signal_str(snap["macd_diff"]))
    ind_table.add_row("BB %B",           f"{snap['bb_pct']:.2f}",       bb_signal(snap["bb_pct"]))
    ind_table.add_row("EMA 9 vs 21",     f"{snap['ema_9']:.2f} / {snap['ema_21']:.2f}",
                      ma_cross_signal(last_price, snap["ema_9"], snap["ema_21"]))
    ind_table.add_row("SMA 50 vs 200",   f"{snap['sma_50']:.2f} / {snap['sma_200']:.2f}",
                      ma_cross_signal(last_price, snap["sma_50"], snap["sma_200"]))
    ind_table.add_row("ATR (14)",        f"{snap['atr']:.2f}",          "Volatility measure")
    ind_table.add_row("Stoch %K / %D",  f"{snap['stoch_k']:.1f} / {snap['stoch_d']:.1f}",
                      rsi_signal(snap["stoch_k"]))
    ind_table.add_row("5-day return",    f"{snap['return_5d']*100:.2f}%", "")
    ind_table.add_row("10-day vol",      f"{snap['volatility_10d']*100:.2f}%", "")

    console.print(ind_table)

    # ── 8. Print prediction ────────────────────────────────────
    signal_color = result["color"]
    signal       = result["signal"]
    prob_up      = result["prob_up"] * 100

    pred_text = Text()
    pred_text.append(f"\n  5-day directional signal:  ", style="bold")
    pred_text.append(f"  {signal}  ", style=f"bold white on {signal_color}")
    pred_text.append(f"\n\n  P(price higher in 5 days): {prob_up:.1f}%\n", style="bold")
    pred_text.append(f"  Random Forest:             {result['prob_rf']*100:.1f}%\n", style="dim")
    pred_text.append(f"  Gradient Boosting:         {result['prob_gb']*100:.1f}%\n", style="dim")
    pred_text.append(f"  Model CV accuracy:         {cv_acc*100:.1f}%\n", style="dim")
    pred_text.append(
        "\n  [Note] CV accuracy on historical data does NOT guarantee future performance.\n",
        style="italic dim red",
    )

    console.print(Panel(pred_text, title="[bold]ML Prediction[/bold]",
                        border_style=signal_color, box=box.ROUNDED))
    console.print()


# ──────────────────────────────────────────────────────────────
# Entry point
# ──────────────────────────────────────────────────────────────

def interactive_menu():
    print_header()
    console.print("[bold]Enter ticker symbols separated by spaces, or 'q' to quit.[/bold]")
    console.print("[dim]Examples: AAPL   |   AAPL MSFT TSLA   |   BTC-USD ETH-USD[/dim]\n")

    while True:
        try:
            raw = console.input("[cyan]Ticker(s) > [/cyan]").strip()
        except (KeyboardInterrupt, EOFError):
            console.print("\n[dim]Goodbye.[/dim]")
            break

        if raw.lower() in ("q", "quit", "exit"):
            console.print("[dim]Goodbye.[/dim]")
            break

        tickers = raw.upper().split()
        if not tickers:
            continue

        for t in tickers:
            analyse_ticker(t)


def main():
    args = [a for a in sys.argv[1:] if not a.startswith("-")]

    print_header()

    if args:
        for ticker in args:
            analyse_ticker(ticker)
    else:
        interactive_menu()


if __name__ == "__main__":
    main()
