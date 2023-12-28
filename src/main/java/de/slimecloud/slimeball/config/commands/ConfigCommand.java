package de.slimecloud.slimeball.config.commands;

import de.mineking.discordutils.commands.ApplicationCommand;
import de.mineking.discordutils.commands.Command;
import de.mineking.discordutils.commands.CommandManager;
import de.mineking.discordutils.commands.Setup;
import de.mineking.discordutils.commands.condition.Scope;
import de.mineking.discordutils.commands.context.ICommandContext;
import de.mineking.discordutils.ui.MessageMenu;
import de.mineking.discordutils.ui.MessageRenderer;
import de.mineking.discordutils.ui.RenderTermination;
import de.mineking.discordutils.ui.UIManager;
import de.mineking.discordutils.ui.components.button.ButtonColor;
import de.mineking.discordutils.ui.components.button.ButtonComponent;
import de.slimecloud.slimeball.config.ConfigCategory;
import de.slimecloud.slimeball.config.GuildConfig;
import de.slimecloud.slimeball.config.engine.CategoryInfo;
import de.slimecloud.slimeball.config.engine.ConfigField;
import de.slimecloud.slimeball.config.engine.ConfigFieldType;
import de.slimecloud.slimeball.config.engine.KeyType;
import de.slimecloud.slimeball.main.CommandPermission;
import de.slimecloud.slimeball.main.SlimeBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationCommand(name = "config", description = "Verwaltet die konfiguration des Servers", scope = Scope.GUILD_GLOBAL)
public class ConfigCommand {
	public CommandPermission permission = CommandPermission.TEAM;

	@Setup
	public static void setup(@NotNull SlimeBot bot, @NotNull UIManager uiManager, @NotNull CommandManager<ICommandContext, ?> cmdManager, @NotNull Command<ICommandContext> command) {
		command.addSubcommand(MenuCommand.class);

		MessageMenu resetMenu = createMenu(bot, uiManager);
		Set<Field> fields = new HashSet<>();

		for (Field f : GuildConfig.class.getDeclaredFields()) {
			f.setAccessible(true);

			//Add categories
			if (f.isAnnotationPresent(CategoryInfo.class))
				command.addSubcommand(CategoryCommand.getCommand(bot, cmdManager, f, resetMenu));
			else if (f.isAnnotationPresent(ConfigField.class)) fields.add(f);
		}

		//Create general category
		command.addSubcommand(new CategoryCommand(bot, cmdManager, null, c -> c, GuildConfig.class.getAnnotation(CategoryInfo.class), fields.toArray(Field[]::new), resetMenu));
	}

	public static MessageMenu createMenu(@NotNull SlimeBot bot, @NotNull UIManager manager) {
		return manager.createMenu("config.value",
				MessageRenderer.embed(s -> new EmbedBuilder()
						.setTitle(s.<CategoryInfo>getCache("category").name() + " -> " + s.<ConfigField>getCache("info").name())
						.setColor(bot.getColor(s.event.getGuild()))
						.setDescription(s.<ConfigField>getCache("info").description())
						.addField(
								"Wert",
								s.getCache("value") == null ? "*Nicht gesetzt*" : s.getCache("value"),
								false
						)
						.build()
				),
				new ButtonComponent("reset", ButtonColor.RED, "Wert zurücksetzen")
						.appendHandler(s -> {
							try {
								//Extract values
								Field field = s.getCache("field");
								Object value = getEmptyValue(field);
								Object instance = s.getCache("instance");

								field.set(instance, value);
								s.<GuildConfig>getCache("config").save();

								//Call update method
								if (instance instanceof ConfigCategory c) c.update();

								//Manually reset cached value. This has to be done because the cache method is called before rendering the components
								s.setCache("value", toString(field, value));
							} catch (IllegalAccessException e) {
								throw new RuntimeException(e);
							}

							s.instantUpdate();
							s.sendReply(MessageCreateData.fromContent("Wert zurückgesetzt!"));
						}).asDisabled(s -> s.getCache("field") == null)
		).cache(s -> {
			try {
				//Get category and field
				Class<?> category = Class.forName("de.slimecloud.slimeball." + s.getState("c"));

				Field field = category.getDeclaredField(s.getState("f"));
				field.setAccessible(true);

				//Load config
				GuildConfig config = bot.loadGuild(s.event.getGuild());

				Object instance = null;
				CategoryInfo info = null;

				//Find category
				if (category == GuildConfig.class) {
					instance = config;
					info = GuildConfig.class.getAnnotation(CategoryInfo.class);
				} else {
					for (Field f : GuildConfig.class.getDeclaredFields()) {
						f.setAccessible(true);

						if (f.getType().equals(category)) {
							instance = f.get(config);
							info = f.getAnnotation(CategoryInfo.class);

							break;
						}
					}
				}

				s.setCache("category", info);
				s.setCache("info", field.getAnnotation(ConfigField.class));
				s.setCache("config", config);

				//Cancel if category not present
				if (instance == null) {
					s.event.reply(":x: Keine Konfiguration für Kategorie vorhanden!").setEphemeral(true).queue();
					throw new RenderTermination();
				}

				//Get field value
				Object value = field.get(instance);

				//Store in cache
				s.setCache("value", toString(field, value));
				s.setCache("instance", instance);
				s.setCache("field", field);
			} catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		});
	}

	@Nullable
	private static String toString(@NotNull Field field, @Nullable Object value) {
		ConfigFieldType type = field.getAnnotation(ConfigField.class).type();

		if (value instanceof Collection<?> c) {
			if (c.isEmpty()) return null;

			return c.stream()
					.map(e -> "- " + type.getString().apply(e))
					.collect(Collectors.joining("\n"));
		} else if (value instanceof Map<?, ?> m) {
			if (m.isEmpty()) return null;

			return m.entrySet().stream()
					.map(e -> "- " + field.getAnnotation(KeyType.class).value().getString().apply(e.getKey()) + " = " + type.getString().apply(e.getValue()))
					.collect(Collectors.joining("\n"));
		} else if (value != null) return type.getString().apply(value);

		return null;
	}

	@Nullable
	private static Object getEmptyValue(@NotNull Field f) {
		if (f.getType().isAssignableFrom(List.class)) return new ArrayList<>();
		else if (f.getType().isAssignableFrom(Set.class)) return new HashSet<>();
		else if (f.getType().isAssignableFrom(Map.class)) return new HashMap<>();
		else if (f.getType().isAssignableFrom(LinkedHashMap.class)) return new LinkedHashMap<>();

		return null;
	}
}
