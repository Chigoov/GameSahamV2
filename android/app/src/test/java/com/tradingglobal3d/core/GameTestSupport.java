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
}
