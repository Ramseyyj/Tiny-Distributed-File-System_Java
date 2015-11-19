package fr.unice.miage.sd.tinydfs.main;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import fr.unice.miage.sd.tinydfs.nodes.Master;
import fr.unice.miage.sd.tinydfs.nodes.MasterImpl;
import fr.unice.miage.sd.tinydfs.nodes.Slave;
import fr.unice.miage.sd.tinydfs.nodes.SlaveImpl;

public class SlaveMain { 
	
	// Usage: java fr.unice.miage.sd.tinydfs.main.SlaveMain master_host dfs_root_folder slave_identifier
	public static void main(String[] args) throws RemoteException, AlreadyBoundException {
		String masterHost = args[0];
		String dfsRootFolder = args[1];
		int slaveId = Integer.parseInt(args[2]);
		System.out.println("SlaveMain.main()");
		
		// Create slave and register it (registration name must be "slave" + slave identifier)
		Slave objSlave= new SlaveImpl();
		Registry registry = LocateRegistry.getRegistry(1099);
		registry.bind("slave"+slaveId, objSlave);
		System.out.println("slave"+slaveId + " enregistré dans le RMI");

	}

}