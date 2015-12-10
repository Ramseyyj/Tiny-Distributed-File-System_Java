package fr.unice.miage.sd.tinydfs.nodes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class SlaveImpl extends UnicastRemoteObject implements Slave {

	private int idSlave;
	private String dfsRootFolder;
	private Slave leftSlave, rightSlave;

	/**
	 * Constructeur Créee un slave et supprime les vieux fichiers
	 * 
	 * @param id = ID du Slave
	 * @param dfsRootFolder = Racine des fragments de fichier
	 * @throws RemoteException = Exeption RMI;
	 */
	public SlaveImpl(int id, String dfsRootFolder) throws RemoteException {
		super();
		this.idSlave = id;
		this.dfsRootFolder = dfsRootFolder;
		File dfsFileRootFolder = new File(dfsRootFolder);
		if (!dfsFileRootFolder.exists()) {
			dfsFileRootFolder.mkdir();
			System.out.println("Création dossier " + dfsFileRootFolder.getName());
		}
	}

	/**
	 * Getter ID
	 */
	@Override
	public int getId() throws RemoteException {
		return idSlave;
	}

	/**
	 * Getter slave gauche
	 */
	@Override
	public Slave getLeftSlave() throws RemoteException {
		return leftSlave;
	}

	/**
	 * Getter slave droit
	 */
	@Override
	public Slave getRightSlave() throws RemoteException {
		return rightSlave;
	}

	/**
	 * Setter slave gauche
	 */
	@Override
	public void setLeftSlave(Slave slave) throws RemoteException {
		leftSlave = slave;
	}

	/**
	 * Setter slave droit
	 */
	@Override
	public void setRightSlave(Slave slave) throws RemoteException {
		rightSlave = slave;
	}

	/**
	 * Méthode de répartition de fragments de fichier aux slaves fils Trouve le
	 * millieu de la liste de fragments et la sauvegarde puis envoie
	 * respectivement les partie gauches et droites de la liste aux slaves
	 * gauche et droits
	 * 
	 * @param filname= nom du fichier
	 * @param subFileContent = Liste de fragments au format byte[]
	 */
	@Override
	public void subSave(String filename, List<byte[]> subFileContent) throws RemoteException {
		int sizeList, middleList;
		middleList = (sizeList = subFileContent.size()) / 2;
		try {// Sauvegarde du fragment au milieu de la liste
			subSaveDisk(idSlave + filename, subFileContent.get(middleList));
		} catch (IOException e) {
			System.err.println("Erreur d'écriture du fichier " + idSlave + filename);
			e.printStackTrace();
		}
		if (middleList != 0) { // Envoi des parties gauches et droit de la liste
								// aux laves fils
			List<byte[]> left = new ArrayList<byte[]>(subFileContent.subList(0, middleList));
			List<byte[]> right = new ArrayList<byte[]>(subFileContent.subList(middleList + 1, sizeList));
			leftSlave.subSave(filename, left);
			rightSlave.subSave(filename, right);
		}
	}
	
	/**
	 * Methode pour récupérer la taille des fragements de fichiers contenus dans ce noeud et ces fils
	 * @param filename
	 * @return filenameSubSize 
	 */
	@Override
	public long getFileSubsize(String filename) throws RemoteException {
		File f = new File(dfsRootFolder + File.separator + idSlave + filename);
		if(!f.exists() || f.isDirectory()) {
			System.out.println("Slave" + idSlave + "ta mere");
			return -1;
		}
		if(rightSlave == null) {
			return f.length();
		}
		long rightSize = rightSlave.getFileSubsize(filename);
		long leftSize = leftSlave.getFileSubsize(filename);
		long res = leftSize + f.length() + rightSize;
		return res;
	}

	/**
	 * Méthode de sauvegarde sur disque Crée un flux de sortie dans un fichier
	 * puis écris le tableau de byte dedans
	 * 
	 * @param filename = filename à sauvegarder dans la racine
	 * @param fileContent = Tableau de byte à écrire dans le fichier
	 * @throws IOException = Exeption en cas de problème d'écriture
	 */
	private void subSaveDisk(String filename, byte[] fileContent) throws IOException {
		FileOutputStream stream = new FileOutputStream(dfsRootFolder + File.separator + filename);
		stream.write(fileContent);
		stream.close();
	}

	/**
	 * Méthode de récupération du contenu d'un fragment de fichier stocké sur le
	 * disque
	 * 
	 * @param filename = Nom du fichier sur le disque à récupérer
	 * @return le fragment du fichier lu.
	 */
	private byte[] subRetrieveDisk(String filename) {
		Path path = Paths.get(dfsRootFolder + File.separator + filename);
		byte[] data = null;
		try {
			data = Files.readAllBytes(path);
		} catch (IOException e) {
			System.err.println("Erreur de lecture du fichier " + dfsRootFolder + File.separator + filename);
			e.printStackTrace();
		}
		return data;
	}

	/**
	 * Méthode de rassemblement des fragments des fils gauche et droits dans une
	 * liste de byte
	 * 
	 * @param le nom du fichier à récupérer sur le disque et dans les slaves fils
	 * @return La liste contenant les fragments de fichiers
	 */
	@Override
	public List<byte[]> subRetrieve(String filename) throws RemoteException {
		if (!(new File(dfsRootFolder + File.separator + idSlave + filename)).exists()) {
			System.err.println("Le fichier " + idSlave + filename + " nexiste pas ... !");
			return null; // Retourne null si le ficher n'existe pas
		}

		if (leftSlave == null) { // retourne une liste de un fragment si le
									// slave n'a pas de fils
			return new ArrayList<>(Arrays.asList(subRetrieveDisk(idSlave + filename)));
		}
		List<byte[]> responsableList = leftSlave.subRetrieve(filename);
		responsableList.add(subRetrieveDisk(idSlave + filename));
		responsableList.addAll(rightSlave.subRetrieve(filename));
		return responsableList; // retourne la liste unie des fragments
	}

	/**
	 * Classe pour récupérer les vieux fichiers de slaves avant de les supprimer
	 * dans le constructeur
	 */
	class filterSlave implements FilenameFilter {

		@Override
		public boolean accept(File folder, String name) {
			return Pattern.compile("^" + idSlave).matcher(name).matches();
		}

	}
}
