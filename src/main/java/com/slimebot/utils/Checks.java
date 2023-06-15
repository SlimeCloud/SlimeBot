package com.slimebot.utils;


import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.IOException;

public class Checks {

    public static Boolean hasTeamRole(Member member, Guild guild){
        YamlFile config = Config.getConfig(guild.getId(), "mainConfig");
        try {
            config.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Role staffRole = guild.getRoleById(config.getString("staffRoleID"));
        return !(member.getRoles().contains(staffRole));
    }


}
