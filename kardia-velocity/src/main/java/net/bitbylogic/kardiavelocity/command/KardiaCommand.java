package net.bitbylogic.kardiavelocity.command;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.velocitypowered.api.command.SimpleCommand;
import net.bitbylogic.kardiavelocity.KardiaVelocity;
import net.bitbylogic.kardiavelocity.auth.KardiaAuth;
import net.bitbylogic.kardiavelocity.util.message.MessageUtil;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class KardiaCommand implements SimpleCommand {

    @Override
    public void execute(Invocation invocation) {
        var sender = invocation.source();

        Map<String, List<String>> serverGroupings = Maps.newConcurrentMap();

        KardiaVelocity.getInstance().getServerManager().getCachedServers().forEach(server -> {
            if (server.serverType() != null && server.instance() != null) {
                serverGroupings.computeIfAbsent(server.instance(),
                        k -> Lists.newCopyOnWriteArrayList())
                        .addAll(server.ids());
            }
        });

        sender.sendMessage(MessageUtil.primary("ᴋᴀʀᴅɪᴀ", "ꜱᴇʀᴠᴇʀ ɪɴꜰᴏ"));

        if (!serverGroupings.isEmpty()) {
            serverGroupings.forEach((instance, ids) -> {
                sender.sendMessage(MessageUtil.deserialize(
                        "<success_primary><instance> <separator>→ <success_highlight><servers> <success_secondary>server(s) available!",
                        Placeholder.unparsed("instance", instance.toUpperCase()), Placeholder.unparsed("servers", String.valueOf(ids.size()))
                ));

                List<String> distinctIds = ids.stream()
                        .distinct()
                        .collect(Collectors.toList());

                sender.sendMessage(MessageUtil.deserialize(" <separator>→ <success_primary>IDs <separator>[<success_highlight><ids><separator>]",
                        Placeholder.parsed("ids", Joiner.on("<separator>, <success_highlight>").join(distinctIds))));
            });
        } else {
            sender.sendMessage(MessageUtil.deserialize("<error_primary>No servers active."));
        }

        sender.sendMessage(MessageUtil.deserialize("<success_secondary>ᴛʜᴇʀᴇ ᴀʀᴇ ᴄᴜʀʀᴇɴᴛʟʏ <success_highlight><players> <success_secondary>ᴘʟᴀʏᴇʀ(ꜱ) ᴏɴʟɪɴᴇ.",
                Placeholder.unparsed("players", String.valueOf(KardiaAuth.getAuthedPlayers()))));
    }

}
