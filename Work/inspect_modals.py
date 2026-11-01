import os, re

repo = r"C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM"
with open(os.path.join(repo, "android/app/src/main/assets/www/app.js"), "r", encoding="utf-8") as f:
    js = f.read()

modals = [
    "openPortfolioInsuranceModal", "openTechTreeModal", "openHeadhuntingModal",
    "openDailyStreakModal", "openSectorDominanceModal", "openTaxAuditModal",
    "openMarketBossModal", "openPrestigeModal", "openStrategyLabModal",
    "openCollectionsGalleryModal", "openRiskModal", "openAchievementsModal",
    "openAvatarCustomizerModal", "openResearchBoostersModal", "openStressTestModal",
    "openAbsenceReportModal", "openWelcomeModal", "openInteractiveTutorialModal",
    "openCustomFundBuilderModal", "openTransactionHistoryModal", "openPlayerProfileModal",
    "openInvestorArchetypeModal", "openResearchTiersModal", "openSaveManagementModal"
]

for m_name in modals:
    idx = js.find("function " + m_name)
    if idx != -1:
        snippet = js[idx:idx+1500]
        title_m = re.search(r"showFlow\(['\"]([^'\"]+)['\"]\)", snippet)
        title = title_m.group(1) if title_m else "NO_TITLE"
        # look for paragraphs and flowText
        p_texts = re.findall(r"<p[^>]*>([^<]+)</p>", snippet)
        ft_texts = re.findall(r"flowText\([^,]+,\s*['\"]([^'\"]+)['\"],\s*['\"]([^'\"]+)['\"]\)", snippet)
        print(f"=== {m_name} ({title}) ===")
        for pt in p_texts[:2]:
            print(f"  P: {pt.strip()[:80]}")
        for k, v in ft_texts[:2]:
            print(f"  FT: {k} -> {v.strip()[:80]}")
