/*
 * Copyright (c) 2024. Vili and contributors.
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 *  file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package org.anarchadia.quasar.impl.commands;

import org.anarchadia.quasar.Quasar;
import org.anarchadia.quasar.api.command.Command;
import org.anarchadia.quasar.api.util.QuasarLogger;

import java.util.stream.Collectors;

public class HelpCmd extends Command {

    public HelpCmd() {
        super("Help", "Shows a list of commands", "help", "h");
    }

    @Override
    public void onCommand(String[] args, String command) {
        if (args.length == 0) {
            QuasarLogger.info("Commands: " + Quasar.getInstance().getCommandManager().commands.stream()
                    .map(Command::getName).collect(Collectors.joining(", ")));
        } else {
            for (Command cmd : Quasar.getInstance().getCommandManager().commands) {
                if (cmd.getName().equalsIgnoreCase(args[0])) {
                    QuasarLogger.info(cmd.getSyntax());
                    return;
                }
            }
            QuasarLogger.error("Command not found.");
        }
    }
}
