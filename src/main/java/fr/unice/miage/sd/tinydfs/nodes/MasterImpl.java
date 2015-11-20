package fr.unice.miage.sd.tinydfs.nodes;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

import fr.unice.miage.sd.tinydfs.exceptions.WrongNbSlaveException;

public class MasterImpl extends UnicastRemoteObject implements Master {
	
	private String dfsRootFolder;
	private int nbSlave;
	private Slave[] slave;
	private Slave rightSlave;
	private Slave leftSlave;
	
	public MasterImpl(String dfsRootFolder, int nbSlave) throws RemoteException {
		super();
		this.nbSlave=nbSlave;
		if ((nbSlave+2 & nbSlave+1) != 0) {
			try {
				throw new WrongNbSlaveException(nbSlave);
			} catch (WrongNbSlaveException e) {
				e.printStackTrace();
			}
		}
		this.dfsRootFolder=dfsRootFolder;
		this.slave = new Slave[nbSlave];
		this.rightSlave = null;
		this.leftSlave = null;
		
	}

	@Override
	public String getDfsRootFolder() throws RemoteException {
		return this.dfsRootFolder;
	}

	@Override
	public int getNbSlaves() throws RemoteException {
		return this.nbSlave;
	}

	@Override
	public void saveFile(File file) throws RemoteException {
		if(rightSlave == null) {
			buildBinaryTree();
		}
		//Lecture du fichier
		byte[] b = null;
		String filename = file.getName();
		try {
			b = Files.readAllBytes(file.toPath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		saveBytes(filename,b);
	}

	@Override
	public void saveBytes(String filename, byte[] fileContent) throws RemoteException {
		if(rightSlave == null) {
			buildBinaryTree();
		}
		//divide the byteArray into nbSlave byte Array
		List<byte[]> divideFile = getMultipleByteArray(fileContent, this.getNbSlaves());
		List<byte[]> forLeftSlave;
		List<byte[]> forRightSlave;
		int middleList = (int) Math.floor(divideFile.size() / 2);
		
	}

	@Override
	public File retrieveFile(String filename) throws RemoteException {
		if(rightSlave == null) {
			buildBinaryTree();
		}
		return null;
	}

	@Override
	public byte[] retrieveBytes(String filename) throws RemoteException {
		if(rightSlave == null) {
			buildBinaryTree();
		}
		return null;
	}
	
	private void buildBinaryTree() {
		//Initialisation des références vers les slaves
		for (int i = 0; i < slave.length; i++) {
			try {
				String path = "rmi://" + InetAddress.getLocalHost().getHostAddress() + "/slave" + Integer.toString(i);
				Remote r = Naming.lookup(path);
				slave[i] = (Slave) r ;
			} catch (UnknownHostException | MalformedURLException | RemoteException | NotBoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		//Initialisation des fils du master
		this.leftSlave = slave[0];
		this.rightSlave = slave[1];
		
		//Contruction de l'arbre binaire
		int i =1;
		while(2*i < slave.length) {
			try {
				slave[i-1].setLeftSlave(slave[(2*i)-1]);
				slave[i-1].setRightSlave(slave[2*i]);
				i++;
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}
	
	private List<byte[]> getMultipleByteArray(byte[] fileContent, int nbArray) {
		List<byte[]> res = new ArrayList<byte[]>();
		
		return res;
	}
}
