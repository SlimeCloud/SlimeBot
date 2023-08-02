package com.slimebot.commands.config;

import com.slimebot.level.Level;
import com.slimebot.main.Main;
import de.mineking.discord.commands.annotated.ApplicationCommand;
import de.mineking.discord.commands.annotated.ApplicationCommandMethod;
import de.mineking.discord.commands.annotated.option.Option;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class LevelConfigCommand {
	@ApplicationCommand(name = "add_role", description = "Fügt eine Level-Rolle hinzu")
	public static class AddRoleCommand {
		@ApplicationCommandMethod
		public void performCommand(SlashCommandInteractionEvent event,
		                           @Option(name = "level", description = "Das Level") int level,
		                           @Option(name = "rolle", description = "Die Rolle, die vergeben wird") Role role
		) {
			ConfigCommand.updateField(event.getGuild(), config -> config.getOrCreateLevel().levelRoles.put(level, role.getIdLong()));

			Level.getLevels(event.getGuild()).forEach(Level::updateRoles);

			event.reply(role.getAsMention() + " als Level-Rolle für " + level + " festgelegt").setEphemeral(true).queue();
		}
	}

	@ApplicationCommand(name = "remove_role", description = "Entfernt eine Level-Rolle")
	public static class RemoveRoleCommand {
		@ApplicationCommandMethod
		public void performCommand(SlashCommandInteractionEvent event,
		                           @Option(name = "level", description = "Das Level") int level
		) {
			ConfigCommand.updateField(event.getGuild(), config -> config.getOrCreateLevel().levelRoles.remove(level));

			Level.getLevels(event.getGuild()).forEach(Level::updateRoles);

			event.reply("Rolle für Level " + level + " entfernt").setEphemeral(true).queue();
		}
	}

	@ApplicationCommand(name = "blacklist_channel", description = "Deaktiviert das Leveling für einen Kanal")
	public static class BlacklistChannelCommand {

		@ApplicationCommandMethod
		public void performCommand(SlashCommandInteractionEvent event, @Option(name = "channel", description = "Der Kanal") GuildChannel channel) {
			ConfigCommand.updateField(event.getGuild(), config -> config.getOrCreateLevel().blacklist.add(channel.getIdLong()));

			event.reply("Kanal " + channel.getAsMention() + " zur Level Blacklist hinzugefügt").setEphemeral(true).queue();
		}

	}

	@ApplicationCommand(name = "un_blacklist_channel", description = "Aktiviere das Leveling für einen Kanal")
	public static class UnblacklistChannelCommand {

		@ApplicationCommandMethod
		public void performCommand(SlashCommandInteractionEvent event, @Option(name = "channel", description = "Der Kanal") GuildChannel channel) {
			ConfigCommand.updateField(event.getGuild(), config -> config.getOrCreateLevel().blacklist.remove(channel.getIdLong()));

			event.reply("Kanal " + channel.getAsMention() + " von der Level Blacklist entfernt").setEphemeral(true).queue();
		}

	}
}
