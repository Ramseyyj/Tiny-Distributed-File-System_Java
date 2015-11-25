package fr.unice.miage.sd.tinydfs.exceptions;

public class DfsRootFolderNotFoundException extends Exception {

	private static final long serialVersionUID = 1L;

	public DfsRootFolderNotFoundException(String dfsRootFolder) {
		super("Error : Distributed File SYstem Root Folder Not Found : " + dfsRootFolder);
	}
}