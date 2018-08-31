package com.tudoreloprisan.tradingAPI.order;

import java.util.Collection;

import com.tudoreloprisan.tradingAPI.instruments.TradeableInstrument;

public interface OrderManagementProvider<M, N, K> {
	
	M placeOrder(Order<N, M> order, K accountId);
	
	boolean modifyOrder(Order<N, M> order, K accountId);

	boolean closeOrder(M orderId, K accountId);

	Collection<Order<String, String>> getAllOrders(String accountId);

	Collection<Order<N, M>> allPendingOrders();

	Collection<Order<N, M>> pendingOrdersForAccount(K accountId);

	<T> T pendingOrderForAccount(M orderId, K accountId);

	Collection<Order<N, M>> pendingOrdersForInstrument(TradeableInstrument<N> instrument);

}
