/*
	Milyn - Copyright (C) 2006

	This library is free software; you can redistribute it and/or
	modify it under the terms of the GNU Lesser General Public
	License (version 2.1) as published by the Free Software
	Foundation.

	This library is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

	See the GNU Lesser General Public License for more details:
	http://www.gnu.org/licenses/lgpl.txt
*/
package org.milyn.javabean.decoders;

import org.milyn.javabean.DataDecodeException;
import org.milyn.javabean.DecodeType;
import java.util.Date;
import java.sql.Time;

/**
 * {@link java.sql.Time} data decoder.
 * <p/>
 * Extends {@link org.milyn.javabean.decoders.DateDecoder} and returns
 * a java.sql.Time instance.
 * <p/>
 *
 * @author <a href="mailto:daniel.bevenius@gmail.com">daniel.bevenius@gmail.com</a>
 */
@DecodeType(Time.class)
public class SqlTimeDecoder extends DateDecoder {

    public Object decode(String data) throws DataDecodeException {
    	Date date = (Date)super.decode(data);
        return new Time(date.getTime());
    }
}
