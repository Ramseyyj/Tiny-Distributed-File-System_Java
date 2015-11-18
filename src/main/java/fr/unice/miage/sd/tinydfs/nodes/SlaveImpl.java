package fr.unice.miage.sd.tinydfs.nodes;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class SlaveImpl extends UnicastRemoteObject implements Slave {

	public SlaveImpl() throws RemoteException {
		super();
	}
	@Override
	public int getId() throws RemoteException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Slave getLeftSlave() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Slave getRightSlave() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setLeftSlave(Slave slave) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setRightSlave(Slave slave) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void subSave(String filename, List<byte[]> subFileContent) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<byte[]> subRetrieve(String filename) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

}
