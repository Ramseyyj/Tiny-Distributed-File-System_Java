package fr.unice.miage.sd.tinydfs.nodes;

import java.io.File;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Master extends Remote {
	//Fields
	/*
	 * Répertoire tampon (voir sujet) : private File DfsRootFolder
	 * Nombre de slave : private Integer nbSlaves (initialized to null)
	 * Tableau de référence : Slave[] slave
	 */
	
	//constructors
	/*public Master(String dfsRootFolder, int nbSlave  )
	 * initialized the field
	 * 
	 */
	
	//Methods
	/**
	 * Get the dfs root folder
	 * @return String DfsRootFolder
	 * @throws RemoteException
	 */
	public String getDfsRootFolder() throws RemoteException;
	
	/**
	 * Get the number of slave
	 * @return Integer nbSlave
	 * @throws RemoteException
	 */
	public int getNbSlaves() throws RemoteException;
	
	/**
	 * Save a file in the dfs
	 * Ask to first Slave to save the data
	 * @param File file
	 * @throws RemoteException
	 */
	public void saveFile(File file) throws RemoteException;
	
	/**
	 * Save a file in the dfs as byte array
	 * Ask to first Slave to save the data
	 * @param String filename
	 * @param byte [] fileContent
	 * @throws RemoteException
	 */
	public void saveBytes(String filename, byte[] fileContent) throws RemoteException;
	
	/**
	 * Retrieve a file in the dfs
	 * Ask to the first Slave the file
	 * @param filename
	 * @return File retrieveFile
	 * @throws RemoteException
	 */
	public File retrieveFile(String filename) throws RemoteException;
	
	/**
	 * Retrieve a file in the dfs as a byte array
	 * Ask to the first Slave the file
	 * @param filename
	 * @return
	 * @throws RemoteException
	 */
	public byte[] retrieveBytes(String filename) throws RemoteException;
	
}
