/*
 * Copyright (c) 2010 Ecole des Mines de Nantes.
 *
 *      This file is part of Entropy.
 *
 *      Entropy is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU Lesser General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *
 *      Entropy is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU Lesser General Public License for more details.
 *
 *      You should have received a copy of the GNU Lesser General Public License
 *      along with Entropy.  If not, see <http://www.gnu.org/licenses/>.
 */
package entropy.execution.driver;

/**
 * Exception related to the XenRpcClient.
 * @author Fabien Hermenier
 *
 */
public class XenClientException extends Exception {

	/**
	 * Default UID of this version.
	 */
	private static final long serialVersionUID = -4880194867698857569L;

	/**
	 * The error code.
	 */
	private String errorCode;
	
	/**
	 * The reason of the error.
	 */
	private String reason;
	
	/**
	 * The request.
	 */
	private String req;
	
	/**
	 * Make a new exception with an error message.
	 * @param request the request that create the exception
	 * @param code the error code
	 * @param explanation the reason of the error
	 */
	public XenClientException(String request, String code, String explanation) {
		this.errorCode = code;
		this.reason = explanation;
		this.req = request;
	}
	
	/**
	 * Get the request that make the exception. 
	 * @return the request as a String
	 */
	public String getRequest() {
		return this.req;
	}
	
	/**
	 * Get the reason of the error.
	 * @return the reason as a String
	 */
	public String getReason() {
		return this.reason;
	}
	
	/**
	 * Get the error code.
	 * @return the error code as a String
	 */
	public String getErrorCode() {
		return this.errorCode;
	}
	
	@Override
	public String getMessage() {
		return this.getErrorCode() + ":" + this.getReason();
	}
}
