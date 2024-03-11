package de.slimecloud.slimeball.features.level.card;

import de.cyklon.jevent.EventHandler;
import de.cyklon.jevent.JEvent;
import de.slimecloud.slimeball.features.github.ContributorAcceptedEvent;
import de.slimecloud.slimeball.main.SlimeBot;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateBoostTimeEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class CardDecorationListener extends ListenerAdapter {
	private final SlimeBot bot;

	public CardDecorationListener(@NotNull SlimeBot bot) {
		this.bot = bot;
		JEvent.getDefaultManager().registerListener(this);
	}

	@Override
	public void onGuildMemberUpdateBoostTime(@NotNull GuildMemberUpdateBoostTimeEvent event) {
		if (event.getNewTimeBoosted() != null) bot.getCardDecorations().grantDecoration(event.getMember(), "booster.png");
		else bot.getCardDecorations().revokeDecoration(event.getMember(), "booster.png");
	}

	@EventHandler
	public void onContributor(@NotNull ContributorAcceptedEvent event) {
		bot.getCardDecorations().grantDecoration(event.getTeam().getGuild().getMember(event.getTarget()), "contributor.png");
	}
}
