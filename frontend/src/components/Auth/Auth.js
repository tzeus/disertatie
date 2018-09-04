import React, { Component } from 'react';
import Input from '../../components/UI/Input/Input';
import Button from '../../components/UI/Button/Button';
import classes from './Auth.css';
import * as actions from '../../store/actions/index';
import {connect} from 'react-redux';
import Spinner from '../../components/UI/Spinner/Spinner';


class Auth extends Component {

  state = {
    hasAccount: true,
    controls: {
      email: {
        elementType: 'input',
        elementConfig: {
          type: 'email',
          placeholder: 'E-mail address'
        },
        value: '',
        validation: {
          required: true,
          isEmail: true
        },
        valid: false,
        touched: false
      },
      password: {
        elementType: 'input',
        elementConfig: {
          type: 'password',
          placeholder: 'password'
        },
        value: '',
        validation: {
          required: true,
          minLength: 6
        },
        valid: false,
        touched: false
      }
    }
  }

  checkValidity(value, rules) {
    let isValid = true;
    if (!rules) {
      return true;
    }

    if (rules.required) {
      isValid = value.trim() !== '' && isValid;
    }

    if (rules.minLength) {
      isValid = value.length >= rules.minLength && isValid
    }

    if (rules.maxLength) {
      isValid = value.length <= rules.maxLength && isValid
    }

    if (rules.isEmail) {
      const pattern = /[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?/;
      isValid = pattern.test(value) && isValid
    }

    if (rules.isNumeric) {
      const pattern = /^\d+$/;
      isValid = pattern.test(value) && isValid
    }

    return isValid;
  }

  inputChangedHandler = (event, controlName) => {
    const updatedControls = {
      ...this.state.controls,
      [controlName]: {
        ...this.state.controls[controlName],
        value: event.target.value,
        valid: this.checkValidity(event.target.value, this.state.controls[controlName].validation),
        touched: true
      }
    }
    this.setState({ controls: updatedControls });
  }

  submitHandler = (event) => {
    event.preventDefault();
    this.props.onAuth(this.state.controls.email.value, this.state.controls.password.value, this.state.hasAccount)
  }

  switchAuthModeHandler = () => {
    this.setState(prevState => {
      return {hasAccount: !prevState.hasAccount}
    })
  }

  render() {
    const formElementsArray = [];

    for (let key in this.state.controls) {
      formElementsArray.push({
        id: key,
        config: this.state.controls[key]
      });
    }

    let form = formElementsArray.map(formElement =>  (
        <Input
          changed={(event) => this.inputChangedHandler(event, formElement.id)}
          key={formElement.id}
          invalid={!formElement.config.valid}
          elementType={formElement.config.elementType}
          elementConfig={formElement.config.elementConfig}
          shouldValidate={formElement.config.validation}
          touched={formElement.config.touched}
          value={formElement.config.value}

        />
      ));

  if(this.props.loading){
    form = <Spinner />
  }     
    return (
      <div className={classes.Auth}>
        <form onSubmit={this.submitHandler}>
          {form}
          <Button buttonClass="Success">SUBMIT</Button>
        </form>
          <Button buttonClass="Danger"
          clicked={this.switchAuthModeHandler}>
          SWITCH TO {this.state.hasAccount ? 'SIGN UP': 'LOG IN'}
          </Button>
      </div>
    );
  }
}

const mapStateToProps = state => {
  return {
    loading: state.auth.loading
  };
};

const mapDispatchToProps = dispatch => {
  return {
    onAuth: (email, password, hasAccount) => dispatch(actions.auth(email, password,hasAccount))
  };
};

export default connect(mapStateToProps, mapDispatchToProps)(Auth);