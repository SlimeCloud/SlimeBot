package com.slimebot.main.config.guild;

import com.slimebot.commands.config.engine.ConfigField;
import com.slimebot.commands.config.engine.ConfigFieldType;
import net.dv8tion.jda.api.entities.Role;

import java.util.Optional;


public class AssignRoleConfig {
    @ConfigField(type = ConfigFieldType.ROLE, command = "role", title = "Assign Role", description = "Diese Rolle wird jedem joinenden Member gegeben")
    public Long role;

    public Optional<Role> getRole() {
        return GuildConfig.getRole(role);
    }
}
