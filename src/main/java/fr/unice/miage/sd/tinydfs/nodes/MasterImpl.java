package fr.unice.miage.sd.tinydfs.nodes;

import java.io.File;
import java.io.FileOutputStream;
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

	public MasterImpl(String dfsRootFolder, int nbSlave) throws RemoteException, WrongNbSlaveException {
		super();
		if ((nbSlave + 2 & nbSlave + 1) != 0) {
			throw new WrongNbSlaveException(nbSlave);
		}
		this.dfsRootFolder = dfsRootFolder;
		File dfsFileRootFolder = new File(dfsRootFolder);
		if(!dfsFileRootFolder.exists()){
			dfsFileRootFolder.mkdir();
			System.out.println("Création dossier " + dfsFileRootFolder.getName());
		}
		else
		{
			File[] oldFilesSlave = dfsFileRootFolder.listFiles();
			for (File oldFile:oldFilesSlave) {
				System.out.println("Suppression du fichier "  + oldFile.getName());
				oldFile.delete();
			}

		}
		this.nbSlave=nbSlave;
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
		File res = new File(dfsRootFolder + File.separator + filename);
		try {
			if(!res.exists()) {
				res.createNewFile();
			} else {
				res.delete();
				res.createNewFile();
			}
			FileOutputStream fos = new FileOutputStream(res);
			fos.write(b);
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return res;
	}

	@Override
	public byte[] retrieveBytes(String filename) throws RemoteException {
		if (rightSlave == null) {
			buildBinaryTree();
		}
		List<byte[]> bLeft = this.leftSlave.subRetrieve(filename);
		List<byte[]> bRight = this.rightSlave.subRetrieve(filename);
		bLeft.addAll(bRight);
		return getRecomposeByteArray(bLeft, bRight);
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

		// Contruction de l'arbre binaire
		int i = 1;
		while (((i+1)*2)-2<= slave.length) {
			try {
				slave[i - 1].setLeftSlave(slave[((i+1)*2)-1]);
				slave[i - 1].setRightSlave(slave[((i+1)*2)-2]);
				i++;
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}
	
	private byte[] getRecomposeByteArray(List<byte[]>...l) {
		byte[] res = new byte[l[0].get(0).length*this.nbSlave];
		int cursor=0;
		for (int k = 0; k < l.length; k++) {
			for (int i = 0; i < l[k].size(); i++) {
				for (int j = 0; j < l[k].get(i).length; j++) {
					res[cursor] = l[k].get(i)[j];
					cursor++;
				}	
			}
		}
		return res;
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
