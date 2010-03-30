/*
 *-----------------------------------------------------------------------------*
 *                            Copyright � 2010 by                              *
 *                    STABILIT Informatik AG, Switzerland                      *
 *                            ALL RIGHTS RESERVED                              *
 *                                                                             *
 * Valid license from STABILIT is required for possession, use or copying.     *
 * This software or any other copies thereof may not be provided or otherwise  *
 * made available to any other person. No title to and ownership of the        *
 * software is hereby transferred. The information in this software is subject *
 * to change without notice and should not be construed as a commitment by     *
 * STABILIT Informatik AG.                                                     *
 *                                                                             *
 * All referenced products are trademarks of their respective owners.          *
 *-----------------------------------------------------------------------------*
*/
/**
 * 
 */
package com.stabilit.sc.srv.cmd;

import com.stabilit.sc.common.io.IRequest;
import com.stabilit.sc.common.io.IResponse;

/**
 * @author JTraber
 *
 */
public class NullCommandValidator implements ICommandValidator {

	private static ICommandValidator nullCommandValidator = new NullCommandValidator();
		
	public static ICommandValidator newInstance() {
		return nullCommandValidator;
	}

	private NullCommandValidator() {
	}
	
	@Override
	public void validate(IRequest request, IResponse resonse) throws ValidatorException {
        throw new ValidatorException("no validator implemented");
	}


}
