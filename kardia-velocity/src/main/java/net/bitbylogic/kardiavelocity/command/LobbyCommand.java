package net.bitbylogic.kardiavelocity.command;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.bitbylogic.kardia.server.ServerType;
import net.bitbylogic.kardiavelocity.KardiaVelocity;
import net.bitbylogic.kardiavelocity.util.message.MessageUtil;

public class LobbyCommand implements SimpleCommand {

    @Override
    public void execute(Invocation invocation) {
        if(invocation.source() instanceof Player player) {
            ServerType serverType = KardiaVelocity.getInstance().getServerManager().serverType();

            if (serverType == null || serverType == ServerType.LOBBY) {
                player.sendMessage(MessageUtil.error("ʏᴏᴜ'ʀᴇ ᴀʟʀᴇᴀᴅʏ ɪɴ ᴛʜᴇ ʟᴏʙʙʏ!"));
                return;
            }

            KardiaVelocity.getInstance().getServerManager().connectPlayer((Player) invocation.source(), "stomarcade_lobby", false);
        }
    }

}
