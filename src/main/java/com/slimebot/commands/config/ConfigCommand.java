package com.slimebot.commands.config;

import com.slimebot.commands.config.engine.ConfigCategory;
import com.slimebot.commands.config.engine.ConfigCategoryCommand;
import com.slimebot.commands.config.engine.ConfigField;
import com.slimebot.commands.config.engine.InstanceProvider;
import com.slimebot.main.CommandContext;
import com.slimebot.main.CommandPermission;
import com.slimebot.main.Main;
import com.slimebot.main.config.Config;
import com.slimebot.main.config.guild.GuildConfig;
import com.slimebot.message.StaffMessage;
import de.mineking.discord.commands.CommandImplementation;
import de.mineking.discord.commands.CommandManager;
import de.mineking.discord.commands.annotated.ApplicationCommand;
import de.mineking.discord.commands.annotated.ApplicationCommandMethod;
import de.mineking.discord.commands.annotated.WhenFinished;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Dieser Befehl ist der Hauptbefehl für die Konfiguration. Wenn du in {@link GuildConfig} Felder mit den korrekten Annotationen {@link ConfigCategory} und {@link ConfigField} erstellst, werden die Unterbefehle automatisch generiert.
 */
@ApplicationCommand(name = "config", description = "Verwaltet die Bot-Konfiguration für diesen Server", guildOnly = true)
public class ConfigCommand {
	public final static Logger logger = LoggerFactory.getLogger(ConfigCommand.class);

	public CommandPermission permission = CommandPermission.TEAM;

	/**
	 * Gibt dir im `handler` Zugriff auf die Konfiguration eines Servers und speichert sie anschließend.
	 * @param guild Der Server, dessen Konfiguration du verändern möchtest
	 * @param handler Der handler, in dem du die Konfiguration anpassen kannst.
	 */
	public static void updateField(Guild guild, Consumer<GuildConfig> handler) {
		updateField(guild.getIdLong(), handler);
	}

	/**
	 * Gibt dir im `handler` Zugriff auf die Konfiguration eines Servers und speichert sie anschließend.
	 * @param guild Der Server, dessen Konfiguration du verändern möchtest
	 * @param handler Der handler, in dem du die Konfiguration anpassen kannst.
	 */
	public static void updateField(long guild, Consumer<GuildConfig> handler) {
		GuildConfig config = GuildConfig.getConfig(guild);
		handler.accept(config);
		config.save();
	}

	@ApplicationCommand(name = "reload", description = "Lädt alle Konfigurationen neu")
	public static class ReloadCommand {
		@ApplicationCommandMethod
		public void performCommand(SlashCommandInteractionEvent event) throws Exception {
			Main.config = Config.readFromFile("config");

			Main.jdaInstance.getGuilds().forEach(g -> {
				GuildConfig.load(g);
				StaffMessage.updateMessage(g);
			});

			event.reply("Konfigurationen neu geladen").setEphemeral(true).queue();
		}
	}

	@WhenFinished
	public void setup(CommandManager<CommandContext> cmdMan) {
		List<Field> mainFields = new ArrayList<>();

		for(Field field : GuildConfig.class.getFields()) {
			if(Modifier.isTransient(field.getModifiers())) continue;

			if(field.isAnnotationPresent(ConfigCategory.class)) {
				registerCategory(cmdMan, field.getAnnotation(ConfigCategory.class), field.getType().getFields(), (create, config) -> {
					try {
						Object temp = field.get(config);

						if(temp == null && create) {
							temp = field.getType().getConstructor().newInstance();
							field.set(config, temp);
						}

						return temp;
					} catch (IllegalAccessException | NoSuchMethodException | InstantiationException | InvocationTargetException e) {
						throw new RuntimeException(e);
					}
				});
			}

			else {
				mainFields.add(field);
			}
		}

		registerCategory(cmdMan, GuildConfig.class.getAnnotation(ConfigCategory.class), mainFields.toArray(Field[]::new), (create, config) -> config);
	}

	private void registerCategory(CommandManager<CommandContext> cmdMan, ConfigCategory category, Field[] fields, InstanceProvider instanceProvider) {
		cmdMan.registerCommand("config " + category.name(), new ConfigCategoryCommand(category, fields, instanceProvider));

		CommandImplementation group = cmdMan.getCommands().get("config " + category.name());

		for(Class<?> sc : category.subcommands()) {
			cmdMan.registerCommand(group, sc);
		}
	}
}
