package fr.unice.miage.sd.tinydfs.nodes;

import java.io.File;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Master extends Remote {

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
	 * @return byte[] retrieveBytes();
	 * @throws RemoteException
	 */
	public byte[] retrieveBytes(String filename) throws RemoteException;
	
	/**
	 * Ask the size of a file
	 * @param filename
	 * @return size of the file
	 * @throws RemoteException
	 */
	public long getFileSize(String filename) throws RemoteException;
	
}
