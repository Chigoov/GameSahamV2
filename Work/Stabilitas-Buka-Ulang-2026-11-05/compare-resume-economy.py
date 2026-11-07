import collections
import decimal
import hashlib
import json
import mmap
import pathlib
import sys

def compact(value):
    return json.dumps(value, separators=(",", ":"), ensure_ascii=False).encode()

def profile(path):
    with pathlib.Path(path).open("rb") as stream, mmap.mmap(stream.fileno(), 0, access=mmap.ACCESS_READ) as file:
        marker = b',"ledger":['
        start = file.rfind(marker)
        assert start > 0 and file[-2:] == b"]}"
        root = json.loads(file[:start] + b"}")
        accounts = {item["id"]: item for item in root["accounts"]}
        recent = collections.defaultdict(lambda: collections.deque(maxlen=10))
        visible = hashlib.sha256()
        visible_count = 0
        index = start + len(marker)
        while index < len(file) - 2:
            end = file.find(b"}", index) + 1
            assert end > index
            try:
                row = json.loads(file[index:end])
            except json.JSONDecodeError:
                end = file.find(b"}", end) + 1
                assert end > index
                row = json.loads(file[index:end])
            index = end + 1
            account_id = row["accountId"]
            if accounts[account_id]["type"] != "FIRM":
                continue
            fields = tuple(row[key] for key in ("timestamp", "accountId", "type", "currency", "amount", "isCredit", "balanceAfter", "description"))
            recent[account_id].append(fields)
            if row["type"] in ("TRADE_BUY", "TRADE_SELL") and decimal.Decimal(row["amount"]) >= 500_000_000:
                visible.update(compact(fields) + b"\n")
                visible_count += 1
        fills = hashlib.sha256()
        for book in sorted(root["runtimeState"]["books"], key=lambda item: item["instrumentId"]):
            fills.update(compact([book["instrumentId"], book["last"], book["halt"]]))
            for fill in book["fills"]:
                fills.update(compact([fill.get(key) for key in ("price", "quantity", "timestamp", "buyer", "seller",
                                                                "sellerRealizedNet", "buyerFee", "sellerFee")]))
        cash_and_positions = [(a["id"], a["cash"], a["reservedCash"], sorted(a["positions"], key=lambda p:(p["exchange"], p["ticker"])))
                              for a in sorted(accounts.values(), key=lambda a:a["id"])]
        return {
            "schemaVersion": root["schemaVersion"],
            "visibleCount": visible_count,
            "visibleSha256": visible.hexdigest(),
            "recentSha256": hashlib.sha256(compact([(a, list(rows)) for a, rows in sorted(recent.items())])).hexdigest(),
            "fillSha256": fills.hexdigest(),
            "cashPositionsSha256": hashlib.sha256(json.dumps(cash_and_positions, sort_keys=True).encode()).hexdigest(),
        }

before, after = map(profile, sys.argv[1:])
same = {key: before[key] == after[key] for key in before if key != "schemaVersion"}
print(json.dumps({"before": before, "after": after, "same": same}, indent=2))
assert all(same.values())
