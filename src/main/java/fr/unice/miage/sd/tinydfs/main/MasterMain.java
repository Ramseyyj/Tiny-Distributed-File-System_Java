package fr.unice.miage.sd.tinydfs.main;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import fr.unice.miage.sd.tinydfs.exceptions.WrongNbSlaveException;
import fr.unice.miage.sd.tinydfs.nodes.Master;
import fr.unice.miage.sd.tinydfs.nodes.MasterImpl;



public class MasterMain {

	// Usage: java fr.unice.miage.sd.tinydfs.main.MasterMain storage_service_name dfs_root_folder nb_slaves
	public static void main(String[] args) throws RemoteException, AlreadyBoundException {
		String storageServiceName = args[0];
		String dfsRootFolder = args[1];
		int nbSlaves = Integer.parseInt(args[2]);
		
		// Create master and register it
		Registry registry = LocateRegistry.createRegistry(1099);
		
		Master objMaster;
		try {
			 objMaster = new MasterImpl(dfsRootFolder,nbSlaves);
			 registry.bind(storageServiceName, objMaster);
			 System.out.println("Master prêt et disponible à l'adresse: rmi://"+InetAddress.getLocalHost().getHostAddress()+
					 ":1099/"+storageServiceName);
		} catch (WrongNbSlaveException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.out.println("Erreur: " + e.getMessage());
		}catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			System.out.println("Erreur: " + e.getMessage());
		}
		
		

		
	}
	
}
