package net.bitbylogic.kardiavelocity.command;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.bitbylogic.kardiavelocity.KardiaVelocity;
import net.bitbylogic.kardiavelocity.util.message.MessageUtil;

public class LobbyCommand implements SimpleCommand {

    @Override
    public void execute(Invocation invocation) {
        if(invocation.source() instanceof Player player) {
            String currentServer = player.getCurrentServer()
                    .map(sc -> sc.getServerInfo().getName())
                    .orElse(null);

            boolean inLobby = currentServer != null &&
                    KardiaVelocity.getInstance()
                            .getServerManager()
                            .getServersByInstance("lobby")
                            .stream()
                            .anyMatch(server -> server.kardiaId().equalsIgnoreCase(currentServer));

            if (inLobby) {
                player.sendMessage(MessageUtil.error("ʏᴏᴜ'ʀᴇ ᴀʟʀᴇᴀᴅʏ ɪɴ ᴛʜᴇ ʟᴏʙʙʏ!"));
                return;
            }

            KardiaVelocity.getInstance().getServerManager().connectPlayer((Player) invocation.source(), "lobby", false);
        }
    }

}
