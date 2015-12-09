package fr.unice.miage.sd.tinydfs.tests.clients;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Properties;

import org.junit.BeforeClass;
import org.junit.Test;

import fr.unice.miage.sd.tinydfs.nodes.Master;
import fr.unice.miage.sd.tinydfs.tests.config.Constants;
import junit.framework.Assert;
import junitx.framework.FileAssert;

public class ClientsSizeFileTest {

	private static String storageServiceName;
	private static String registryHost; 
	private static Master master;
	private static long testStartTime;
	
	private final long unite = 1000;
	
	@BeforeClass
	/**
	 * Reads the properties and sets up the master.
	 */
	public static void setUp() {
		testStartTime = System.currentTimeMillis();
		Properties prop = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream(ClientsTest.class.getResource(
					Constants.PROPERTIES_FILE_PATH).getFile());
			prop.load(input);
			storageServiceName = prop.getProperty(
					Constants.SERVICE_NAME_PROPERTY_KEY);
			registryHost = prop.getProperty(
					Constants.REGISTRY_HOST_PROPERTY_KEY);
		} 
		catch (IOException e) {
			e.printStackTrace();
		} 
		finally {
			if (input != null) {
				try {
					input.close();
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		try {
			Registry registry = LocateRegistry.getRegistry(
					registryHost, Registry.REGISTRY_PORT);
			master = (Master) registry.lookup(storageServiceName);
		} 
		catch (RemoteException | NotBoundException e) {
			e.printStackTrace();
			System.err.println("[ClientsTestSizeFile] No master found, exiting.");
			System.exit(1);
		}
		try {
			Thread.sleep(500);
		} 
		catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	/**
	 * get Size of file before to save and
	 * Saves a file through the master, retrieves it, 
	 * and checks that the size before operation to save is equal after the size
	 */
	public void textualSizeClientTest() {
		try {
			
			  System.out.println("******************************************************************************");
			  System.out.println("\t \t CLIENT SIZE FILE TEST");
			  
			  System.out.println("******************************************************************************");
			  
				File expectedFile = new File(this.getClass().getResource(Constants.TEXTUAL_SAMPLE_FILE_PATH).getFile());
				if(expectedFile.exists())
				{
			  System.out.println("TEST SIZE: \t \t BEFORE SAVE ");
			  System.out.println("TEST SIZE: \t \t FILE NAME: " + expectedFile.getName() +" SIZE : " +expectedFile.length()/unite +" KO");
					
					master.saveFile(expectedFile);
					
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					File retrievedFile = master.retrieveFile(Constants.TEXTUAL_SAMPLE_FILE_NAME);
					retrievedFile.createNewFile();
					
			  System.out.println("TEST SIZE: \t \t AFTER SAVE: RETRIEVE");
					
			  System.out.println("TEST SIZE END: \t \t FILE NAME: " +retrievedFile.getName() +" SIZE : " +retrievedFile.length()/unite +" KO ");
					
					assertEquals(expectedFile.length(), retrievedFile.length());
					//FileAssert.assertBinaryEquals(expectedFile, retrievedFile);
			 //System.out.println("***END CLIENT TEST SIZE FILE**************");
				}else{
					System.out.println("FILE TEST NO FOUND");
				}
				
				
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
