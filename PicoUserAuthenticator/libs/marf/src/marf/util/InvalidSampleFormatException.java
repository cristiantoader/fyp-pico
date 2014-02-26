package marf.util;

/**
 * <p>Class InvalidSampleFormatException</p>
 * <p>$Header: /cvsroot/marf/marf/src/marf/util/InvalidSampleFormatException.java,v 1.2.4.1 2003/02/16 19:05:01 mokhov Exp $</p>
 */
public class InvalidSampleFormatException extends Exception
{
	/**
	 * Exception for specific sample format
	 * @param piFormat Format number, caused the exception to be thrown
	 */
	public InvalidSampleFormatException(int piFormat)
	{
		super("Invalid sample file format: " + piFormat);
	}

	/**
	 * Generic exception
	 * @param pstrMsg error message only
	 */
	public InvalidSampleFormatException(String pstrMsg)
	{
		super(pstrMsg);
	}
}

// EOF
