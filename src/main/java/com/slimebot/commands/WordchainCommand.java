package com.slimebot.commands;

import com.slimebot.games.GamePlayer;
import com.slimebot.games.wordchain.Wordchain;
import com.slimebot.main.Main;
import de.mineking.discord.commands.annotated.ApplicationCommand;
import de.mineking.discord.commands.annotated.ApplicationCommandMethod;
import de.mineking.discord.ui.CallbackState;
import de.mineking.discord.ui.components.button.ButtonColor;
import de.mineking.discord.ui.components.button.ButtonComponent;
import de.mineking.discord.ui.components.select.StringSelectComponent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;


@ApplicationCommand(name = "wordchain", description = "Spiele das Wordchain Minispiel mit anderen Leuten!", guildOnly = true)
public class WordchainCommand {
    @ApplicationCommandMethod
    public void performCommand(SlashCommandInteractionEvent event) {
        if (GamePlayer.isInGame(event.getMember().getIdLong())) {
            event.reply("Du bist schon in einem Game!").setEphemeral(true).queue();
            return;
        }
        AtomicInteger time = new AtomicInteger(15);
        AtomicInteger lives = new AtomicInteger(3);

        Main.discordUtils.getUIManager().createMenu()
                .addMessageFrame("main", () ->
                                new EmbedBuilder()
                                        .setTitle(event.getMember().getEffectiveName() + "'s Wortkette: Einstellungen")
                                        .addField(":timer: Timeout", "Ändere die Zeit die man hat, um sich ein Wort zu überlegen", false)
                                        .addField(":x: Fehler Punkte", "Ändert die Anzahl der Fehler, die gemacht werden dürfen", false)
                                        .setTimestamp(Instant.now())
                                        .build(),
                        frame -> frame.addComponents(
                                new StringSelectComponent("time", menu -> menu
                                        .addOption("5", "5", Emoji.fromUnicode("U+23F2"))
                                        .addOption("10", "10", Emoji.fromUnicode("U+23F2"))
                                        .addOption("15", "15", Emoji.fromUnicode("U+23F2"))
                                        .addOption("20", "20", Emoji.fromUnicode("U+23F2"))
                                        .addOption("30", "30", Emoji.fromUnicode("U+23F2"))
                                        .setRequiredRange(1, 1)
                                        .setDefaultOptions(SelectOption.of("15", "15"))
                                ).handle((menu, evt) -> {
                                    evt.deferEdit().queue();
                                    time.set(Integer.valueOf(evt.getInteraction().getSelectedOptions().get(0).getValue()));
                                }),
                                new StringSelectComponent("lives", menu -> menu
                                        .addOption("1", "1", Emoji.fromUnicode("U+274C"))
                                        .addOption("2", "2", Emoji.fromUnicode("U+274C"))
                                        .addOption("3", "3", Emoji.fromUnicode("U+274C"))
                                        .addOption("4", "4", Emoji.fromUnicode("U+274C"))
                                        .addOption("5", "5", Emoji.fromUnicode("U+274C"))
                                        .setRequiredRange(1, 1)
                                        .setDefaultOptions(SelectOption.of("3", "3"))
                                ).handle((menu, evt) -> {
                                    evt.deferEdit().queue();
                                    lives.set(Integer.valueOf(evt.getInteraction().getSelectedOptions().get(0).getValue()));
                                }),
                                new ButtonComponent("create", ButtonColor.GREEN, "Erstellen").handle((menu, evt) -> {
                                    if (GamePlayer.isInGame(event.getMember().getIdLong())) {
                                        event.reply("Du bist schon in einem Game!").setEphemeral(true).queue();
                                        return;
                                    }
                                    menu.close();
                                    event.getChannel().sendMessage(event.getMember().getAsMention() + " hat ein neues Wortketten Minispiel erstellt!").queue(message -> {
                                        message.createThreadChannel("Wordchain").queue(channel -> {
                                            new Wordchain(event.getMember().getIdLong(), channel.getIdLong(), event.getGuild().getIdLong(), time.shortValue(), lives.shortValue());
                                        });
                                    });
                                })
                        )
                )
                .start(new CallbackState(event), "main");
    }
}
