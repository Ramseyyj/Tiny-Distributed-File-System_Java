package fr.unice.miage.sd.tinydfs.nodes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class SlaveImpl extends UnicastRemoteObject implements Slave {

	private int idSlave;
	private String dfsRootFolder;
	private Slave leftSlave,rightSlave;

	public SlaveImpl(int id, String dfsRootFolder) throws RemoteException {
		super();
		this.dfsRootFolder = dfsRootFolder;
		idSlave = id;
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
		rightSlave = slave;
	}

	@Override
	public void subSave(String filename, List<byte[]> subFileContent) throws RemoteException {
		int sizeList,middleList;
		middleList=(sizeList=subFileContent.size())/2;
		try {
			subSaveDisk(idSlave+filename, subFileContent.get(middleList));
		} catch (IOException e) {
			System.err.println("Erreur d'écriture du fichier " + idSlave+filename);
			e.printStackTrace();
		}
		if (middleList != 0) {
			leftSlave.subSave(filename, subFileContent.subList(0, middleList));
			rightSlave.subSave(filename, subFileContent.subList(middleList + 1,sizeList));
		}
	}

	private void subSaveDisk(String filename, byte[] fileContent) throws IOException {
		FileOutputStream stream = new FileOutputStream(dfsRootFolder + File.separator + filename);
		stream.write(fileContent);
		stream.close();
	}

	private byte[] subRetireveDisk(String filename) {
		Path path = Paths.get(dfsRootFolder + File.separator + filename);
		byte[] data = null;
		try {
			data = Files.readAllBytes(path);
		} catch (IOException e) {
			System.err.println("Erreur de lecture du fichier "+dfsRootFolder + File.separator + filename);
			e.printStackTrace();
		}
		return data;
	}

	@Override
	public List<byte[]> subRetrieve(String filename) throws RemoteException {
		if(leftSlave!=null)
		{
			List<byte[]> responsableList = leftSlave.subRetrieve(filename);
			responsableList.add(subRetireveDisk(idSlave+filename));
			responsableList.addAll(rightSlave.subRetrieve(filename));
			return responsableList;
		}
		else {
			return Arrays.asList(subRetireveDisk(idSlave+filename));
		}
	}
}
