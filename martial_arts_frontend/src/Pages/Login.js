import React from 'react';
import NavBar from '../Components/NavBar';
import LoginForm from '../Components/LoginForm';
import WelcomeBar from '../Components/WelcomeBar';

export default class Login extends React.Component
{
  constructor(props)
  {
    super(props);
    this.state = 
    {
        login: true,
        user: { username: "", password: "", firstName: "", lastName: ""}
    }
    this.getUser = this.getUser.bind(this);
}

    async getUser(username, password)
    {
        try
        {
            const response = await fetch(this.props.apiUrl + "/user/login", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ username, password })
            });
            if(!response.ok) throw new Error("login failed");
            const data = await response.json();
            this.setState({user: data});
        } catch(e)
        {
            alert("Invalid username or password.");
            return;
        }
        document.cookie = "username=" + this.state.user.username;
        document.cookie = "password=" + this.state.user.password;
        document.cookie = "firstName=" + this.state.user.firstName;
        document.cookie = "lastName=" + this.state.user.lastName;
    }

    render()
    {
        let welcome = <div></div>;
        if(this.props.user.username != "") welcome = <WelcomeBar user={this.props.user}/>;

        return(
            <div className="login">
                {welcome}
                <NavBar/>
                <LoginForm getUser={this.getUser} user={this.state.user}/>
            </div>)
    }
}