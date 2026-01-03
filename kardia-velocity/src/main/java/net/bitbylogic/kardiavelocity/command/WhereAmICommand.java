package net.bitbylogic.kardiavelocity.command;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.bitbylogic.kardiavelocity.util.message.MessageUtil;
import net.kyori.adventure.text.Component;

public class WhereAmICommand implements SimpleCommand {

    @Override
    public void execute(Invocation invocation) {
        if(invocation.source() instanceof Player sender) {
            sender.sendMessage(MessageUtil.deserialize("<secondary>ʏᴏᴜ'ʀᴇ ᴄᴜʀʀᴇɴᴛʟʏ ᴏɴ ꜱᴇʀᴠᴇʀ <highlight>"
                    + sender.getCurrentServer().get().getServerInfo().getName()));
        }
    }

}
