package com.slimebot.commands;

import com.slimebot.main.Main;
import com.slimebot.utils.Config;
import de.mineking.discord.DiscordUtils;
import de.mineking.discord.commands.annotated.ApplicationCommand;
import de.mineking.discord.commands.annotated.ApplicationCommandMethod;
import de.mineking.discord.commands.annotated.WhenFinished;
import de.mineking.discord.events.interaction.ModalHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.simpleyaml.configuration.file.YamlFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@ApplicationCommand(name = "bug", description = "Melde einen Bug")
public class BugCommand {

    private static final Logger logger = LoggerFactory.getLogger(BugCommand.class);

    private final Map<String, LocalDateTime> lastBugReport = new HashMap<>();

    private static YamlFile getGitHubConfig(String guildId) {
        YamlFile config = Config.getConfig(guildId, "githubConfig");
        if (!config.exists()) {
            createGitHubConfig(config);
        }
        try {
            config.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return config;
    }

    private static void createGitHubConfig(YamlFile config) {
        try {
            config.createNewFile();
            config.set("enabled", false);
            config.setComment("enabled", "Enable or disable the GitHub issue creation");
            config.set("personalAccessToken", "");
            config.set("repoName", "SlimeCloud/java-SlimeBot");
            config.setComment("repoName", "The name of the repository where the issues should be created. Format: User/Repo");
            config.set("user", "SlimeCloud");
            config.setComment("user", "The user or organization that should be used to create the issues");
            config.set("issueTemplate", """
                    **Describe the bug**
                    %description%
                                        
                         
                    **To Reproduce**
                    %steps%
                                        
                    					
                    **Expected behavior**
                    %expected%
                                        
                         
                    **Solution idea**
                    %solution%
                                        
                         
                    **ToDos**
                                        
                         
                    **Screenshots**
                                        
                         
                    **Additional context**
                    """);
            config.setComment("issueTemplate", """
                    %title% = Title of the issue
                    %description% = Description of the issue
                    %steps% = Steps to reproduce the issue
                    %expected% = Expected behavior
                    %solution% = Solution idea
                    """);
            config.save();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @ApplicationCommandMethod
    public void performCommand(SlashCommandInteractionEvent event) {
        if (lastBugReport.containsKey(event.getUser().getId())) {
            LocalDateTime lastReport = lastBugReport.get(event.getUser().getId());
            if (lastReport.plusMinutes(5).isAfter(LocalDateTime.now())) {
                event.reply("Du kannst nur alle 5 Minuten einen Bugreport erstellen!").setEphemeral(true).queue();
                return;
            }
        }
        lastBugReport.put(event.getUser().getId(), LocalDateTime.now());

        TextInput title = TextInput.create("title", "Titel", TextInputStyle.SHORT)
                .setMinLength(10)
                .setPlaceholder("Eine kurze präzise Beschreibung des Bugs")
                .build();

        TextInput howToReproduce = TextInput.create("steps", "Schritte zum Reproduzieren", TextInputStyle.PARAGRAPH)
                .setMinLength(10)
                .setPlaceholder("""
                        1. Gehe zu '....'
                        2. Klicke auf '....'
                        3. Scrolle nach unten zu '....'""")
                .build();

        TextInput textInput = TextInput.create("bug", "Beschreibung", TextInputStyle.PARAGRAPH)
                .setMinLength(10)
                .setPlaceholder("Eine ausführliche Beschreibung des Bugs")
                .build();

        TextInput expected = TextInput.create("expected", "Erwartetes Verhalten", TextInputStyle.PARAGRAPH)
                .setMinLength(10)
                .setPlaceholder("Ich erwarte, dass '....' passiert")
                .build();

        TextInput solution = TextInput.create("solution", "Lösung", TextInputStyle.PARAGRAPH)
                .setPlaceholder("Ich würde '....' ändern, damit '....' passiert")
                .setRequired(false)
                .build();

        Modal modal = Modal.create("bug", "Melde einen Bug")
                .addActionRow(title)
                .addActionRow(textInput)
                .addActionRow(howToReproduce)
                .addActionRow(expected)
                .addActionRow(solution)
                .build();

        event.replyModal(modal).queue();
    }

    @WhenFinished
    public void setup(DiscordUtils utils){
        utils.getEventManager().registerHandler(new ModalHandler("bug", this::processModal));
    }

    public void processModal(ModalInteractionEvent event) {
        YamlFile config = Config.getConfig(Objects.requireNonNull(event.getGuild()).getId(), "mainConfig");


        try {
            config.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setColor(Main.embedColor(event.getGuild().getId()))
                .setTitle(event.getValue("title").getAsString())

                .setDescription("**Fehlerbeschreibung:**\n\n")
                .appendDescription(event.getValue("bug").getAsString() + "\n")
                .appendDescription("\n\n**Schritte zum Reproduzieren:** \n\n")
                .appendDescription(event.getValue("steps").getAsString() + "\n")
                .appendDescription("\n\n**Erwartetes Verhalten:** \n\n")
                .appendDescription(event.getValue("expected").getAsString() + "\n")
                .appendDescription("\n\n**Lösung:** \n\n")
                .appendDescription(event.getValue("solution").getAsString() + "\n");

        event.reply("Der Report wurde erfolgreich ausgeführt").setEphemeral(true).queue();

        event.getGuild()
                .getTextChannelById(config.getString("logChannel"))
                .sendMessage("## Ein neuer Bug wurde gefunden! " +
                        "Erstellt von " + event.getMember().getAsMention())
                .setEmbeds(embedBuilder.build())
                .queue();

        YamlFile githubConfig = getGitHubConfig(event.getGuild().getId());
        if (githubConfig.getBoolean("enabled")) {
            createIssue(event);
        }
        logger.info("New bug report from " + event.getUser().getGlobalName() + " (" + event.getUser().getId() + "): " + event.getValue("title").getAsString());
    }

    private void createIssue(ModalInteractionEvent event) {
        YamlFile githubConfig = getGitHubConfig(event.getGuild().getId());
        String token = githubConfig.getString("personalAccessToken");
        String user = githubConfig.getString("user");
        String repoName = githubConfig.getString("repoName");
        String issueBody = githubConfig.getString("issueTemplate");
        issueBody = issueBody.replace("%title%", event.getValue("title").getAsString())
                .replace("%description%", event.getValue("bug").getAsString())
                .replace("%steps%", event.getValue("steps").getAsString())
                .replace("%expected%", event.getValue("expected").getAsString())
                .replace("%solution%", event.getValue("solution").getAsString());
        issueBody += "\n\nReport von: " + event.getUser().getGlobalName() + " (" + event.getUser().getId() + ")";

        try {
            GitHub github = new GitHubBuilder().withOAuthToken(token, user).build();
            GHRepository repo = github.getRepository(repoName);
            repo.createIssue("Bug: " + event.getValue("title").getAsString())
                    .body(issueBody)
                    .label("bug")
                    .create();
        } catch (IOException e) {
            logger.error("Error while creating issue", e);
        }

    }

}
