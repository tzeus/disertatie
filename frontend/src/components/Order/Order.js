import React, { Component } from 'react';
import classes from './order.css';

class Order extends Component {
  state = {

  }

  colorOrderByStatus = () => {
    switch (this.props.orderState) {

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
    console.log('Entered render method');

    return (
      <div className={classes.Order} style={{ backgroundColor: this.colorOrderByStatus() }} >
        <table>
          <tbody>
            <tr>
              <td>OrderId: </td>
              <td><strong>{this.props.orderId}</strong></td>
            </tr>
            <tr>
              <td>Instrument: </td>
              <td><strong>{this.props.instrument}</strong></td>
            </tr>
            <tr>
              <td>Units: </td>
              <td><strong>{this.props.units}</strong></td>
            </tr>
            <tr>
              <td>Side: </td>
              <td><strong>{this.props.side}</strong></td>
            </tr>
            <tr>
              <td>Order type: </td>
              <td><strong>{this.props.type}</strong></td>
            </tr>
            <tr>
              <td>Take profit: </td>
              <td><strong>{this.props.takeProfit}</strong></td>
            </tr>
            <tr>
              <td>Stop loss: </td>
              <td><strong>{this.props.stopLoss}</strong></td>
            </tr>
            <tr>
              <td>State: </td>
              <td><strong>{this.props.orderState}</strong></td>
            </tr>
            <tr>
              <td>Filling transaction id: </td>
              <td><strong>{this.props.fillingTransactionID}</strong></td>
            </tr>
            <tr>
              <td>Created at: </td>
              <td><strong>{this.props.createTime}</strong></td>
            </tr>
          </tbody>
        </table>










      </div>
    );
  }

}


export default Order;