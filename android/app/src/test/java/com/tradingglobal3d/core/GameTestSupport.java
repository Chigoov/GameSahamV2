package com.tradingglobal3d.core;

import com.tradingglobal3d.core.facade.TradingFacade;
import org.json.JSONObject;

/** Financial subsystem fixtures start with access; CareerAccessTest tests real progression separately. */
public final class GameTestSupport {
    private GameTestSupport() { }
    public static void unlockManager(TradingFacade game) {
        try {
            JSONObject state = game.getCareerManager().exportState();
            state.put("rank", "PORTFOLIO_MANAGER");
            game.getCareerManager().restoreState(state);
        } catch (Exception error) { throw new AssertionError(error); }
    }

    /** Unlocks only the IDX Level-4 gate for domain stress fixtures that are not career tests. */
    public static void unlockIdxLevelFourForStressFixture(TradingFacade game) {
        if (game.getProgressManager().getCurrentLevel() < 4
                && !game.getProgressManager().awardMissionXp("TEST-IDX-LEVEL-4", 450, game.getGameTimestamp()))
            throw new AssertionError("Could not provision Level-4 access for isolated stress fixture");
    }
}
