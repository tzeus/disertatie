import React, { Component } from 'react';
import Order from '../Order/Order';
import allOrders from '../../assets/orders.json';
import classes from './Orders.css';
import Auxiliary from '../../hoc/Auxiliary';

class Orders extends Component {
  constructor() {
    super();
    this.state = {
      orders: []
    }
  }

  componentDidMount() {



    let myOrders = allOrders.allOrders.map((order) => {
      // return (

      <li className={classes.listItem}>
        <a href="#">Order ID: {order.orderId}</a>
      </li>

      //     <Order
      //       key={order.orderId}
      //       orderId={order.orderId}
      //       instrument={order.instrument}
      //       units={order.units}
      //       side={order.side}
      //       type={order.type}
      //       takeProfit={order.takeProfit}
      //       stopLoss={order.stopLoss}
      //       price={order.price}
      //       orderState={order.state}
      //       fillingTransactionID={order.fillingTransactionID}
      //       createTime={order.createTime}
      //     />
      //   )
      // })
      // this.setState({orders: myOrders});
    });
    this.setState({ myOrders: myOrders });

  }

  colorOrderByStatus = (orderState) => {
    switch (orderState) {

      case 'FILLED':
        return '#5C9210';
      case 'PENDING':
        return 'gray';
      case 'CANCELLED':
        return '#944317';
      default:
        break;
    }
  }


  render() {

    let myOrders = allOrders.allOrders.map((order) => {
      return (

        <li style={{ backgroundColor: order.state === 'FILLED' ? '#5C9210' : '#944317' }}>
          <a href="#">Order ID: {order.orderId}</a>
        </li>
      )
    });

    return (
      <Auxiliary>
        <div className={classes.Flow}>

          <div className={classes.NavigationItems}>
            <ul>
              <li>Recent orders</li>
              {myOrders}
            </ul>
          </div>
          <div className={classes.OrderInfo}>
            
      </div>
        </div>
      </Auxiliary>
    );
  }
}


export default Orders;

