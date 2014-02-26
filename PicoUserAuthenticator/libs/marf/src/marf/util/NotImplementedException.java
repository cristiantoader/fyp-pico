package marf.util;

/**
 * <p>Class NotImplementedException</p>
 * <p>This class extends RuntimeException for MARF unimplemented parts</p>
 *
 * <p>$Header: /cvsroot/marf/marf/src/marf/util/NotImplementedException.java,v 1.4.4.1 2003/02/16 19:05:01 mokhov Exp $</p>
 */
public class NotImplementedException extends RuntimeException
{
	/**
	 * Generic exception
	 * @param pstrMessage Error message string
	 */
	public NotImplementedException(String pstrMessage)
	{
		super("Not implemented: " + pstrMessage);
	}
}

// EOF
