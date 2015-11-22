package fr.unice.miage.sd.tinydfs.main;

import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;

import fr.unice.miage.sd.tinydfs.nodes.Slave;
import fr.unice.miage.sd.tinydfs.nodes.SlaveImpl;

public class SlaveMain { 
	
	// Usage: java fr.unice.miage.sd.tinydfs.main.SlaveMain master_host dfs_root_folder slave_identifier
	public static void main(String[] args) throws RemoteException, AlreadyBoundException {
		String masterHost = args[0];
		String dfsRootFolder = args[1];
		int slaveId = Integer.parseInt(args[2]);
		
		// Create slave and register it (registration name must be "slave" + slave identifier)
		Slave objSlave= new SlaveImpl(slaveId, dfsRootFolder);
		try {
			String url = "rmi://" + masterHost + "/slave" + slaveId;
			Naming.rebind(url, objSlave);
		} catch (MalformedURLException e) {
			System.err.println("Error :\n");
			e.printStackTrace();
		}
		System.out.println("slave"+slaveId + " enregistré dans le RMI");

	}

}