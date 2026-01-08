package net.bitbylogic.kardiavelocity.command;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.bitbylogic.kardiavelocity.util.message.MessageUtil;
import net.bitbylogic.utils.smallcaps.SmallCapsConverter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

public class WhereAmICommand implements SimpleCommand {

    @Override
    public void execute(Invocation invocation) {
        if(invocation.source() instanceof Player sender) {
            sender.sendMessage(MessageUtil.deserialize("<secondary>ʏᴏᴜ'ʀᴇ ᴄᴜʀʀᴇɴᴛʟʏ ᴏɴ ꜱᴇʀᴠᴇʀ <highlight><server>", Placeholder.parsed("server", SmallCapsConverter.convert(sender.getCurrentServer().get().getServerInfo().getName()))));
        }
    }

}
