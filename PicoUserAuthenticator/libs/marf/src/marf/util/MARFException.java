package marf.util;

import java.io.*;

/**
 * <p>Class MARFException</p>
 * <p>This class extends Exception for MARF specifics</p>
 *
 * <p>$Header: /cvsroot/marf/marf/src/marf/util/MARFException.java,v 1.7.2.1 2003/02/16 19:05:01 mokhov Exp $</p>
 */
public class MARFException extends Exception
{
	/**
	 * Error message container
	 */
	protected String strMessage;

	/**
	 * Generic exception
	 * @param pstrMessage Error message string
	 */
	public MARFException(String pstrMessage)
	{
		super(pstrMessage);
		this.strMessage = pstrMessage;
	}

	/**
	 * This is used for debug purposes only with some unusual Exception's.
	 * It allows the originiating Exceptions stack trace to be returned.
	 * @param pstrMessage Error message string
	 * @param poException Exception object to dump
	 */
	public MARFException(String pstrMessage, Exception poException)
	{
		super(pstrMessage);

	 	// Based on PostgreSQL JDBC driver's PGSQLException.
		try
		{
			ByteArrayOutputStream oByteArrayOutputStream = new ByteArrayOutputStream();
			PrintWriter oPrintWriter = new PrintWriter(oByteArrayOutputStream);

			oPrintWriter.println("Exception: " + poException.toString() + "\nStack Trace:\n");
			poException.printStackTrace(oPrintWriter);
			oPrintWriter.println("End of Stack Trace");

			strMessage += pstrMessage + " " + oByteArrayOutputStream.toString();

			oPrintWriter.println(strMessage);

			oPrintWriter.flush();
			oPrintWriter.close();

			oByteArrayOutputStream.close();
		}
		catch(Exception ioe)
		{
			strMessage +=
				pstrMessage + " " +
				poException.toString() +
				"\nIO Error on stack trace generation! " +
				ioe.toString();
		}
	}

	/**
	 * Returns string representation of the error message.
	 * @return error string
	 */
	public final String getMessage()
	{
		return this.strMessage;
	}
}

// EOF
