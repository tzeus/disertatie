import React, {Component} from 'react';
import Chart from './CandleSticks/Chart';
import candleData from '../../../assets/candleData.json';
import { TypeChooser } from "react-stockcharts/lib/helper";

class Trade extends Component {
	componentDidMount() {

let data = candleData.prices.map((price) => {
	return (
			data = {
				high: price.highPrice,
				low: price.lowPrice,	
				close: price.closePrice,	
				date: new Date(price.eventDate),	
				volume: price.volume,	
				open: price.openPrice,	
			}
	);
})

		// getData().then(data => {
			this.setState({ data : data });
		// })
	}
	render() {
		if (this.state == null) {
			return <div>Loading...</div>
		}
		return (
			<TypeChooser>
				{type => <Chart type={type} data={this.state.data} width={1170} />}
			</TypeChooser>
		)
	}

}



export default Trade;