package br.unb.cic.iris.command;

/*** added by dBaseCommand
 */
public interface MailCommand {
	public void execute();
	public void setParameters(java.util.List<String> parameters);
	public void explain();
	public String getCommandName();
}