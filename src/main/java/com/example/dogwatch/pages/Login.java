package com.example.dogwatch.pages;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.alerts.AlertManager;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.PasswordField;
import org.apache.tapestry5.corelib.components.TextField;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.Request;
import org.slf4j.Logger;

import com.example.dogwatch.annotations.AnonymousAccess;
import com.example.dogwatch.security.AuthenticationException;
import com.example.dogwatch.services.Authenticator;

@Import(stylesheet = "custom/css/login.css")
@AnonymousAccess
public class Login {
	@Inject
	private Logger logger;

	@Inject
	private AlertManager alertManager;

	@InjectComponent
	private Form loginForm;

	@InjectComponent("username")
	private TextField usernameField;

	@InjectComponent("password")
	private PasswordField passwordField;

	@Property
	private String username;

	@Property
	private String password;

	@Inject
	private Request request;

	@Inject
	private Authenticator authenticator;

	@Property
	@Persist
	private String target;
	
	@Inject
	private Messages messages;
	
    /**
     * Respond to page activation by capturing the "target" path info as the
     * name of the target page (the page to return to after login)
     * @param context the EventContext
     */
    public void onActivate(EventContext context)
    {
        if (context.getCount() > 0)
        {
            target = context.get(String.class, 0);
        }
    }
	
	Object onSubmitFromLoginForm() {
		try {
			authenticator.login(username, password);
		} catch (AuthenticationException e) {
			alertManager.error(messages.get("error"));
			return Login.class;
		}
		alertManager.success(messages.get("welcome"));
		if (target!=null)
		{
			String returnTarget= target.toLowerCase();
			target=null;
			return returnTarget;
		}
		else
			return Search.class;

	}


}
