package com.slimebot.utils;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

public class Checks {

    public static Boolean hasTeamRole(Member member, Guild guild){
        Role staffRole = guild.getRoleById("1081650648124248124");  //ToDo get ID from a Config eg. Settings
                                                                    //MainID: 1077266943003865188
                                                                    //TestID: 1081650648124248124
        return !(member.getRoles().contains(staffRole));
    }


}
