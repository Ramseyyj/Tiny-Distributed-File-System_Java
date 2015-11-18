package fr.unice.miage.sd.tinydfs.nodes;

import java.io.File;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class MasterImpl extends UnicastRemoteObject implements Master {

	public MasterImpl() throws RemoteException {
		super();
	}

	@Override
	public String getDfsRootFolder() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getNbSlaves() throws RemoteException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void saveFile(File file) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void saveBytes(String filename, byte[] fileContent) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public File retrieveFile(String filename) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] retrieveBytes(String filename) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void register(Slave slave) throws RemoteException {
		// TODO Auto-generated method stub
		
	}
}
