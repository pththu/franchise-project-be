import logging
import os
from logging.handlers import TimedRotatingFileHandler

LOG_ROOT = os.path.join(os.path.dirname(__file__), "log")
LOG_FORMAT = "%(asctime)s | %(levelname)-8s | %(name)s | %(message)s"
DATE_FORMAT = "%Y-%m-%d %H:%M:%S"


def _make_handler(subfolder: str, filename: str) -> TimedRotatingFileHandler:
    """Create a daily-rotating file handler inside log/<subfolder>/."""
    folder = os.path.join(LOG_ROOT, subfolder)
    os.makedirs(folder, exist_ok=True)

    handler = TimedRotatingFileHandler(
        filename=os.path.join(folder, filename),
        when="midnight",       # rotate at midnight or "H" if want rotate for each hour
        interval=1,            # every 1 day
        backupCount=30,        # keep 30 days of history
        encoding="utf-8",
    )
    handler.suffix = "%d-%m-%Y"  # rotated files: service.log.11-03-2026
    handler.setFormatter(logging.Formatter(LOG_FORMAT, datefmt=DATE_FORMAT))
    return handler


def setup_logging():
    """Call once at startup to wire all loggers to daily-rotating files."""
    root = logging.getLogger()
    root.setLevel(logging.INFO)

    console = logging.StreamHandler()
    console.setFormatter(logging.Formatter(LOG_FORMAT, datefmt=DATE_FORMAT))
    root.addHandler(console)

    modules = {
        "service":          ("service",          "service.log"),
        "semantic_search":  ("semantic_search",  "semantic_search.log"),
        "recommend_system": ("recommend_system", "recommend_system.log"),
        "translate":        ("translate",        "translate.log"),
    }

    for module_name, (subfolder, filename) in modules.items():
        mod_logger = logging.getLogger(module_name)
        mod_logger.addHandler(_make_handler(subfolder, filename))
