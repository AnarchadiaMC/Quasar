/*
 * Copyright (c) 2024. Vili and contributors.
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 *  file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package org.anarchadia.quasar.api.event.events.client;

import org.anarchadia.quasar.api.event.EventCancellable;

public class KeyEvent extends EventCancellable {
    private final int key;
    private final int code;
    private final Status status;

    public KeyEvent(int key, int code, Status status) {
        this.key = key;
        this.code = code;
        this.status = status;
    }

    /**
     * Gets the key.
     *
     * @return key
     */
    public int getKey() {
        return key;
    }

    /**
     * Gets the key code.
     *
     * @return key code
     */
    public int getCode() {
        return code;
    }

    /**
     * Event enums.
     */
    public enum Status {
        PRESSED,
        RELEASED
    }
}
