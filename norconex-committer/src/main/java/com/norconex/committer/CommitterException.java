/* Copyright 2010-2013 Norconex Inc.
 * 
 * This file is part of Norconex Committer.
 * 
 * Norconex Committer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Norconex Committer is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Norconex Committer. If not, see <http://www.gnu.org/licenses/>.
 */
package com.norconex.committer;

/**
 * Triggered when something went wrong with committing.
 * @author Pascal Essiembre
 */
public class CommitterException extends RuntimeException {

    private static final long serialVersionUID = -805913995358009121L;

    /**
     * Constructor.
     */
    public CommitterException() {
        super();
    }
    /**
     * Constructor.
     * @param message error message
     */
    public CommitterException(String message) {
        super(message);
    }
    /**
     * Constructor.
     * @param cause original exception
     */
    public CommitterException(Throwable cause) {
        super(cause);
    }
    /**
     * Constructor.
     * @param message error message.
     * @param cause original exception
     */
    public CommitterException(String message, Throwable cause) {
        super(message, cause);
    }

}
