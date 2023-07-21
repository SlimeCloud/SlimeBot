package com.slimebot.commands;

import com.slimebot.games.Game;
import com.slimebot.games.PlayerGameState;
import com.slimebot.games.games.Wordchain;
import de.mineking.discord.commands.annotated.ApplicationCommand;
import de.mineking.discord.commands.annotated.ApplicationCommandMethod;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

import java.time.Instant;


@ApplicationCommand(name = "wordchain", description = "Spiele das Wordchain minispiel mit anderen Leuten!")
public class WordchainCommand extends ListenerAdapter {
    @ApplicationCommandMethod
    public void performCommand(SlashCommandInteractionEvent event) {
        Long memberId = event.getMember().getIdLong();
        if(PlayerGameState.isInGame(memberId)) {
            event.reply("Du bist schon in einem Game!").setEphemeral(true).queue();
            return;
        }

        Game game = new Wordchain(memberId, event.getChannel(), event.getGuild().getIdLong());

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle(event.getMember().getEffectiveName() + "'s Wortkette: Einstellungen")
                .addField(":timer: Timeout", "Ändere die Zeit die man hat, um sich ein Wort zu überlegen", false)
                .addField(":x: Fehler Punkte", "Ändert die Anzahl der Fehler, die gemacht werden dürfen", false)
                .setTimestamp(Instant.now());

        event.replyEmbeds(embedBuilder.build())
                .addActionRow(
                        StringSelectMenu.create(game.uuid + ":settings:time")
                                .addOption("5", "5", Emoji.fromUnicode("U+23F2"))
                                .addOption("10", "10", Emoji.fromUnicode("U+23F2"))
                                .addOption("15", "15", Emoji.fromUnicode("U+23F2"))
                                .addOption("20", "20", Emoji.fromUnicode("U+23F2"))
                                .addOption("30", "30", Emoji.fromUnicode("U+23F2"))
                                .setRequiredRange(1, 1)
                                .setDefaultOptions(SelectOption.of("15", "15"))
                                .build()
                )
                .addActionRow(
                        StringSelectMenu.create(game.uuid + ":settings:lives")
                                .addOption("1", "1", Emoji.fromUnicode("U+274C"))
                                .addOption("2", "2", Emoji.fromUnicode("U+274C"))
                                .addOption("3", "3", Emoji.fromUnicode("U+274C"))
                                .addOption("4", "4", Emoji.fromUnicode("U+274C"))
                                .addOption("5", "5", Emoji.fromUnicode("U+274C"))
                                .setRequiredRange(1, 1)
                                .setDefaultOptions(SelectOption.of("3", "3"))
                                .build()
                )
                .addActionRow(
                        Button.success(game.uuid + ":settings:create", Emoji.fromUnicode("U+2705"))
                )
                .setEphemeral(true)
                .queue();
    }
}
