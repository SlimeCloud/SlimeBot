package de.slimecloud.slimeball.features.general;

import de.mineking.discordutils.commands.ApplicationCommand;
import de.mineking.discordutils.commands.ApplicationCommandMethod;
import de.slimecloud.slimeball.main.SlimeBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

@ApplicationCommand(name = "ping", description = "ping pong", defer = true)
public class PingCommand {
	@ApplicationCommandMethod
	public void performCommand(@NotNull SlimeBot bot, @NotNull SlashCommandInteractionEvent event) {
		event.getJDA().getRestPing().flatMap(ping -> event.getHook().editOriginalEmbeds(new EmbedBuilder()
				.setTitle("Pong")
				.setColor(bot.getColor(event.getGuild()))
				.addField("Gateway", event.getJDA().getGatewayPing() + "ms", true)
				.addField("Rest", String.valueOf(ping), true)
				.build()
		)).queue();
	}
}
