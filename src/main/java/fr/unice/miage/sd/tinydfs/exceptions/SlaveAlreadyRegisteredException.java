package fr.unice.miage.sd.tinydfs.exceptions;

public class SlaveAlreadyRegisteredException extends Exception {

	private static final long serialVersionUID = 6447677191421739371L;

	public SlaveAlreadyRegisteredException(int idSlave) {
		super("Slave " + idSlave + " is already registered.");
	}

}
