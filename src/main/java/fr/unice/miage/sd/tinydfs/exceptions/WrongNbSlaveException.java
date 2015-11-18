package fr.unice.miage.sd.tinydfs.exceptions;

public class WrongNbSlaveException extends Exception {

	private static final long serialVersionUID = 1L;

	public WrongNbSlaveException(int nbSlave) {
	        super("The number of slave : " + nbSlave + " is not a power of 2");
	 }
}
