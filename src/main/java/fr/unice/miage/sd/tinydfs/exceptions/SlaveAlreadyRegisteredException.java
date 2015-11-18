package fr.unice.miage.sd.tinydfs.exceptions;

public class SlaveAlreadyRegisteredException extends Exception {
	
	private static final long serialVersionUID = 1L;

	public SlaveAlreadyRegisteredException(int slaveId) {
	        super("The slave with " + slaveId + " is already registered. Try to change the id of the slave");
	 }
}
