package fr.unice.miage.sd.tinydfs.main;

import java.io.File;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import fr.unice.miage.sd.tinydfs.nodes.Master;

public class MainTst {

	public static void main(String[] args) {
		MainTst t = new MainTst();
		byte[] b = {'a','z','f','f','e',',','u'};
		List<byte[]> res = t.getMultipleByteArray2(b);
		for (int i = 0; i < res.size(); i++) {
			for (int j = 0; j < res.get(i).length; j++) {
				System.out.print(res.get(i)[j]);
				System.out.print(" ");
			}
			System.out.println();
		}
		String path;
		try {
			path = "rmi://" + InetAddress.getLocalHost().getHostAddress() + "/masterhost";
			Remote r = Naming.lookup(path);
			((Master) r).saveBytes("test", null);
		} catch (UnknownHostException | MalformedURLException | RemoteException | NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		File test = new File("C:\\Users\\remi\\Desktop\\test123.txt");
	}
	
	private List<byte[]> getMultipleByteArray2(byte[] fileContent) {
		List<byte[]> res = new ArrayList<byte[]>();
		int byteArrayLength = fileContent.length / 4;
		int notInRangeByte = fileContent.length % 4;
		int cursor = 0;
		for (int i = 0; i < 4; i++) {
			byte[] forSlave;
			if(notInRangeByte == 0) {
				forSlave = new byte[byteArrayLength];
			} else {
				forSlave = new byte[byteArrayLength+1];
				notInRangeByte--;
			}
			for (int j = 0; j < forSlave.length; j++) {
				forSlave[j] = fileContent[cursor];
			}
			res.add(forSlave);
			cursor++;
			
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
		for (int i = 0; i < 4; i++) {
			byte[] forSlave = new byte[toDivide.length/4];
			for (int j = 0; j < forSlave.length; j++) {
				forSlave[j] = toDivide[curseur];
				curseur++;
			}
			res.add(forSlave);
		}
		return res;
	}


}
