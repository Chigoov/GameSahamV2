import java.nio.file.*; import java.math.*; import org.json.*;
import com.tradingglobal3d.core.facade.*; import com.tradingglobal3d.core.storage.*; import com.tradingglobal3d.core.market.*; import com.tradingglobal3d.core.collection.*; import com.tradingglobal3d.core.funds.*; import com.tradingglobal3d.core.model.*;
public class Review14Probe {
 static Path assets;
 static TradingFacade fresh(Path dir) throws Exception { TradingFacade f=new TradingFacade(SaveManager.forDirectory(dir.toFile()));f.setCatalog(InstrumentCatalog.fromJsonString(Files.readString(assets.resolve("data/market_universe.json"))));f.initializeNewGame(GameMode.INTERNAL_MARKET,new BigDecimal("50000000"));return f; }
 public static void main(String[] args) throws Exception { assets=Path.of(args[0]);HistoricalDatasetLoader.setAssetSource(x->Files.newInputStream(assets.resolve(x)));
  if(args.length>1 && args[1].equals("catalog")) {System.out.println("catalog_with_registered_asset_source="+new CollectionManager().getAllItems().size());System.out.println("funds_with_registered_asset_source="+new MutualFundManager().getAllFunds().size());return;}
  Path dir=Files.createTempDirectory("review14-deposit-");TradingFacade f=fresh(dir);
  System.out.println("net_worth_before_deposit="+f.getPersonalNetWorth().getAmount());
  f.openTimeDeposit("DEP-BCA-1M",new BigDecimal("10000000"));
  System.out.println("deposits_before_restart="+f.getActiveTimeDeposits().length());System.out.println("net_worth_after_deposit="+f.getPersonalNetWorth().getAmount());f.save();
  TradingFacade g=new TradingFacade(SaveManager.forDirectory(dir.toFile()));g.setCatalog(InstrumentCatalog.fromJsonString(Files.readString(assets.resolve("data/market_universe.json"))));
  System.out.println("load_success="+g.load());System.out.println("deposits_after_restart="+g.getActiveTimeDeposits().length());System.out.println("cash_after_restart="+g.getPersonalAccount().getTotalCash(Currency.IDR).getAmount());
  for(String id:new String[]{"FUND-SH-01","FUND-MX-01","FUND-SE-01","FUND-SY-03"}) {MutualFund fund=f.getMutualFundManager().getFund(id);if(fund!=null){Account a=f.getAccount(fund.getPoolAccountId());System.out.println(id+"_policy="+fund.getType()+"_stock_positions="+a.getPositions().size()+"_details="+fund.toJsonObject(f.getPersonalAccount().getId()));}}
  JSONObject a=f.getIssuerFundamentals("IDX:BBCA");Instrument inst=f.getCatalog().getById("IDX:BBCA");inst.updatePrice(new Money(inst.getCurrentPrice().getAmount().multiply(new BigDecimal("2")),Currency.IDR),0);JSONObject b=f.getIssuerFundamentals("IDX:BBCA");System.out.println("profit_before_quote_change="+a.getJSONObject("incomeStatement").getString("netProfit"));System.out.println("profit_after_quote_doubled="+b.getJSONObject("incomeStatement").getString("netProfit"));
  CollectibleItem trophy=f.getCollectionManager().getAllItems().stream().filter(i->i.getId().startsWith("COL-MEMO-")).findFirst().get();System.out.println("trophy_before="+trophy.isOwned());int rep=f.getCareerManager().getReputationScore();f.buyCollection(trophy.getId());System.out.println("trophy_bought_for_zero="+trophy.isOwned());f.sellCollection(trophy.getId());f.buyCollection(trophy.getId());System.out.println("reputation_before_trophy="+rep+"_after_rebuy="+f.getCareerManager().getReputationScore());
 }
}