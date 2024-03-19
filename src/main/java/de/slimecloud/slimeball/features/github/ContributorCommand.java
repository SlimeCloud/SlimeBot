package de.slimecloud.slimeball.features.github;

import de.mineking.discordutils.commands.ApplicationCommand;
import de.mineking.discordutils.commands.ApplicationCommandMethod;
import de.mineking.discordutils.commands.condition.IRegistrationCondition;
import de.mineking.discordutils.commands.condition.Scope;
import de.mineking.discordutils.commands.context.ICommandContext;
import de.mineking.discordutils.commands.option.Option;
import de.mineking.discordutils.events.Listener;
import de.mineking.discordutils.events.handlers.ButtonHandler;
import de.slimecloud.slimeball.config.GuildConfig;
import de.slimecloud.slimeball.main.SlimeBot;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;

@Slf4j
@ApplicationCommand(name = "contributor", description = "Bewerbe dich für die Contributor-Rolle wenn du am unserem Open Source Projekt mitgearbeitet hast", scope = Scope.GUILD_GLOBAL)
public class ContributorCommand {
	public final IRegistrationCondition<ICommandContext> condition = (manager, guild, cache) -> cache.<GuildConfig>getState("config").getLogChannel().isPresent();

	@ApplicationCommandMethod
	public void performCommand(@NotNull SlimeBot bot, @NotNull SlashCommandInteractionEvent event,
	                           @Option(description = "Wie heißt du auf GitHub?") String user,
	                           @Option(description = "Der GitHub link zu deinem PR") String link
	) {
		bot.loadGuild(event.getGuild()).getLogChannel().ifPresent(channel ->
				channel.sendMessage(link).addEmbeds(new EmbedBuilder()
						.setTitle("Neue Contributor Bewerbung")
						.setAuthor(event.getUser().getName(), null, event.getUser().getEffectiveAvatarUrl())
						.addField("GitHub Name", user, false)
						.addField("Discord Name", event.getUser().getAsMention(), false)
						.addField("Referenz", link, false)
						.setColor(bot.getColor(event.getGuild()))
						.build()
				).addActionRow(
						Button.success("contributor:accept", "Annehmen"),
						Button.danger("contributor:reject", "Ablehnen")
				).flatMap(x -> event.reply("Vielen dank für deine Mithilfe!\nUnser Team wird dies Prüfen und dir dann die Rolle zuweisen.").setEphemeral(true)).queue()
		);
	}

	@Listener(type = ButtonHandler.class, filter = "contributor:accept")
	public void handleAccept(@NotNull SlimeBot bot, @NotNull ButtonInteractionEvent event) {
		UserSnowflake user = SlimeBot.getUser(event.getMessage().getEmbeds().get(0));

		bot.loadGuild(event.getGuild()).getContributorRole().ifPresent(role -> {
			//Call event
			new ContributorAcceptedEvent(user, event.getMember()).callEvent();

			event.getGuild().addRoleToMember(user, role).reason("Hat am GitHub Projekt mitgearbeitet").queue();

			event.getMessage().delete().queue();
			event.getJDA().openPrivateChannelById(user.getIdLong())
					.flatMap(channel -> channel.sendMessage("Dir wurde die ContributorRolle auf dem SlimeCloud Discord gegeben."))
					.queue();

			event.reply(user.getAsMention() + " wurde die Contributor Rolle gegeben.").queue();
		});
	}

	@Listener(type = ButtonHandler.class, filter = "contributor:reject")
	public void handleDeny(@NotNull ButtonInteractionEvent event) {
		UserSnowflake user = SlimeBot.getUser(event.getMessage().getEmbeds().get(0));

		event.getMessage().delete().queue();
		event.getJDA().openPrivateChannelById(user.getIdLong())
				.flatMap(channel -> channel.sendMessage("Dir wurde die ContributorRolle auf dem SlimeCloud Discord leider **nicht** gegeben."))
				.queue();

		event.reply(user.getAsMention() + " wurde die Contributor Rolle nicht gegeben.").queue();
	}
}