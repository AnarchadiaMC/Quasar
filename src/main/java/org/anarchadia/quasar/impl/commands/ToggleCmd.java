/*
 * Copyright (c) 2024. Vili and contributors.
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 *  file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package org.anarchadia.quasar.impl.commands;

import org.anarchadia.quasar.Quasar;
import org.anarchadia.quasar.api.command.Command;
import org.anarchadia.quasar.api.module.Module;
import org.anarchadia.quasar.api.util.LoggingUtil;

public class ToggleCmd extends Command {

    public ToggleCmd() {
        super("Toggle", "Toggle a module.", "toggle <mod>", "t");
    }
    
    
    @Override
    public void onCommand(String[] args, String command) {
        if (args.length == 0) {
            LoggingUtil.error("Please specify a module.");
            return;
        }

        String moduleName = args[0];
        Module module = Quasar.getInstance().getModuleManager().getModule(String.valueOf(moduleName));

        if (module == null) {
            LoggingUtil.error("Module not found.");
            return;
        }

        module.toggle();
    }
}