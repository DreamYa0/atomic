/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * <p>
 * Copyright 2012-2016 the original author or authors.
 */
package com.atomic.exception;

/**
 * Exception during the assertion (for example : when getting the data in the database, or accessing to file system).
 *
 * @author Régis Pouiller
 *
 */
public class AssertJDBException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AssertJDBException(Exception exception) {
        super(exception);
    }

    /**
     * @param message 消息的异常
     * @param objects 消息的参数
     */
    public AssertJDBException(String message, Object... objects) {
        super(String.format(message, objects));
    }
}
