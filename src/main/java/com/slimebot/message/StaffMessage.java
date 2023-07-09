package com.slimebot.message;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.simpleyaml.configuration.ConfigurationSection;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StaffMessage extends ListenerAdapter {
    @Override
    public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent event) {
        updateMessage(event.getGuild(), event.getRoles());
    }

    @Override
    public void onGuildMemberRoleRemove(GuildMemberRoleRemoveEvent event) {
        updateMessage(event.getGuild(), event.getRoles());
    }

    @Override
    public void onReady(ReadyEvent event) {
        for(Guild guild : event.getJDA().getGuilds()) {
            updateMessage(guild, getConfig(guild.getId()));
        }
    }

    private void updateMessage(Guild guild, List<Role> roles) {
        YamlFile config = getConfig(guild.getId());
        List<Long> roleIDs = getRoleIDs(config);
        if (roles.stream().noneMatch(role -> roleIDs.contains(role.getIdLong()))) {
            return;
        }
        updateMessage(guild, config);
    }

    private void updateMessage(Guild guild, YamlFile config){
        String message = buildMessage(config, guild);
        TextChannel channel = guild.getTextChannelById(config.getLong("channelID"));
        assert channel != null;
        if (config.getInt("messageID") == -1) {
            channel.sendMessage(message).queue(message1 -> {
                config.set("messageID", message1.getIdLong());
                try {
                    config.save();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } else {
            channel.editMessageById(config.getLong("messageID"), message).queue();
        }
    }

    private YamlFile getConfig(String guildId) {
        YamlFile config = new YamlFile("Slimebot/" + guildId + "/staffMessage.yml");
        if (!config.exists()) {
            try {
                createConfig(config);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            config.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return config;
    }

    private void createConfig(YamlFile config) {
        try {
            config.createNewFile(true);
            config.set("roles.123456.description", "This is a description");
            config.set("messageID", 123456);
            config.set("channelID", 123456);
            config.setComment("messageID", "The ID of the message that should be edited. Set to -1 if you want to create a new message");
            config.set("roles.premessage", """
                    🪐Team Vorstellung🪐
                    
                    *Im Im folgenden werden die Teammitglieder im Zusammenhang mit ihren Rollen vorgestellt. Bei Bedarf und Situation werden diese unangekündigt verändert!*""");
            config.setComment("message", "Is shown before the roles");
            config.save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<Long> getRoleIDs(YamlFile config) {
        ConfigurationSection section = config.getConfigurationSection("roles");
        List<Long> roleIds=new ArrayList<>();
        for (String key : section.getKeys(false)) {
            try {
                Long.parseLong(key);
            } catch (NumberFormatException e) {
                continue;
            }
            roleIds.add(Long.parseLong(key));
        }
        return roleIds;
    }

    private String buildMessage(YamlFile config, Guild guild) {
        ConfigurationSection section = config.getConfigurationSection("roles");
        StringBuilder builder = new StringBuilder();
        for (String key : section.getKeys(false)) {
            try {
                Long.parseLong(key);
            } catch (NumberFormatException e) {
                builder.append(section.getString(key)).append("\n\n");
                continue;
            }
            Role role = guild.getRoleById(key);
            assert role != null;
            List<Member> members = guild.getMembersWithRoles(role);
            if (members.isEmpty()) {
                continue;
            }
            builder.append(role.getAsMention()).append(" *").append(section.getString(key + ".description")).append("*\n");
            for (Member member : members) {
                builder.append("> ").append(member.getAsMention()).append("\n");
            }
            builder.append("\n");
        }
        return builder.toString();
    }
}
