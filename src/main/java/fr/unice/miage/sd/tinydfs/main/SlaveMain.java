package fr.unice.miage.sd.tinydfs.main;

import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.Remote;
import java.rmi.RemoteException;

import fr.unice.miage.sd.tinydfs.exceptions.SlaveAlreadyRegisteredException;
import fr.unice.miage.sd.tinydfs.nodes.Slave;
import fr.unice.miage.sd.tinydfs.nodes.SlaveImpl;

public class SlaveMain { 
	
	// Usage: java fr.unice.miage.sd.tinydfs.main.SlaveMain master_host dfs_root_folder slave_identifier
	public static void main(String[] args) throws RemoteException, AlreadyBoundException {
		
		if(args.length==3)
		{
			String masterHost = args[0];
			String dfsRootFolder = args[1];
			int slaveId = Integer.parseInt(args[2]);
			
			// Create slave and register it (registration name must be "slave" + slave identifier)
			Slave objSlave= new SlaveImpl(slaveId, dfsRootFolder);
			try {
				String url = "rmi://" + masterHost + "/slave" + slaveId;
				
				boolean result = checkRegisterSlave(url, slaveId);
				if(!result)
				{
					Naming.rebind(url, objSlave);
					System.out.println("slave "+slaveId + " enregistré dans le RMI");
				}
				
			}
			catch (SlaveAlreadyRegisteredException e)
			{
				System.out.println("ERROR: " + e.getMessage());
			}catch (MalformedURLException e) {
				System.err.println("Error :\n");
				e.printStackTrace();
			} 
		}else{
			System.out.println("ERROR NOMBRE DE PARAMETRE INCORRECT");
		}
		
		

	}
	
	public static boolean checkRegisterSlave(String uri, int slaveId) throws SlaveAlreadyRegisteredException
	{
		boolean result =true;
		try {
			Remote r = Naming.lookup(uri);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			result=false;
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			result=false;
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			result=false;
		}
		if(result)
		{
			throw new SlaveAlreadyRegisteredException(slaveId);
		}
		return result;
	}

}