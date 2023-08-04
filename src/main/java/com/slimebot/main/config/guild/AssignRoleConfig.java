package com.slimebot.main.config.guild;

import com.slimebot.commands.config.engine.ConfigField;
import com.slimebot.commands.config.engine.ConfigFieldType;
import net.dv8tion.jda.api.entities.Role;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class AssignRoleConfig {
    @ConfigField(type = ConfigFieldType.ROLE_LIST, command = "role", title = "Assign Role", description = "Diese Rolle wird jedem joinenden Member gegeben")
    public List<Long> role = new ArrayList<>();

    public Optional<List<Role>> getRoles() {
        return GuildConfig.getRoles(role);
    }
}
