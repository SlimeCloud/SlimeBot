package com.slimebot.commands.config.engine;

import com.slimebot.main.config.guild.GuildConfig;

public interface InstanceProvider {
	Object getInstance(boolean create, GuildConfig config) throws Exception;
}
