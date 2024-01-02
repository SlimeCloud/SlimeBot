package de.slimecloud.slimeball.config.commands;

import de.mineking.discordutils.commands.ApplicationCommand;
import de.mineking.discordutils.commands.ApplicationCommandMethod;
import de.mineking.discordutils.ui.MessageMenu;
import de.mineking.discordutils.ui.MessageRenderer;
import de.mineking.discordutils.ui.UIManager;
import de.mineking.discordutils.ui.components.button.ButtonColor;
import de.mineking.discordutils.ui.components.button.ButtonComponent;
import de.mineking.discordutils.ui.components.button.MenuComponent;
import de.mineking.discordutils.ui.components.select.EntitySelectComponent;
import de.mineking.discordutils.ui.components.select.StringSelectComponent;
import de.mineking.discordutils.ui.components.types.Component;
import de.mineking.discordutils.ui.components.types.ComponentRow;
import de.mineking.discordutils.ui.state.DataState;
import de.slimecloud.slimeball.config.GuildConfig;
import de.slimecloud.slimeball.config.engine.CategoryInfo;
import de.slimecloud.slimeball.config.engine.ConfigField;
import de.slimecloud.slimeball.main.Main;
import de.slimecloud.slimeball.main.SlimeBot;
import de.slimecloud.slimeball.util.ErrorConsumer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@ApplicationCommand(name = "menu", description = "Öffnet ein Menü für die Konfiguration")
public class MenuCommand {
	private final MessageMenu menu;

	public MenuCommand(@NotNull SlimeBot bot, @NotNull UIManager manager) {
		List<Component<?>> components = new ArrayList<>(Arrays.stream(GuildConfig.class.getDeclaredFields())
				.filter(f -> f.isAnnotationPresent(CategoryInfo.class))
				.map(f -> {
					CategoryInfo info = f.getAnnotation(CategoryInfo.class);
					f.setAccessible(true);

					return new MenuComponent<>(createCategory(bot, manager, c -> {
						try {
							return f.get(c);
						} catch (IllegalAccessException e) {
							throw new RuntimeException(e);
						}
					}, info, f.getType().getDeclaredFields()), ButtonColor.GRAY, info.name()).asDisabled(s -> {
						try {
							return f.get(bot.loadGuild(s.event.getGuild())) == null;
						} catch (IllegalAccessException e) {
							throw new RuntimeException(e);
						}
					});
				})
				.toList()
		);

		components.add(0, new MenuComponent<>(createCategory(bot, manager, c -> c, GuildConfig.class.getAnnotation(CategoryInfo.class), GuildConfig.class.getDeclaredFields()), ButtonColor.BLUE, "Allgemein"));

		menu = manager.createMenu(
				"config",
				MessageRenderer.embed(s -> new EmbedBuilder()
						.setDescription("## Konfiguration für **" + s.event.getGuild().getName() + "**\n")
						.setColor(bot.getColor(s.event.getGuild()))
						.setThumbnail(s.event.getGuild().getIconUrl())
						.appendDescription("Verwende die Buttons unter dieser Nachricht, um einzelne Kategorien zu konfigurieren\n")
						.appendDescription("Bevor die Konfiguration hier angepasst werden kann, muss eine Kategorie mit `/config <category> enable` aktiviert werden")
						.build()
				),
				ComponentRow.ofMany(components)
		);
	}

	@NotNull
	private static MessageMenu createCategory(@NotNull SlimeBot bot, @NotNull UIManager manager, @NotNull Function<GuildConfig, Object> instance, @NotNull CategoryInfo category, @NotNull Field[] fields) {
		//Get field components
		List<ComponentRow> components = ComponentRow.ofMany(Arrays.stream(fields)
				.filter(f -> f.isAnnotationPresent(ConfigField.class))
				.map(f -> new MenuComponent<>(
						createFieldMenu(bot, manager, instance, category, f),
						ButtonColor.BLUE,
						f.getAnnotation(ConfigField.class).name()
				))
				.toList()
		);

		//Add button to go back to main menu
		components.add(new ButtonComponent("back", ButtonColor.GRAY, "Zurück").appendHandler(s -> manager.getMenu("config").display(s.event)));

		//Create menu
		return manager.createMenu(
				"config." + category.command(),
				MessageRenderer.embed(s -> new EmbedBuilder()
						.setDescription("## " + category.name() + "\n")
						.setColor(bot.getColor(s.event.getGuild()))
						.appendDescription(category.description())
						.appendDescription("\n### Aktuelle Konfiguration\n")
						.appendDescription("```json\n" + Main.formattedJson.toJson(instance.apply(bot.loadGuild(s.event.getGuild()))) + "```")
						.build()
				),
				components
		);
	}

	@NotNull
	private static MessageMenu createFieldMenu(@NotNull SlimeBot bot, @NotNull UIManager manager, @NotNull Function<GuildConfig, Object> instance, CategoryInfo category, @NotNull Field field) {
		ConfigField info = field.getAnnotation(ConfigField.class);

		if (List.class.isAssignableFrom(field.getType())) return createListValueMenu(bot, manager, instance, category, info, field);
		if (Map.class.isAssignableFrom(field.getType())) return createMapValueMenu(bot, manager, instance, category, info, field);
		return createValueMenu(bot, manager, instance, category, info, field);
	}

	private static void handle(@NotNull SlimeBot bot, @NotNull DataState<?> state, @NotNull Function<GuildConfig, Object> instance, @NotNull ErrorConsumer<Object> handler) {
		GuildConfig config = bot.loadGuild(state.event.getGuild());
		try {
			handler.accept(instance.apply(config));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		config.save();
	}

	@NotNull
	private static MessageMenu createValueMenu(@NotNull SlimeBot bot, @NotNull UIManager manager, @NotNull Function<GuildConfig, Object> instance, @NotNull CategoryInfo category, @NotNull ConfigField info, @NotNull Field field) {
		field.setAccessible(true);

		List<Component<?>> components = new ArrayList<>();

		components.add(new ButtonComponent("back", ButtonColor.GRAY, "Zurück").appendHandler(s -> manager.getMenu("config." + category.command()).display(s.event)));
		components.add(new ButtonComponent("reset", ButtonColor.RED, "Zurücksetzten").appendHandler(s -> {
			handle(bot, s, instance, o -> field.set(o, null));
			s.update();
		}));

		Component<?> component = info.type().createComponent(manager, field.getType(), "config." + category.command() + "." + info.command(), "value", "Wert festlegen", (s, v) -> handle(bot, s, instance, o -> field.set(o, v)));

		if (component instanceof EntitySelectComponent || component instanceof StringSelectComponent) components.add(0, component);
		else components.add(component);

		return manager.createMenu(
				"config." + category.command() + "." + info.command(),
				MessageRenderer.embed(s -> {
					try {
						Object value = field.get(instance.apply(bot.loadGuild(s.event.getGuild())));

						return new EmbedBuilder()
								.setDescription("## " + category.name() + " → " + info.name() + "\n")
								.setColor(bot.getColor(s.event.getGuild()))
								.appendDescription(info.description())
								.appendDescription("\n### Aktueller Wert\n")
								.appendDescription(value == null ? "*nicht gesetzt*" : info.type().toString(value))
								.build();
					} catch (IllegalAccessException e) {
						throw new RuntimeException(e);
					}
				}),
				ComponentRow.ofMany(components)
		);
	}

	@NotNull
	@SuppressWarnings({"rawtypes", "unchecked"})
	private static MessageMenu createListValueMenu(@NotNull SlimeBot bot, @NotNull UIManager manager, @NotNull Function<GuildConfig, Object> instance, @NotNull CategoryInfo category, @NotNull ConfigField info, @NotNull Field field) {
		field.setAccessible(true);

		List<Component<?>> components = new ArrayList<>();

		components.add(new ButtonComponent("back", ButtonColor.GRAY, "Zurück").appendHandler(s -> manager.getMenu("config." + category.command()).display(s.event)));
		components.add(new ButtonComponent("reset", ButtonColor.RED, "Zurücksetzten").appendHandler(s -> {
			handle(bot, s, instance, o -> field.set(o, new ArrayList<>()));
			s.update();
		}));

		Component<?> add = info.type().createComponent(manager, field.getType(), "config." + category.command() + "." + info.command(), "add", "Wert hinzufügen", (s, v) -> handle(bot, s, instance, o -> ((Collection) field.get(o)).add(v)));

		Component<?> remove = new StringSelectComponent("remove", s -> ((Collection<?>) instance.apply(bot.loadGuild(s.event.getGuild()))).stream()
				.map(e -> info.type().createSelectOption(bot, e))
				.toList()
		).setPlaceholder("Wert entfernen").appendHandler((s, v) -> {
			handle(bot, s, instance, o -> ((Collection) field.get(o)).remove(v));
			s.update();
		});

		if (add instanceof EntitySelectComponent || add instanceof StringSelectComponent) {
			components.add(0, add);
			components.add(1, remove);
		} else {
			components.add(add);
			components.add(remove);
		}

		return manager.createMenu(
				"config." + category.command() + "." + info.command(),
				MessageRenderer.embed(s -> {
					try {
						Collection<?> value = (Collection<?>) field.get(instance.apply(bot.loadGuild(s.event.getGuild())));

						return new EmbedBuilder()
								.setDescription("## " + info.name() + "\n")
								.setColor(bot.getColor(s.event.getGuild()))
								.appendDescription(info.description())
								.appendDescription("\n### Aktuelle Einträge\n")
								.appendDescription(value.stream().map(e -> "- " + info.type().toString(e)).collect(Collectors.joining("\n")))
								.build();
					} catch (IllegalAccessException e) {
						throw new RuntimeException(e);
					}
				}),
				ComponentRow.ofMany(components)
		);
	}

	@NotNull
	private static MessageMenu createMapValueMenu(@NotNull SlimeBot bot, @NotNull UIManager manager, @NotNull Function<GuildConfig, Object> instance, @NotNull CategoryInfo category, @NotNull ConfigField info, @NotNull Field field) {
		field.setAccessible(true);

		return manager.createMenu(
				"config." + category.command() + "." + info.command(),
				MessageRenderer.embed(s -> new EmbedBuilder()
						.setTitle(info.name())
						.setColor(bot.getColor(s.event.getGuild()))
						.build()
				)
		);
	}

	@ApplicationCommandMethod
	public void performCommand(@NotNull SlashCommandInteractionEvent event) {
		menu.display(event);
	}
}
