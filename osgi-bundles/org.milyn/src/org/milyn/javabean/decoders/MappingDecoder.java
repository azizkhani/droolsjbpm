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
import org.milyn.javabean.DataDecoder;
import org.milyn.config.Configurable;

import java.util.Properties;

/**
 * Mapping decoder.
 * <p/>
 * Decodes the supplied data by using it to lookup the decode mapping from the
 * decoders configuration parameters.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class MappingDecoder implements DataDecoder, Configurable {
    
    private Properties resourceConfig;
    private boolean strict = true;

    public void setConfiguration(Properties resourceConfig) {
        this.resourceConfig = resourceConfig;
        strict = resourceConfig.getProperty("strict", "true").equals("true");
    }

    public Object decode(String data) throws DataDecodeException {
        if(resourceConfig != null) {
            String mappingValue = resourceConfig.getProperty(data);

            if(mappingValue == null) {
                if(strict) {
                    throw new DataDecodeException("Mapping <param> for data '" + data + "' not defined.");
                } else {
                    return data;
                }
            }

            return mappingValue;
        } else {
            return data;
        }
    }

    public void setStrict(boolean strict) {
        this.strict = strict;
    }
}
