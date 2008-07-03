package org.guvnor.tools.utils;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IDecoratorManager;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.guvnor.tools.Activator;
import org.guvnor.tools.GuvnorDecorator;
import org.guvnor.tools.utils.webdav.IWebDavClient;
import org.guvnor.tools.utils.webdav.WebDavClientFactory;
import org.guvnor.tools.utils.webdav.WebDavServerCache;
import org.guvnor.tools.utils.webdav.WebDavSessionAuthenticator;

/**
 * A set of utilities for interacting with the Eclipse platform.
 * 
 * @author jgraham
 */
public class PlatformUtils {
	
	private static PlatformUtils instance;
	
	/**
	 * For convenience, we keep one instance of PlatformUtils around
	 * @return the PlatformUtils instance
	 */
	public static PlatformUtils getInstance() {
		if (instance == null) {
			instance = new PlatformUtils();
		}
		return instance;
	}
	
	public static void updateDecoration() {
		IDecoratorManager manager = Activator.getDefault().getWorkbench().getDecoratorManager();
		manager.update(GuvnorDecorator.DECORATOR_ID);
	}
	/**
	 * Opens a read-only, in-memory editor.
	 * @param contents The contents for the editor
	 * @param name The name of the file. Will be used to determine
	 *        eclipse editor association, defaulting to text editor
	 *        if no association is found
	 */
	public static void openEditor(String contents, String name) {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IStorage storage = new StringStorage(contents, name);
		IStorageEditorInput input = new StringInput(storage);
		IWorkbenchPage page = window.getActivePage();
		IEditorDescriptor desc = PlatformUI.getWorkbench().
        							getEditorRegistry().getDefaultEditor(name);
		// If there is no editor associated with the given file name, we'll just
		// use the eclipse text editor as a default
		String editorId = desc != null?desc.getId():"org.eclipse.ui.DefaultTextEditor";
		try {
		if (page != null) {
			page.openEditor(input, editorId);
		}
		} catch (Exception e) {
			Activator.getDefault().writeLog(IStatus.ERROR, e.getMessage(), e);
		}
	}
	
	public boolean authenticateForServer(String server, IWebDavClient client) throws Exception {
		AuthPromptResults res = promptForAuthentication(server);
		if (res != null) {
			if (res.wasSaved()) {
				Platform.addAuthorizationInfo(new URL(server), "", "basic", res.getAuthInfo());
			} else {
				WebDavSessionAuthenticator authen = new WebDavSessionAuthenticator();
				authen.addAuthenticationInfo(new URL(server), "", "basic", res.getAuthInfo());
				client.setSessionAuthenticator(authen);
				client.setSessionAuthentication(true);
			}
			return true;
		} else {
			return false;
		}
	}
	
	public void updateAuthentication(String server, String username, 
			                        String password, boolean saveInfo) throws Exception {
		Map<String, String> info = new HashMap<String, String>();
		info.put("username", username);
		info.put("password", password);
		URL serverUrl = new URL(server);
		IWebDavClient client = WebDavServerCache.getWebDavClient(server);
		if (client == null) {
			client = WebDavClientFactory.createClient(serverUrl);
			WebDavServerCache.cacheWebDavClient(server, client);
		}
		if (saveInfo) {
			Platform.flushAuthorizationInfo(serverUrl, "", "basic");
			Platform.addAuthorizationInfo(serverUrl, "", "basic", info);
		} else {
			WebDavSessionAuthenticator authen = new WebDavSessionAuthenticator();
			authen.addAuthenticationInfo(new URL(server), "", "basic", info);
			client.setSessionAuthenticator(authen);
			client.setSessionAuthentication(true);
		}
	}
	
	/**
	 * Convenience method for reporting log in failure
	 */
	public static void reportAuthenticationFailure() {
		Display display = PlatformUI.getWorkbench().getDisplay();
		display.asyncExec(new Runnable() {
			public void run() {
				Display display = Display.getCurrent();
				Shell shell = display.getActiveShell();
				MessageDialog.openError(shell, "Guvnor Repository Log in", 
						               "Authentication failure");
			}
		});
	}
	
	/**
	 * Prompts for user name and password for a given Guvnor repository.
	 * @param server The repository for log in
	 * @return The dialog results. Includes whether the user decided to save
	 *         the user name and password in the platform's key ring.
	 *         Null if the user cancels the dialog.
	 */
	public AuthPromptResults promptForAuthentication(final String server) {
		
		Display display = PlatformUI.getWorkbench().getDisplay();
		AuthPromptRunnable op = new AuthPromptRunnable(server);
	    display.syncExec(op);
	    return op.getResults();
	}
	
	/**
	 * An operation for running a log in dialog in the next
	 * available UI thread.
	 * @author jgraham
	 */
	class AuthPromptRunnable implements Runnable {
		AuthPromptResults res = null;
		private String server;
		
		public AuthPromptRunnable(String server) {
			this.server = server;
		}
		
		public void run() {
            Display display = Display.getCurrent();
            Shell shell = display.getActiveShell();
            AuthenticationPromptDialog diag = new AuthenticationPromptDialog(shell, server);
            if (diag.open() == Dialog.OK) {
            	Map<String, String> info = new HashMap<String, String>();
            	info.put("username", diag.getUserName());
            	info.put("password", diag.getPassword());
            	res = new AuthPromptResults(info, diag.saveAuthenInfo());
            }
        }
		
		public AuthPromptResults getResults() {
			return res;
		}
	}
	
	/**
	 * The results from a log in dialog prompt.
	 * @author jgraham
	 */
	public class AuthPromptResults {
		// username and password
		private Map<String, String> info;
		// whether the user wants to save the authentication information
		// in the platform's key ring file
		private boolean saved;
		
		public AuthPromptResults(Map<String, String> info, boolean saved) {
			this.info = info;
			this.saved = saved;
		}
		
		public Map<String, String> getAuthInfo() {
			return info;
		}
		
		public boolean wasSaved() {
			return saved;
		}
	}
}