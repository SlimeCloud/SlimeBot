package com.slimebot.commands;

import com.slimebot.main.SlimeEmoji;
import com.slimebot.main.config.guild.FdmdsConfig;
import com.slimebot.main.config.guild.GuildConfig;
import de.mineking.discord.DiscordUtils;
import de.mineking.discord.commands.annotated.ApplicationCommand;
import de.mineking.discord.commands.annotated.ApplicationCommandMethod;
import de.mineking.discord.events.Listener;
import de.mineking.discord.events.interaction.ButtonHandler;
import de.mineking.discord.events.interaction.ModalHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IModalCallback;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;

@ApplicationCommand(name = "fdmds", description = "Schlage eine Frage für \"Frag doch mal den Schleim\" vor!", feature = "fdmds")
//feature commands are guild-only
public class FdmdsCommand {
    @Listener(type = ButtonHandler.class, filter = "fdmds:create")
    @ApplicationCommandMethod
    public void sendModal(IModalCallback event, String question, String choices) { //Non-@Option parameters will be null when called from DiscordUtils CommandHandler
        event.replyModal(
                Modal.create("fdmds:" + (question == null ? "send" : "edit"), "Schlage eine fdmds Frage vor")
                        .addActionRow(
                                TextInput.create("question", "Deine Frage", TextInputStyle.SHORT)
                                        .setPlaceholder("Welche Eissorte mögt ihr am liebsten?")
                                        .setValue(question)
                                        .setMinLength(10)
                                        .setMaxLength(150)
                                        .setRequired(true)
                                        .build()
                        )
                        .addActionRow(
                                TextInput.create("choices", "Deine Antwortmöglichkeiten", TextInputStyle.PARAGRAPH)
                                        .setPlaceholder("Jede Antwortmöglichkeit in einer neuen Zeile, z.B:\nErdbeere\nCookie\nSchokolade")
                                        .setValue(choices)
                                        .setMinLength(10)
                                        .setMaxLength(800)
                                        .setRequired(true)
                                        .build()
                        )
                        .build()
        ).queue();
    }

    @Listener(type = ModalHandler.class, filter = "fdmds:(.*)")
    public void handleFdmdsModal(ModalInteractionEvent event) {
        String question = event.getValue("question").getAsString();

        StringBuilder choicesStr = new StringBuilder();

        if (event.getModalId().contains("send")) {
            String[] choices = event.getValue("choices").getAsString().split("\n");

            if (choices.length <= 1) {
                event.reply("Du musst **mindestens 2** Antwortmöglichkeiten angeben!\n**Achte darauf jede Antwortmöglichkeit in eine neue Zeile zu schreiben!**").setEphemeral(true).queue();
                return;
            }

            if (choices.length > 9) {
                event.reply("Du kannst **maximal 9** Antwortmöglichkeiten angeben!").setEphemeral(true).queue();
                return;
            }

            for (int i = 0; i < choices.length; i++) {
                choicesStr
                        .append(SlimeEmoji.fromId(i + 1).format())
                        .append(" -> ")
                        .append(choices[i].strip())
                        .append("\r\n");
            }
        } else {
            choicesStr.append(event.getValue("choices").getAsString());
        }

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setColor(GuildConfig.getColor(event.getGuild()))
                .setTitle("Frag doch mal den Schleim")
                .setFooter("Vorschlag von: " + event.getUser().getGlobalName() + " (" + event.getUser().getId() + ")");

        if (event.getModalId().contains("edit")) {
            embedBuilder.setFooter(event.getMessage().getEmbeds().get(0).getFooter().getText())
                    .addField("Frage:", question, false);
        } else if (event.getModalId().contains("send")) {
            embedBuilder.addField("Frage:", "Heute würde ich gerne von euch wissen, " + question.split(" ", 2)[0].toLowerCase() + " " + question.split(" ", 2)[1], false);
        }
        embedBuilder.addField("Auswahlmöglichkeiten:", choicesStr.toString(), false);

        MessageEditBuilder message = new MessageEditBuilder()
                .setActionRow(
                        Button.secondary("fdmds.edit", "Bearbeiten"),
                        Button.danger("fdmds.send", "Senden")
                )
                .setEmbeds(embedBuilder.build());

        if (event.getModalId().contains("edit")) {
            message.setContent("Bearbeitet von " + event.getMember().getAsMention());
            event.getMessage().editMessage(message.build()).queue();

            event.reply("Frage wurde bearbeitet.").setEphemeral(true).queue();
        } else {
            GuildConfig.getConfig(event.getGuild()).getFdmds().flatMap(FdmdsConfig::getLogChannel).ifPresentOrElse(
                    channel -> {
                        channel.sendMessage(MessageCreateData.fromEditData(message.build())).queue();
                        event.reply("Frage erfolgreich eingereicht! Das Team wird die Frage kontrollieren und anschließend veröffentlicht.").setEphemeral(true).queue();
                    },
                    () -> event.reply("Error: Channel wurde nicht gesetzt!").setEphemeral(true).queue()
            );
        }
    }

    @Listener(type = ButtonHandler.class, filter = "fdmds.edit")
    public void editFdmds(ButtonInteractionEvent event) {
        MessageEmbed embed = event.getMessage().getEmbeds().get(0);
        sendModal(event, embed.getFields().get(0).getValue(), embed.getFields().get(1).getValue());
    }

    @Listener(type = ButtonHandler.class, filter = "fdmds.send")
    public void sendFdmds(DiscordUtils manager, ButtonInteractionEvent event) {
        GuildConfig.getConfig(event.getGuild()).getFdmds().ifPresent(fdmds ->
                fdmds.getChannel().ifPresentOrElse(
                        channel -> {

                            MessageEmbed embed = event.getMessage().getEmbeds().get(0);
                            String question = embed.getFields().get(0).getValue();
                            String choices = embed.getFields().get(1).getValue();
                            String footerText = embed.getFooter().getText();
                            Member requester = event.getGuild().getMemberById(footerText.substring(footerText.lastIndexOf(' ') + 2, footerText.length() - 1));


                            StringBuilder text = new StringBuilder()
                                    .append(fdmds.getRole().map(Role::getAsMention).orElse("")).append("\n")
                                    .append("Einen Wunderschönen hier ist ").append(requester.getAsMention()).append(" <:slimewave:1080225151104331817>,\n\n")
                                    .append(question).append("\n\n")
                                    .append(choices).append("\n\n")
                                    .append("Du möchtest selbst eine Umfrage Einreichen? Verwende </fdmds:")
                                    .append(manager.getCommandCache().getGuildCommand(event.getGuild().getIdLong(), "fdmds"))
                                    .append(">")
                                    .append(" oder den Knopf unter dieser Nachricht!");

                            // Send and add reactions
                            channel.sendMessage(text)
                                    .addActionRow(Button.secondary("fdmds:create", "Frage einreichen"))
                                    .queue(m -> {
                                        for (int i = 0; i < choices.lines().count(); i++) {
                                            m.addReaction(SlimeEmoji.fromId(i + 1).getEmoji()).queue();
                                        }

                                        event.reply("Frage verschickt!").setEphemeral(true).queue();
                                    });

                            event.getMessage().delete().queue();
                        },
                        () -> event.reply("Error: Channel wurde nicht gesetzt!").setEphemeral(true).queue()
                )
        );
    }
}
