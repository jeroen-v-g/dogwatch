package com.example.dogwatch.components;

import java.io.IOException;

import org.apache.tapestry5.BindingConstants;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.services.ApplicationGlobals;
import org.apache.tapestry5.services.Response;
import org.slf4j.Logger;

import com.example.dogwatch.services.Authenticator;

/**
 * Layout component for pages of application test-project.
 */
@Import(module = {"bootstrap/collapse","menu"})
public class Layout {

	public class PageNameClass {
		private String pageName;
		private String className;

		public PageNameClass(String pageName, String className) {
			this.pageName = pageName;
			this.className = className;
		}

		public String getPageName() {
			return pageName;
		}

		public String getClassName() {
			return className;
		}
	}

	@Inject
	private ComponentResources resources;

	/**
	 * The page title, for the <title> element and the
	 * <h1>element.
	 */
	@Property
	@Parameter(required = true, defaultPrefix = BindingConstants.LITERAL)
	private String title;

	@Property
	private PageNameClass pageNameClass;

	@Property
	@Inject
	@Symbol(SymbolConstants.APPLICATION_VERSION)
	private String appVersion;

	@Inject
	private Response response;

	@Property
	@Inject
	private Authenticator authenticator;

	@Inject
	private ApplicationGlobals applicationGlobals;
	
	@Inject
	private Logger logger;
	
	@Inject
	private Messages messages;

	public String getClassForPageName() {
		return resources.getPageName().equalsIgnoreCase(pageNameClass.className) ? "active" : null;
	}

	private String getContextPath() {
		String context = applicationGlobals.getServletContext().getContextPath();
		return context != null ? context : "";
	}

	public void onActionFromSignOut() {
		authenticator.logout();
		try {
			response.sendRedirect("/" + getContextPath());
		} catch (IOException e) {
			logger.error("Could not redirect",e);
		}
	}

	public PageNameClass[] getPageNames() {
		if (authenticator.isLoggedIn())
			return new PageNameClass[] { new PageNameClass(messages.get("Search"), "Search"),
					new PageNameClass(messages.get("NetworkDisk"), "NetworkDisk"),
					new PageNameClass(messages.get("UploadImages"), "UploadImages"),
					new PageNameClass(messages.get("ChangePassword"), "ChangePassword") };
		else
			return new PageNameClass[] { new PageNameClass(messages.get("Search"), "Search") };
	}

}
