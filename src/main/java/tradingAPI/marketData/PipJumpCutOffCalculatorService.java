package tradingAPI.marketData;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.springframework.beans.factory.annotation.Value;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;

import tradingAPI.instruments.InstrumentService;
import tradingAPI.instruments.TradeableInstrument;
import tradingAPI.market.CurrentPriceInfoProvider;
import tradingAPI.market.Price;

public class PipJumpCutOffCalculatorService<T, K> implements PipJumpCutOffCalculator<T> {

	private final Cache<TradeableInstrument<T>, Double>	offsetCache;
	private final TradeableInstrument<T>				refInstrument;
	private final CurrentPriceInfoProvider<T, K>		currentPriceInfoProvider;
	private final Double								refInstrumentPip;
	private final ReadWriteLock							lock	= new ReentrantReadWriteLock();
	private final InstrumentService<T>					instrumentService;
	@Value("${broker.accountId}")
	private K											accountId;

	public PipJumpCutOffCalculatorService(TradeableInstrument<T> refInstrument,
			CurrentPriceInfoProvider<T, K> currentPriceInfoProvider, Double refInstrumentPip,
			InstrumentService<T> instrumentService) {
		this.refInstrument = refInstrument;
		this.currentPriceInfoProvider = currentPriceInfoProvider;
		this.refInstrumentPip = refInstrumentPip;
		this.instrumentService = instrumentService;
		offsetCache = CacheBuilder.newBuilder().expireAfterWrite(6L, TimeUnit.HOURS).build();
	}

	@SuppressWarnings("unchecked")
	private Double fetchSingleInstrumentPrice(TradeableInstrument<T> instrument) {
		Map<TradeableInstrument<T>, Price<T>> priceMap = this.currentPriceInfoProvider
				.getCurrentPricesForInstruments(Lists.newArrayList(instrument), accountId);
		Price<T> price = priceMap.get(instrument);
		Double instrumentPrice = (price.getAskPrice() + price.getBidPrice()) / 2;
		Lock writeLock = this.lock.writeLock();
		try {
			writeLock.lock();
			this.offsetCache.put(instrument, instrumentPrice);
		} finally {
			writeLock.unlock();
		}
		return instrumentPrice;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Double calculatePipJumpCutOff(TradeableInstrument<T> instrument) {
		Double refInstrumentPrice = this.offsetCache.getIfPresent(refInstrument);
		Double instrumentPrice = this.offsetCache.getIfPresent(instrument);
		if (refInstrumentPrice == null && instrumentPrice == null) {
			Map<TradeableInstrument<T>, Price<T>> priceMap = this.currentPriceInfoProvider
					.getCurrentPricesForInstruments(Lists.newArrayList(refInstrument, instrument), accountId);

			Price<T> refPrice = priceMap.get(refInstrument);
			refInstrumentPrice = (refPrice.getAskPrice() + refPrice.getBidPrice()) / 2;
			Price<T> price = priceMap.get(instrument);
			instrumentPrice = (price.getAskPrice() + price.getBidPrice()) / 2;
			Lock writeLock = this.lock.writeLock();
			try {
				writeLock.lock();
				this.offsetCache.put(refInstrument, refInstrumentPrice);
				this.offsetCache.put(instrument, instrumentPrice);
			} finally {
				writeLock.unlock();
			}
		} else if (refInstrumentPrice == null && instrumentPrice != null) {
			refInstrumentPrice = fetchSingleInstrumentPrice(refInstrument);
		} else if (instrumentPrice == null && refInstrumentPrice != null) {
			instrumentPrice = fetchSingleInstrumentPrice(instrument);
		}
		double pipRef = this.instrumentService.getPipForInstrument(refInstrument);
		double pip = this.instrumentService.getPipForInstrument(instrument);
		return ((instrumentPrice * pipRef) / (refInstrumentPrice * pip)) * refInstrumentPip;
	}

}
