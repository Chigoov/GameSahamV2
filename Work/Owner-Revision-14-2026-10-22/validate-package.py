"""Check the planning package only; this does not test or approve the game."""
from collections import Counter
from pathlib import Path
import csv
import hashlib
import json
import re


def main():
    base = Path(__file__).resolve().parent
    root = base.parents[1]
    decisions = json.loads((base / "DECISIONS.json").read_text(encoding="utf-8"))
    assert decisions["status"] == "OWNER_ANSWERS_RECEIVED"
    assert {a["id"] for a in decisions["answers"]} == {f"Q{i:02}" for i in range(1, 16)}
    assert all(a["owner_answer_verbatim"].strip() for a in decisions["answers"])

    def read_csv(name):
        with (base / name).open(encoding="utf-8-sig", newline="") as stream:
            return list(csv.DictReader(stream))

    items = read_csv("KATALOG-KOLEKSI-150.csv")
    funds = read_csv("KATALOG-REKSA-DANA-24.csv")
    tasks = read_csv("TRACKER.csv")
    assert len(items) == len({i["item_id"] for i in items}) == 150
    assert len({i["kategori_id"] for i in items}) == 12
    assert {i["rarity"] for i in items} == {"UMUM", "TIDAK_UMUM", "LANGKA", "EPIK", "LEGENDARIS"}
    assert sum(i["status"] == "EXISTING" for i in items) == 12
    assert sum(i["status"] == "NEW_PROPOSED" for i in items) == 138
    assert sum(i["cara_perolehan"] == "ACHIEVEMENT" for i in items) == 10
    for item in items:
        assert item["nama"] and item["kategori"] and item["art_brief"]
        assert int(item["harga_awal_game_idr"]) >= 0
        assert int(item["biaya_pemeliharaan_harian_game_idr"]) >= 0
        if item["cara_perolehan"] == "ACHIEVEMENT":
            assert item["dapat_dijual"] == item["dapat_diagunkan"] == "false"
            assert item["valuation_policy"] == "NON_FINANCIAL_TROPHY"

    legacy_items = {i["item_id"]: i for i in items if i["status"] == "EXISTING"}
    for old in decisions["baseline_catalogs"]["collections"]:
        current = legacy_items[old["item_id"]]
        for key in ("nama", "legacy_category", "legacy_rarity", "harga_awal_game_idr",
                    "biaya_pemeliharaan_harian_game_idr", "bonus_reputasi"):
            assert current[key] == str(old[key]), (old["item_id"], key)

    assert len(funds) == len({f["fund_id"] for f in funds}) == 24
    assert len({f["keluarga"] for f in funds}) == 8
    assert sum(f["status"] == "EXISTING" for f in funds) == 3
    for fund in funds:
        assert sum(int(fund[k]) for k in ("saham_pct", "surat_utang_pct", "kas_kontrak_pct")) == 100
        assert 1 <= int(fund["skor_risiko_game_1_7"]) <= 7
        assert fund["benchmark_id"] and fund["kebijakan"] and fund["factsheet_required"]
    legacy_funds = {f["fund_id"]: f for f in funds if f["status"] == "EXISTING"}
    for old in decisions["baseline_catalogs"]["funds"]:
        assert legacy_funds[old["fund_id"]]["nama"] == old["nama"]
        assert legacy_funds[old["fund_id"]]["initial_nav_new_only"] == "PRESERVE_EXISTING"

    required_ids = {f"F{i:02}" for i in range(1, 6)} | {f"R{i:02}" for i in range(1, 15)} | {"INT"}
    assert len(tasks) == 20 and {t["id"] for t in tasks} == required_ids
    for task in tasks:
        assert set(task["depends_on"].split(";")) <= required_ids | {"P00"}
    task_map = {t["id"]: t for t in tasks}

    def visit(task_id, active, done):
        if task_id == "P00" or task_id in done:
            return
        assert task_id not in active, f"Dependency cycle: {task_id}"
        active.add(task_id)
        for dependency in task_map[task_id]["depends_on"].split(";"):
            visit(dependency, active, done)
        active.remove(task_id)
        done.add(task_id)

    for task_id in required_ids:
        visit(task_id, set(), set())
    spec = (base / "LONGTASK-14-REVISI.md").read_text(encoding="utf-8")
    assert all(re.search(rf"^## \d+\. R{i:02}\b", spec, re.M) for i in range(1, 15))
    assert all(f"F{i:02}" in spec for i in range(1, 6))
    prompt = root / "Prompts/Longtask-14-Revisi-Pemilik-2026-10-22.md"
    assert prompt.is_file()
    for filename in ("README.md", "LONGTASK-14-REVISI.md", "REVIEW-AND-INTAKE.md"):
        content = (base / filename).read_text(encoding="utf-8")
        for target in re.findall(r"\]\(([^)]+)\)", content):
            if "://" not in target and target != "PACKET-VALIDATION.json":
                assert (base / target.split("#")[0]).exists(), (filename, target)

    report = {
        "verdict": "PASS_PLANNING_PACKAGE_ONLY",
        "date": "2026-10-22",
        "owner_answers": 15,
        "collections": len(items),
        "collection_categories": dict(Counter(i["kategori"] for i in items)),
        "collection_rarities": dict(Counter(i["rarity"] for i in items)),
        "preserved_legacy_collections": len(legacy_items),
        "funds": len(funds),
        "fund_families": dict(Counter(f["keluarga"] for f in funds)),
        "preserved_legacy_funds": len(legacy_funds),
        "tracker_rows": len(tasks),
        "dependency_cycles": 0,
        "implementation_status": "NOT_STARTED_BY_THIS_PACKAGE",
        "product_acceptance": "NOT_EVALUATED",
        "scope": "Counts, IDs, legacy values, allocations, decisions, dependency graph and document links only",
        "sha256": {
            f.name: hashlib.sha256(f.read_bytes()).hexdigest()
            for f in base.iterdir() if f.is_file() and f.name != "PACKET-VALIDATION.json"
        },
        "prompt_sha256": hashlib.sha256(prompt.read_bytes()).hexdigest(),
    }
    (base / "PACKET-VALIDATION.json").write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")
    print(json.dumps({k: report[k] for k in ("verdict", "owner_answers", "collections", "funds", "tracker_rows", "dependency_cycles")}))


if __name__ == "__main__":
    main()
