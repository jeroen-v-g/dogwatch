package com.example.dogwatch.pages;

import org.apache.tapestry5.alerts.AlertManager;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.corelib.components.PasswordField;
import org.apache.tapestry5.ioc.annotations.Inject;

import com.example.dogwatch.security.AuthenticationException;
import com.example.dogwatch.services.Authenticator;

public class ChangePassword {
	@InjectComponent("passwordOld")
	private PasswordField passwordOldField;

	@InjectComponent("passwordNew")
	private PasswordField passwordNewField;

	@InjectComponent("passwordNew1")
	private PasswordField passwordNew1Field;

	@Inject
	private AlertManager alertManager;

	@Property
	private String passwordOld;

	@Property
	private String passwordNew;

	@Property
	private String passwordNew1;

	@Inject
	private Authenticator authenticator;

	void onSubmitFromPasswordForm() {
		try {
			if (!authenticator.verifyPassword(passwordOld)) {
				alertManager.error("Bestaande wachtwoord klopt niet");
				return;
			}
			if (!passwordNew.contentEquals(passwordNew1)) {
				alertManager.error("Nieuwe wachtwoorden zijn ongelijk");
				return;
			}
			if (authenticator.storeNewPassword(passwordOld, passwordNew))
				alertManager.success("Wachtwoord is aangepast");
			else
				alertManager.error("Wachtwoord kon niet worden aangepast");

		} catch (AuthenticationException e) {
			alertManager.error("Wachtwoord kon niet worden aangepast");
			return;
		}

	}
}
