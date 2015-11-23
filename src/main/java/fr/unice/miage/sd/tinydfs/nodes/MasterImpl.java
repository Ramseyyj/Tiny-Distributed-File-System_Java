package fr.unice.miage.sd.tinydfs.nodes;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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

	public MasterImpl(String dfsRootFolder, int nbSlave) throws RemoteException, WrongNbSlaveException {
		super();
		if ((nbSlave + 2 & nbSlave + 1) != 0) {
			throw new WrongNbSlaveException(nbSlave);
		}
		this.dfsRootFolder = dfsRootFolder;
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
		// Lecture du fichier
		try {
			saveBytes(file.getName(), Files.readAllBytes(file.toPath()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void saveBytes(String filename, byte[] fileContent) throws RemoteException {
		if (rightSlave == null) {
			buildBinaryTree();
		}
		// divide the byteArray into nbSlave byte Array
		List<byte[]> divideFile = getMultipleByteArray(fileContent);
		List<byte[]> forLeftSlave = divideFile.subList(0, divideFile.size()/2);
		List<byte[]> forRightSlave = divideFile.subList(divideFile.size()/2, divideFile.size()) ;
		leftSlave.subSave(filename, forLeftSlave);
		rightSlave.subSave(filename, forRightSlave);
	}

	@Override
	public File retrieveFile(String filename) throws RemoteException {
		byte[] b = retrieveBytes(filename);
		InputStream is = new ByteArrayInputStream(b);
		File res = new File(dfsRootFolder + File.pathSeparator + filename);
		return res;
	}

	@Override
	public byte[] retrieveBytes(String filename) throws RemoteException {
		if (rightSlave == null) {
			buildBinaryTree();
		}
		return null;
	}

	private void buildBinaryTree() {
		// Initialisation des références vers les slaves
		for (int i = 0; i < this.nbSlave; i++) {
			try {
				String path = "rmi://" + InetAddress.getLocalHost().getHostAddress() + "/slave" + i;
				Remote r = Naming.lookup(path);
				slave[i] = (Slave) r;
			} catch (UnknownHostException | MalformedURLException | RemoteException | NotBoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// Initialisation des fils du master
		this.leftSlave = slave[0];
		this.rightSlave = slave[1];
		try {
			System.out.println(leftSlave.getId());
		} catch (RemoteException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// Contruction de l'arbre binaire
		int i = 1;
		while ((2 * i)+1 < slave.length) {
			try {
				slave[i - 1].setLeftSlave(slave[(2 * i)]);
				slave[i - 1].setRightSlave(slave[(2 * i) + 1]);
				i++;
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	private List<byte[]> getMultipleByteArray(byte[] fileContent) {
		byte[] toDivide;
		List<byte[]> res = new ArrayList<byte[]>();
		if (fileContent.length % 2 == 1) {
			toDivide = new byte[fileContent.length + 1];
			for (int i = 0; i < fileContent.length; i++) {
				toDivide[i] = fileContent[i];
			}
		} else {
			toDivide = fileContent;
		}
		int curseur = 0;
		for (int i = 0; i < this.nbSlave; i++) {
			byte[] forSlave = new byte[toDivide.length/nbSlave];
			for (int j = 0; j < forSlave.length; j++) {
				forSlave[i] = toDivide[curseur];
				curseur++;
			}
			res.add(forSlave);
		}
		return res;
	}
}
