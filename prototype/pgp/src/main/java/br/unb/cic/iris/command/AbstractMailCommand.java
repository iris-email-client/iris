package br.unb.cic.iris.command;

import br.unb.cic.iris.i18n.MessageBundle;
import
br.unb.cic.iris.core.exception.EmailException;
import
br.unb.cic.iris.core.exception.EmailMessageValidationException;
import
br.unb.cic.iris.core.exception.EmailUncheckedException;
import java.util.List;
/*** added by dBaseCommand
 */
public abstract class AbstractMailCommand implements MailCommand {
	protected List<String> parameters;
	@Override
	public void setParameters(List<String> parameters) {
		this.parameters = parameters;
	}
	protected boolean validParameters() {
		return parameters != null && parameters.size() > 0;
	}
	public static String message(String key) {
		return MessageBundle.message(key);
	}
	protected abstract void handleExecute() throws EmailException;
	public void execute() {
		try {
			handleExecute();
		}
		catch(EmailUncheckedException eux) {
			System.err.printf("%s: %s\n", MessageBundle.message("error"),
				eux.getLocalizedMessage());
		}
		catch(EmailMessageValidationException emvx) {
			System.err.println(MessageBundle.message("error.validation"));
			for(String msg : emvx.getMessages()) {
				System.err.println(" - " + msg);
			}
		}
		catch(EmailException ex) {
			System.err.printf("%s: %s\n", MessageBundle.message("error"),
				ex.getMessage());
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		catch(Throwable t) {
			t.printStackTrace();
		}
	}
}