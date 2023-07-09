package com.slimebot.events;

import com.slimebot.utils.Config;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class OnJoin extends ListenerAdapter {
	@Override
	public void onGuildJoin(GuildJoinEvent event) {
		Config.createMain(event.getGuild().getId());
	}
}
