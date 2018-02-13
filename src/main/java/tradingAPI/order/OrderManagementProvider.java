package tradingAPI.order;

import java.util.Collection;

import tradingAPI.instruments.TradeableInstrument;

public interface OrderManagementProvider<M, N, K> {
	
	M placeOrder(Order<N, M> order, K accountId);
	
	boolean modifyOrder(Order<N, M> order, K accountId);

	boolean closeOrder(M orderId, K accountId);

	Collection<Order<N, M>> allPendingOrders();

	Collection<Order<N, M>> pendingOrdersForAccount(K accountId);

	Order<N, M> pendingOrderForAccount(M orderId, K accountId);

	Collection<Order<N, M>> pendingOrdersForInstrument(TradeableInstrument<N> instrument);

}
