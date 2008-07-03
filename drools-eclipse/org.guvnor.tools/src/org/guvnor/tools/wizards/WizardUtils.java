package org.guvnor.tools.wizards;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Platform;
import org.guvnor.tools.Activator;
import org.guvnor.tools.GuvnorRepository;
import org.guvnor.tools.utils.webdav.IWebDavClient;
import org.guvnor.tools.utils.webdav.WebDavClientFactory;
import org.guvnor.tools.utils.webdav.WebDavServerCache;
import org.guvnor.tools.utils.webdav.WebDavSessionAuthenticator;

public class WizardUtils {
	public static void createGuvnorRepository(GuvWizardModel model) throws Exception {
		Activator.getLocationManager().addRepository(new GuvnorRepository(model.getRepLocation()));
		URL serverUrl = new URL(model.getRepLocation());
		Map<String, String> info = new HashMap<String, String>();
		info.put("username", model.getUsername());
		info.put("password", model.getPassword());
		if (model.shouldSaveAuthInfo()) {
			Platform.addAuthorizationInfo(serverUrl, "", "basic", info);	
		} else {
			IWebDavClient client = WebDavClientFactory.createClient(serverUrl);
			WebDavServerCache.cacheWebDavClient(serverUrl.toString(), client);
			WebDavSessionAuthenticator authen = new WebDavSessionAuthenticator();
			authen.addAuthenticationInfo(serverUrl, "", "basic", info);
			client.setSessionAuthenticator(authen);
			client.setSessionAuthentication(true);
		}
	}
}
