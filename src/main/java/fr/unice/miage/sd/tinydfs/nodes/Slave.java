package fr.unice.miage.sd.tinydfs.nodes;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface Slave extends Remote {
	//Champs
	/*
	 * Id du slave : private Integer id
	 * dossier tampon : private File dfsRootFolder
	 * Right slave : private Slave rightSlave (initialized to null)
	 * Left Slave : private Slave leftSlave (initialized to null)
	 * 
	 */
	
	//Constructeurs
	/*public Slave(Integer id, String dfsRootFolder)
	 * Initialise les fields
	 *  
	*/
	
	//Méthodes
	
	
	/**
	 * Get the id of the Slave
	 * @return Integer id
	 * @throws RemoteException
	 */
	public int getId() throws RemoteException;
	
	/**
	 * Get the left Slave of this Slave
	 * @return Slave leftSlave
	 * @throws RemoteException
	 */
	public Slave getLeftSlave() throws RemoteException;
	
	/**
	 * Get the right Slave of this Slave
	 * @return Slave rightSlave
	 * @throws RemoteException
	 */
	public Slave getRightSlave()  throws RemoteException;

	/**
	 * Set the left Slave
	 * @param Slave slave
	 * @throws RemoteException
	 */
	public void setLeftSlave(Slave slave) throws RemoteException;

	/**
	 * Set the right Slave
	 * @param Slave slave
	 * @throws RemoteException
	 */
	public void setRightSlave(Slave slave) throws RemoteException;
	
	/**
	 * Save data in the slave
	 * Si le fichier existe déjà, alors on le remplace  
	 * If right/leftSlave is null, then register the unique byte[] in the list in a file on the disk (on a folder ~/.slave(ID) )
	 * If not null : choisir un pivot dans la liste, enregistrer le pivot sur disque, envoyer les deux listes obtenus à droite et à gauche
	 * @param String filename
	 * @param List<byte[]> subFileContent
	 * @throws RemoteException
	 */
	public void subSave(String filename, List<byte[]> subFileContent) throws RemoteException;

	/**
	 * For a node Slave, return all the data from all his children
	 * if right/left slave is null, return the content of the file (null if does'nt exist)
	 * else acces to subfile of the node, assembly with his own subfile, and return everything
	 * @param filename
	 * @return List<byte> subFile 
	 * @throws RemoteException
	 */
	public List<byte[]> subRetrieve(String filename) throws RemoteException;

}
