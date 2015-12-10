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

/**
 * @author remi
 *
 */
/**
 * @author remi
 *
 */
/**
 * @author remi
 *
 */
public class MasterImpl extends UnicastRemoteObject implements Master {

	private String dfsRootFolder;
	private int nbSlave;
	private Slave[] slave;
	private Slave rightSlave;
	private Slave leftSlave;
	private boolean isBuilded;
	private HashMap<String, List<Thread>> fileLocked; 
	// Un hashmap contiens la liste des Saves en cours
	// Avoir une liste permet d'avoir plusieur Saves sur le même filename en meme temps .

	/**
	 * Constructor
	 * @param dfsRootFolder
	 * @param nbSlave
	 * @throws RemoteException
	 * @throws WrongNbSlaveException
	 */
	public MasterImpl(String dfsRootFolder, int nbSlave) throws RemoteException, WrongNbSlaveException {
		//constructeur RMI
		super();
		// Verfication que le nombre de Salve est correcte
		if ((nbSlave + 2 & nbSlave + 1) != 0) {
			throw new WrongNbSlaveException(nbSlave);
		}
		//Initialisation et création du dfsRootFOlder si celui ci n'existe pas
		this.dfsRootFolder = dfsRootFolder;
		File dfsFileRootFolder = new File(dfsRootFolder);
		if (!dfsFileRootFolder.exists()) {
			dfsFileRootFolder.mkdir();
			System.out.println("Création dossier " + dfsFileRootFolder.getName());
		//Si le fichier existait déjà, on supprime son contenu
		} else {
			File[] oldFilesSlave = dfsFileRootFolder.listFiles();
			for (File oldFile : oldFilesSlave) {
				System.out.println("Suppression du fichier " + oldFile.getName());
				oldFile.delete();
			}

		}
		//Initialisation des champs
		this.isBuilded = false;
		this.nbSlave = nbSlave;
		this.slave = new Slave[nbSlave];
		this.rightSlave = null;
		this.leftSlave = null;
		this.fileLocked = new HashMap<>();// Instanciation du Hashmap
	}

	/**
	 * Getter dfsRootFolder
	 */
	@Override
	public String getDfsRootFolder() throws RemoteException {
		return this.dfsRootFolder;
	}

	/**
	 * Getter nbSlave
	 */
	@Override
	public int getNbSlaves() throws RemoteException {
		return this.nbSlave;
	}
	
	/**
	 * saveFile, appelle savebytes, enregistre le fichier sur le système de fichier distribué
	 * @param File file
	 */
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
	/**
	 * Sauvegarde d'un tableau de bytes dans les slaves
	 * @param filename
	 * @param byte[] fileContent
	 */
	public void saveBytes(final String filename, final byte[] fileContent) throws RemoteException {
		//Si premier appel client, alors on construit l'arbre
		if (!isBuilded) {
			buildBinaryTree();
			isBuilded = true;
		}
		//Création d'un thread pour la sauvegarde soit non bloquante
		Thread threadRetrieve = new Thread(new Runnable() {
			@Override
			public void run() {
				System.out.println("Thread running");
				//On découpe notre tableau en nbSlave tableau de taille égal
				List<byte[]> divideFile = getMultipleByteArray(fileContent);
				//On découpe la liste en deux pour chacun des slaves fils du master
				List<byte[]> forLeftSlave = new ArrayList<byte[]>(divideFile.subList(0, divideFile.size() / 2));
				List<byte[]> forRightSlave = new ArrayList<byte[]>(
						divideFile.subList(divideFile.size() / 2, divideFile.size()));
				try {
					//Sauvegarde des données dans les slaves du master
					leftSlave.subSave(filename, forLeftSlave);
					rightSlave.subSave(filename, forRightSlave);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				/*Une fois le thread
				* terminé, il se retire
				* lui même de la liste, afin de retirer le lock
				*/
				fileLocked.get(filename).remove(this);
			}
		});
		// Sauvegarde des threads dans la liste correspondant à la Clé juste avant le décelnchement du Thread
		if (!fileLocked.containsKey(filename)) {
			fileLocked.put(filename, new ArrayList<Thread>());
		}
		fileLocked.get(filename).add(threadRetrieve);
		//On démarre le thread
		threadRetrieve.start();
	}

	/**
	 * retrieveFile : récupère les datas contenus dans les slaves et les renvoit sous forme d'un fichier (appel client)
	 * @param filename
	 * @return File filename
	 */
	@Override
	public File retrieveFile(String filename) throws RemoteException {
		//On récupère les morceaux de fichiers contenus dans les slaves
		byte[] b = retrieveBytes(filename);
		//Si on a rien récupéré, on retourne null
		if(b==null) {
			return null;
		}
		//On crée le fichier qu'on renverra à l'utilisateur
		File res = new File(dfsRootFolder + File.separator + filename);
		try {
			if (!res.exists()) {
				res.createNewFile();
			} else {
				res.delete();
				res.createNewFile();
			}
			//On écrit les données dans ce fichier
			FileOutputStream fos = new FileOutputStream(res);
			fos.write(b);
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//On renvoit le fichier
		return res;
	}

	@Override
	/**
	 * retrieveBytes : recupère les morceaux de fichiers contenus dans les slaves
	 * et les renvoit sous la forme d'un unique tableau de bytes (appel client)
	 * @param fileName
	 * @return bytes[] of filename
	 */
	public byte[] retrieveBytes(String filename) throws RemoteException {
		//Si v'est un premier p=appel client, on construit l'arbre
		if (!isBuilded) {
			buildBinaryTree();
			isBuilded = true;
		}
		//Si une sauvegarde est en cours sur le nom de fichier, on attend que la sauvegarde soit terminé
		if (fileLocked.containsKey(filename)) {

			for (Thread retrieve : fileLocked.get(filename)) { // Si un thread
																// ou plusieurs
																// threads
																// "Save" est
																// actuellement
																// en train de
																// s'exécuter
				try {
					retrieve.join(); // On attends la fin de ce thread "Save"
										// (avant de continuer l'exécution du
										// retireve)
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		//On récupere les bytes contenus dans les slaves
		List<byte[]> bLeft = this.leftSlave.subRetrieve(filename);
		//Si rien n'est récupéré, on renvoit null
		if(bLeft == null) {
			 return null;
		}
		List<byte[]> bRight = this.rightSlave.subRetrieve(filename);
		//On retorune un tableau de byte construit sur les tableaux récupérés
		return getRecomposeByteArray(bLeft, bRight);
	}
	
	/**
	 * Femande au master la taille d'un fichier (appel client)
	 * @param filename
	 * @return size of filename
	 */
	@Override
	public long getFileSize(String filename) throws RemoteException {
		if(!isBuilded) {
			buildBinaryTree();
		}
		//Même méthode d'attente que dans le retrieveBytes().
		if (fileLocked.containsKey(filename)) {

			for (Thread retrieve : fileLocked.get(filename)) { 
				try {
					retrieve.join(); 
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		long leftSize = leftSlave.getFileSubsize(filename);
		if(leftSize == -1) {
			System.err.println("Impossible de retrouver la taille, le fichier n'existe pas");
			return -1;
		}
		long rightSize = rightSlave.getFileSubsize(filename);
		return leftSize + rightSize;
	}

	/**
	 * Fonction de construction de l'arbre de otre système de fichier
	 */
	private void buildBinaryTree() {
		// Initialisation des références vers les slaves
		for (int i = 0; i < this.nbSlave; i++) {
			try {
				//On récupère le nom du service proposé par un slave
				String path = "rmi://" + InetAddress.getLocalHost().getHostAddress() + "/slave" + i;
				//On y accède
				Remote r = Naming.lookup(path);
				//On l'enregistre dans notre tableau de slave
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
		// Il s'agit uniquement d'un jeu d'indice sur le tableau des slaves
		int i = 1;
		while (((i + 1) * 2) - 2 < slave.length) {
			try {
				slave[i - 1].setLeftSlave(slave[((i + 1) * 2) - 2]);
				slave[i - 1].setRightSlave(slave[((i + 1) * 2) - 1]);
				i++;
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}

		//Test de construction de l'arbre, pour vérifier que tout les slmaves ont les bons fils
		for (int j = 0; j < (slave.length / 2) - 1; j++) {
			try {
				System.out.println(
						"Slave" + slave[j].getId() + " has for left leftSlave slave" + slave[j].getLeftSlave().getId()
								+ " and has for rightSlave slave" + slave[j].getRightSlave().getId());
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * getRecomposeByteArray
	 * Assemble des list<byte[]> en un tableau de byte
	 * @param leftList
	 * @param rightList
	 * @return byte[]
	 */
	private byte[] getRecomposeByteArray(List<byte[]> leftList, List<byte[]> rightList) {
		byte[] res = new byte[0];
		//Pour chaque élément de chaque liste, on concatène le tableau récupéré avec le précédent
		for (int i = 0; i < leftList.size(); i++) {
			res = concat(res, leftList.get(i));
		}
		for (int i = 0; i < rightList.size(); i++) {
			res = concat(res, rightList.get(i));
		}
		return res;
	}
	/**
	 * concat
	 * Assemble deux tableaux de bytes en un seul
	 * @param b1
	 * @param b2
	 * @return b1 concatener avec b2
	 */
	private byte[] concat(byte[] b1, byte[] b2) {
		int b1Len = b1.length;
		int b2Len = b2.length;
		byte[] res = new byte[b1Len + b2Len];
		System.arraycopy(b1, 0, res, 0, b1Len);
		System.arraycopy(b2, 0, res, b1Len, b2Len);
		return res;
	}

	/**
	 * getMultipleByteArray
	 * décompose un tableau de byte en nbSlave tableau de byte
	 * Renvoit une liste contenant tout les tableaux obtenus
	 * @param fileContent
	 * @return List<byte[]>
	 */
	private List<byte[]> getMultipleByteArray(byte[] fileContent) {
		System.out.println("Filecontent length : " + fileContent.length);
		List<byte[]> res = new ArrayList<byte[]>();
		//On calcule le nombre de tableau à créer
		//On calcule le nombre de byte en trop
		int byteArrayLength = fileContent.length / this.nbSlave;
		int notInRangeByte = fileContent.length % this.nbSlave;
		int cursor = 0;
		//Création des tableaux. 
		for (int i = 0; i < this.nbSlave; i++) {
			byte[] forSlave;
			if (notInRangeByte == 0) {
				forSlave = new byte[byteArrayLength];
			} else {
				forSlave = new byte[byteArrayLength + 1];
				notInRangeByte--;
			}
			//On remplit le tableau qui vient d'être crée avec les bytes voulus
			for (int j = 0; j < forSlave.length; j++) {
				forSlave[j] = fileContent[cursor];
				cursor++;
			}
			res.add(forSlave);
		}
		return res;
	}
}
