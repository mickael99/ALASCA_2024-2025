package fr.sorbonne_u.components.exceptions;

// Copyright Jacques Malenfant, Sorbonne Universite.
// Jacques.Malenfant@lip6.fr
//
// This software is a computer program whose purpose is to provide a
// new implementation of the DEVS simulation standard for Java.
//
// This software is governed by the CeCILL-C license under French law and
// abiding by the rules of distribution of free software.  You can use,
// modify and/ or redistribute the software under the terms of the
// CeCILL-C license as circulated by CEA, CNRS and INRIA at the following
// URL "http://www.cecill.info".
//
// As a counterpart to the access to the source code and  rights to copy,
// modify and redistribute granted by the license, users are provided only
// with a limited warranty  and the software's author,  the holder of the
// economic rights,  and the successive licensors  have only  limited
// liability. 
//
// In this respect, the user's attention is drawn to the risks associated
// with loading,  using,  modifying and/or developing or reproducing the
// software by the user in light of its specific status of free software,
// that may mean  that it is complicated to manipulate,  and  that  also
// therefore means  that it is reserved for developers  and  experienced
// professionals having in-depth computer knowledge. Users are therefore
// encouraged to load and test the software's suitability as regards their
// requirements in conditions enabling the security of their systems and/or 
// data to be ensured and,  more generally, to use and operate it in the 
// same conditions as regards security. 
//
// The fact that you are presently reading this means that you have had
// knowledge of the CeCILL-C license and that you accept its terms.

// -----------------------------------------------------------------------------
/**
 * The class <code>NotProvidedOperationException</code> defines the exception
 * thrown when equipments do not implement some operation appearing in the
 * equipment control component interfaces.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>White-box Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p><strong>Black-box Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p>Created on : 2023-09-15</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class		NotProvidedOperationException
extends		Exception
{
	private static final long serialVersionUID = 1L;

	/**
	 * @see java.lang.Exception#Exception()
	 */
	public			NotProvidedOperationException()
	{
		super();
	}

	/**
    * @param message   the detail message. The detail message is saved for later retrieval by the {@link #getMessage()} method.
   	 * @see java.lang.Exception#Exception(String)
	 */
	public			NotProvidedOperationException(String message)
	{
		super(message);
	}

	/**
     * @param cause	the cause (which is saved for later retrieval by the {@link #getCause()} method).  (A <tt>null</tt> value is permitted, and indicates that the cause is nonexistent or unknown.)
	 * @see java.lang.Exception#Exception(Throwable)
	 */
	public			NotProvidedOperationException(Throwable cause)
	{
		super(cause);
	}

	/**
     * @param message	the detail message (which is saved for later retrieval by the {@link #getMessage()} method).
     * @param cause		the cause (which is saved for later retrieval by the {@link #getCause()} method).  (A <tt>null</tt> value is permitted, and indicates that the cause is nonexistent or unknown.)
	 * @see java.lang.Exception#Exception(String, Throwable)
	 */
	public			NotProvidedOperationException(
		String message,
		Throwable cause
		)
	{
		super(message, cause);
	}

	/**
     * @param message				the detail message.
     * @param cause					the cause.  (A {@code null} value is permitted, and indicates that the cause is nonexistent or unknown.)
     * @param enableSuppression		whether or not suppression is enabled or disabled
     * @param writableStackTrace	whether or not the stack trace should be writable
	 * @see java.lang.Exception#Exception(String, Throwable, boolean, boolean)
	 */
	public			NotProvidedOperationException(
		String message,
		Throwable cause,
		boolean enableSuppression,
		boolean writableStackTrace
		)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
// -----------------------------------------------------------------------------
