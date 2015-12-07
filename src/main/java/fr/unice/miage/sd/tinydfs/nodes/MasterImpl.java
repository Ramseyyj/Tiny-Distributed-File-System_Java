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
import java.util.HashMap;
import java.util.List;

import fr.unice.miage.sd.tinydfs.exceptions.WrongNbSlaveException;

public class MasterImpl extends UnicastRemoteObject implements Master {

	private String dfsRootFolder;
	private int nbSlave;
	private Slave[] slave;
	private Slave rightSlave;
	private Slave leftSlave;
	private boolean isBuilded;
	private HashMap<String, List<Thread>> fileLocked;

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
		this.isBuilded = false;
		this.nbSlave=nbSlave;
		this.slave = new Slave[nbSlave];
		this.rightSlave = null;
		this.leftSlave = null;
		this.fileLocked = new HashMap<>();
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
	public void saveBytes(final String filename, final byte[] fileContent) throws RemoteException {
		if (!isBuilded) {
			buildBinaryTree();
			isBuilded = true;
		}
		Thread threadRetrieve = new Thread(new Runnable() {
			@Override
			public void run() {
				System.out.println("Thread running");
				List<byte[]> divideFile = getMultipleByteArray2(fileContent);
				List<byte[]> forLeftSlave = new ArrayList<byte[]>(divideFile.subList(0, divideFile.size()/2));
				List<byte[]> forRightSlave = new ArrayList<byte[]>(divideFile.subList(divideFile.size()/2, divideFile.size())) ;
				try {
					leftSlave.subSave(filename, forLeftSlave);
					rightSlave.subSave(filename, forRightSlave);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				fileLocked.get(filename).remove(this);
			}
		});
		if(!fileLocked.containsKey(filename)){
			fileLocked.put(filename, new ArrayList<Thread>());
		}
		fileLocked.get(filename).add(threadRetrieve);
		threadRetrieve.start();
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
		if (!isBuilded) {
			buildBinaryTree();
			isBuilded = true;
		}
		for (Thread retrieve : fileLocked.get(filename)) {
				try {
					
					retrieve.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		List<byte[]> bLeft = this.leftSlave.subRetrieve(filename);
		List<byte[]> bRight = this.rightSlave.subRetrieve(filename);
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
		while (((i+1)*2)-2< slave.length) {
			try {
				slave[i - 1].setLeftSlave(slave[((i+1)*2)-2]);
				slave[i - 1].setRightSlave(slave[((i+1)*2)-1]);
				i++;
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		
		//A supprimer, test de construction de l'arbre
		for (int j = 0; j < (slave.length / 2) - 1; j++) {
			try {
				System.out.println("Slave" + slave[j].getId() + " has for left leftSlave slave"+ slave[j].getLeftSlave().getId() + " and has for rightSlave slave" + slave[j].getRightSlave().getId());
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}
	
	private byte[] getRecomposeByteArray(List<byte[]> leftList, List<byte[]> rightList) {
		byte[] res = new byte[0];
		for (int i = 0; i < leftList.size(); i++) {
			res = concat(res, leftList.get(i));
		}
		for (int i = 0; i < rightList.size(); i++) {
			res = concat(res, rightList.get(i));
		}
		System.out.println("res size : "+ res.length);
		return res;
	}
	
	private byte[] concat(byte[] b1, byte[] b2) {
		int b1Len = b1.length;
		int b2Len = b2.length;
		byte[] res = new byte[b1Len + b2Len];
		System.arraycopy(b1, 0, res, 0, b1Len);
		System.arraycopy(b2, 0, res, b1Len, b2Len);
		return res;
	}
		
	private List<byte[]> getMultipleByteArray2(byte[] fileContent) {
		System.out.println("Filecontent length : " + fileContent.length);
		List<byte[]> res = new ArrayList<byte[]>();
		int byteArrayLength = fileContent.length / this.nbSlave;
		int notInRangeByte = fileContent.length % this.nbSlave;
		int cursor = 0;
		for (int i = 0; i < this.nbSlave; i++) {
			byte[] forSlave;
			if(notInRangeByte == 0) {
				forSlave = new byte[byteArrayLength];
			} else {
				forSlave = new byte[byteArrayLength+1];
				notInRangeByte--;
			}
			for (int j = 0; j < forSlave.length; j++) {
				forSlave[j] = fileContent[cursor];
				cursor++;
			}
			res.add(forSlave);			
		}
		return res;
	}

	private List<byte[]> getMultipleByteArray(byte[] fileContent) {
		System.out.println("Filecontent length : " + fileContent.length);
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
