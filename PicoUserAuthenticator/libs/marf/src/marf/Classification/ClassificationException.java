package marf.Classification;

/**
 * <p>Class ClassificationException</p>
 * <p>$Header: /cvsroot/marf/marf/src/marf/Classification/ClassificationException.java,v 1.3.4.1 2003/02/16 18:08:53 mokhov Exp $</p>
 */
public class ClassificationException extends Exception
{
	/**
	 * Generic exception
	 * @param pstrMessage Error message string
	 */
	public ClassificationException(String pstrMessage)
	{
		super(pstrMessage);
	}
}

// EOF
