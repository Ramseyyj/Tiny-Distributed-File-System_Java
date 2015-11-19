package fr.unice.miage.sd.tinydfs.nodes;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class SlaveImpl extends UnicastRemoteObject implements Slave {

	private int idSlave;
	private String dfsRootFolder;
	private Slave leftSlave;
	private Slave rightSlave;

	public SlaveImpl(int id, String dfsRootFolder) throws RemoteException {
		super();
		this.dfsRootFolder=dfsRootFolder;
		idSlave=id;
		
	}

	@Override
	public int getId() throws RemoteException {
		return idSlave;
	}

	@Override
	public Slave getLeftSlave() throws RemoteException {
		return leftSlave;
	}

	@Override
	public Slave getRightSlave() throws RemoteException {
		return rightSlave;
	}

	@Override
	public void setLeftSlave(Slave slave) throws RemoteException {
		leftSlave = slave;
	}

	@Override
	public void setRightSlave(Slave slave) throws RemoteException {
		rightSlave = rightSlave;
	}

	@Override
	public void subSave(String filename, List<byte[]> subFileContent) throws RemoteException {
		// TODO Auto-generated method stub

		int middleList = (int) Math.floor(subFileContent.size() / 2);
		subFileContent.get(middleList);
		subSavedisk(filename, subFileContent.get(middleList));
		if (subFileContent.size() > 1) {
			leftSlave.subSave(filename, subFileContent.subList(0, middleList));
			rightSlave.subSave(filename, subFileContent.subList(0, middleList + 1));
		}
	}
	private void subSavedisk(String filename, byte[] FileContent)
	{
		
	}
	private byte[] subRetireveDisk(String filename)
	{
		return null;
	}
	@Override
	public List<byte[]> subRetrieve(String filename) throws RemoteException {
		List<byte[]> responsableList = leftSlave.subRetrieve(filename);
		responsableList.add(subRetireveDisk(filename));
		responsableList.addAll(rightSlave.subRetrieve(filename));
		return responsableList;
	
	}

}
