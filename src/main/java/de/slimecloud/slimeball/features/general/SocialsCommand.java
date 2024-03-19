package de.slimecloud.slimeball.features.general;


import de.mineking.discordutils.commands.ApplicationCommand;
import de.mineking.discordutils.commands.ApplicationCommandMethod;
import de.mineking.discordutils.commands.condition.Scope;
import de.slimecloud.slimeball.main.SlimeBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

@ApplicationCommand(name = "socials", description = "Socials von Miká und Nico", scope = Scope.GUILD_GLOBAL)
public class SocialsCommand {

	@ApplicationCommandMethod
	public void performCommand(@NotNull SlimeBot bot, @NotNull SlashCommandInteractionEvent event) {
		event.replyEmbeds(new EmbedBuilder()
				.setTitle("📱 Social-Media präsenz")
				.addField("__**gamingguidesde**__",
						"- **YouTube:** [@gamingguidesde](https://www.youtube.com/@gamingguidesde)\n" +
								"- **Twitch:** [/gamingguidesde](https://twitch.tv/gamingguidesde)\n" +
								"- **Instagram:** [@gamingguidesde](https://www.instagram.com/gamingguidesde/)\n" +
								"- **TikTok:** [@sirggde](https://www.tiktok.com/@sirggde)\n" +
								"- **X (Twitter):** [@gamingguidesde](https://x.com/gamingguidesde)\n" +
								"- **Labymod:** [@gamingguidesde](https://laby.net/@gamingguidesde)\n",
						false)
				.addField("__**Vegastep**__",
						"- **Spotify:** [Vegastep](https://open.spotify.com/artist/0ZzsW7JiW4Ok3H7nFl4yV1)\n" +
								"- **Twitch:** [/vegastep](https://www.twitch.tv/vegastep)\n" +
								"- **Instagram:** [@vegastep](https://www.instagram.com/vegastep/)\n" +
								"- **YouTube:** [@Vegastep](https://www.youtube.com/@Vegastep)\n" +
								"- **Soundcloud:** [vegastep](https://soundcloud.com/vegastep)\n",
						false)
				.addField("__**Slimecast**__",
						"- **Spotify:** [Slimecast](https://open.spotify.com/show/0HNYFHg2WNq1P56qK6defn)\n" +
								"- **Instagram:** [@slimecastde](https://instagram.com/slimecastde)\n" +
								"- **X (Twitter):** [ @slimecastde](https://x.com/slimecastde)\n",
						false)
				.setColor(bot.getColor(event.getGuild()))
				.setThumbnail(event.getGuild().getIconUrl())
				.build()).setEphemeral(true).queue();
	}
}
