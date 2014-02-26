package marf.Storage;

import java.io.IOException;

/**
 * <p>Interface StorageManager</p>
 * <p>Almost every concrete module must implement this interface</p>
 *
 * <p>$Header: /cvsroot/marf/marf/src/marf/Storage/StorageManager.java,v 1.5 2003/01/28 23:53:08 mokhov Exp $</p>
 */
public interface StorageManager
{
	//XXX: should IOException be replaced by a StorageException?

	/**
	 * An object must know how dump itself to a file.
	 * Options are: Object serialization, XML, plain text
	 * @exception IOException
	 */
	public void dump() throws java.io.IOException;

	/**
	 * An object must know how retore its non-transient data structures from a file.
	 * Options are: Object serialization, XML, plain text
	 * @exception IOException
	 */
	public void restore() throws java.io.IOException;
}

// EOF
