package fr.unice.miage.sd.tinydfs.tests.clients;

import static org.junit.Assert.assertEquals;

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

public class ClientsSizeFileTest {

	private static String storageServiceName;
	private static String registryHost; 
	private static Master master;
	private final long unite = 1000;
	
	@BeforeClass
	/**
	 * Methode exécutée avant le lancement des tests
	 * Lecture du fichier tests.properties pour récupérer les paramètres de connexion au Master.
	 */
	public static void setUp() {
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
	 * Méthode éxécuter après le setUp afin de verifier que la taille du fichier avant la sauvegarde est égale 
	 * à celle après la sauvegarde
	 * Utilise l'assertion assertEquals de JUnit pour vérifier l'égalité des tailles
	 * @throws org.junit.Assert.fail
	 * @throws org.junit.Assert.failNotEquals
	 * @throws org.junit.Assert.assertEquals
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
					
					
			  System.out.println("TEST SIZE: \t \t AFTER SAVE: RETRIEVESIZE");
					assertEquals(expectedFile.length(), master.getFileSize(Constants.TEXTUAL_SAMPLE_FILE_NAME));
					System.out.println("Test du cas ou le fichier n'existe pas");
					assertEquals(-1, master.getFileSize("cecidoitetreunfichierinexistant"));
				}else{
					System.out.println("FILE TEST NO FOUND");
				}
				
				
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
