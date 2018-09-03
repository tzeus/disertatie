import React, { Component } from 'react';
import classes from './Home.css';
import Card from './../Cards/Card1';
import Account from '../Account/Account';
// import {connect} from 'react-redux';
// import * as actions from '../../store/actions/action';

class Home extends Component {

    render() {
        return (
            <div className={classes.Wrapper}>
                <Card />
                <Card />
                <Account />
                <Card />
                <Card />
            </div>
        );
    }
}



export default Home;