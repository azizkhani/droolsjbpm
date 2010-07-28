/**
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package bitronix.tm.resource.common;

/**
 * {@link XAStatefulHolder} state change listener interface.
 * <p>&copy; <a href="http://www.bitronix.be">Bitronix Software</a></p>
 *
 * @author lorban
 */
public interface StateChangeListener {

    /**
     * Fired when the internal state of a {@link XAStatefulHolder} has changed.
     * @param source the {@link XAStatefulHolder} changing state.
     * @param oldState the old state of the {@link XAStatefulHolder}.
     * @param newState the new state of the {@link XAStatefulHolder}.
     */
    public void stateChanged(XAStatefulHolder source, int oldState, int newState);

    /**
     * Fired before the internal state of a {@link XAStatefulHolder} has changed.
     * @param source the {@link XAStatefulHolder} changing state.
     * @param currentState the current state of the {@link XAStatefulHolder}.
     * @param futureState the future state of the {@link XAStatefulHolder}.
     */
    public void stateChanging(XAStatefulHolder source, int currentState, int futureState);

}
