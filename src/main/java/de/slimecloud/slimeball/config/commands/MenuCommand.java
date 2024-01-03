package de.slimecloud.slimeball.config.commands;

import de.mineking.discordutils.commands.ApplicationCommand;
import de.mineking.discordutils.commands.ApplicationCommandMethod;
import de.mineking.discordutils.ui.MessageMenu;
import de.mineking.discordutils.ui.MessageRenderer;
import de.mineking.discordutils.ui.RenderTermination;
import de.mineking.discordutils.ui.UIManager;
import de.mineking.discordutils.ui.components.button.ButtonColor;
import de.mineking.discordutils.ui.components.button.ButtonComponent;
import de.mineking.discordutils.ui.components.button.MenuComponent;
import de.mineking.discordutils.ui.components.select.EntitySelectComponent;
import de.mineking.discordutils.ui.components.select.StringSelectComponent;
import de.mineking.discordutils.ui.components.types.Component;
import de.mineking.discordutils.ui.components.types.ComponentRow;
import de.mineking.discordutils.ui.state.DataState;
import de.slimecloud.slimeball.config.ConfigCategory;
import de.slimecloud.slimeball.config.GuildConfig;
import de.slimecloud.slimeball.config.engine.CategoryInfo;
import de.slimecloud.slimeball.config.engine.ConfigField;
import de.slimecloud.slimeball.config.engine.ConfigFieldType;
import de.slimecloud.slimeball.config.engine.KeyType;
import de.slimecloud.slimeball.main.Main;
import de.slimecloud.slimeball.main.SlimeBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Consumer;
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
				.map(f -> {
					f.setAccessible(true);
					ConfigField info = f.getAnnotation(ConfigField.class);

					return new MenuComponent<>(
							createFieldMenu(bot, manager, f.getGenericType(), instance, (s, c) -> {
								try {
									return f.get(instance.apply(c));
								} catch (IllegalAccessException e) {
									throw new RuntimeException(e);
								}
							}, (s, c, v) -> {
								try {
									f.set(instance.apply(c), v);
								} catch (IllegalAccessException e) {
									throw new RuntimeException(e);
								}
							}, s -> info.name(), category, info, f.isAnnotationPresent(KeyType.class) ? f.getAnnotation(KeyType.class).value() : null),
							ButtonColor.BLUE,
							f.getAnnotation(ConfigField.class).name()
					);
				})
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
	private static Type getGenericType(@NotNull Type type, int i) {
		return ((ParameterizedType) type).getActualTypeArguments()[i];
	}

	@NotNull
	private static Class<?> getClass(@NotNull Type type) {
		if (type instanceof Class<?> c) return c;
		else if (type instanceof ParameterizedType p) return (Class<?>) p.getRawType();

		throw new IllegalArgumentException();
	}

	@NotNull
	private static Object createEmptyCollection(@NotNull Class<?> clazz, @NotNull Type type) {
		if (clazz.isAssignableFrom(ArrayList.class)) return new ArrayList<>();
		else if (clazz.isAssignableFrom(HashSet.class)) return new HashSet<>();
		else if (clazz.isAssignableFrom(EnumSet.class)) return emptyEnumSet(getClass(getGenericType(type, 0)));

		throw new IllegalArgumentException();
	}

	@SuppressWarnings("unchecked")
	private static <E extends Enum<E>> EnumSet<E> emptyEnumSet(@NotNull Class<?> type) {
		return EnumSet.noneOf((Class<E>) type);
	}

	@NotNull
	private static MessageMenu createFieldMenu(@NotNull SlimeBot bot, @NotNull UIManager manager, @NotNull Type generic, @NotNull Function<GuildConfig, Object> categoryInstance, @NotNull Getter getter, @NotNull Setter setter, @NotNull Function<DataState<?>, String> display, @NotNull CategoryInfo category, @NotNull ConfigField field, @Nullable ConfigFieldType keyType) {
		Class<?> type = getClass(generic);

		if (EnumSet.class.isAssignableFrom(type)) return createEnumSetValueMenu(bot, manager, generic, categoryInstance, getter, setter, display, category, field);
		if (Collection.class.isAssignableFrom(type)) return createListValueMenu(bot, manager, type, generic, categoryInstance, getter, setter, display, category, field);
		if (Map.class.isAssignableFrom(type)) return createMapValueMenu(bot, manager, type, generic, categoryInstance, getter, setter, display, category, field, keyType);
		return createValueMenu(bot, manager, type, categoryInstance, getter, setter, display, category, field);
	}

	private static void set(@NotNull SlimeBot bot, @NotNull DataState<?> state, @NotNull Function<GuildConfig, Object> category, @NotNull Setter setter, @Nullable Object value) {
		GuildConfig config = bot.loadGuild(state.event.getGuild());
		setter.set(state, config, value);
		if (category.apply(config) instanceof ConfigCategory c) c.update(state.event.getGuild());
		config.save();
	}

	@SuppressWarnings("unchecked")
	private static <T> T get(@NotNull SlimeBot bot, @NotNull DataState<?> state, @NotNull Getter getter) {
		return (T) getter.get(state, bot.loadGuild(state.event.getGuild()));
	}

	@SuppressWarnings("unchecked")
	private static <T> void handle(@NotNull SlimeBot bot, @NotNull DataState<?> state, @NotNull Function<GuildConfig, Object> category, @NotNull Getter getter, @NotNull Consumer<T> handler) {
		GuildConfig config = bot.loadGuild(state.event.getGuild());
		handler.accept((T) getter.get(state, config));
		if (category.apply(config) instanceof ConfigCategory c) c.update(state.event.getGuild());
		config.save();
	}

	@NotNull
	private static MessageMenu createValueMenu(@NotNull SlimeBot bot, @NotNull UIManager manager, @NotNull Class<?> type, @NotNull Function<GuildConfig, Object> categoryInstance, @NotNull Getter getter, @NotNull Setter setter, @NotNull Function<DataState<?>, String> display, @NotNull CategoryInfo category, @NotNull ConfigField field) {
		List<Component<?>> components = new ArrayList<>();

		components.add(new ButtonComponent("back", ButtonColor.GRAY, "Zurück").appendHandler(s -> manager.getMenu("config." + category.command()).display(s.event)));
		components.add(new ButtonComponent("reset", ButtonColor.RED, "Zurücksetzten").appendHandler(s -> {
			set(bot, s, categoryInstance, setter, null);
			s.update();
		}));

		Component<?> component = field.type().createComponent(manager, type, "config." + category.command() + "." + field.command(), "value", "Wert festlegen", (s, v) -> set(bot, s, categoryInstance, setter, v));

		if (component instanceof EntitySelectComponent || component instanceof StringSelectComponent) components.add(0, component);
		else components.add(component);

		return manager.createMenu(
				"config." + category.command() + "." + field.command(),
				MessageRenderer.embed(s -> {
					Object value = get(bot, s, getter);

					return new EmbedBuilder()
							.setDescription("## " + category.name() + " → " + display.apply(s) + "\n")
							.setColor(bot.getColor(s.event.getGuild()))
							.appendDescription(field.description())
							.appendDescription("\n### Aktueller Wert\n")
							.appendDescription(value == null ? "*nicht gesetzt*" : field.type().toString(value))
							.build();
				}),
				ComponentRow.ofMany(components)
		);
	}

	@NotNull
	@SuppressWarnings({"rawtypes", "unchecked"})
	private static MessageMenu createListValueMenu(@NotNull SlimeBot bot, @NotNull UIManager manager, @NotNull Class<?> type, @NotNull Type generic, @NotNull Function<GuildConfig, Object> categoryInstance, @NotNull Getter getter, @NotNull Setter setter, @NotNull Function<DataState<?>, String> display, @NotNull CategoryInfo category, @NotNull ConfigField field) {
		Type componentType = getGenericType(generic, 0);
		Class<?> componentClass = getClass(componentType);

		List<Component<?>> components = new ArrayList<>();

		components.add(new ButtonComponent("back", ButtonColor.GRAY, "Zurück").appendHandler(s -> manager.getMenu("config." + category.command()).display(s.event)));
		components.add(new ButtonComponent("reset", ButtonColor.RED, "Zurücksetzten").appendHandler(s -> {
			set(bot, s, categoryInstance, setter, createEmptyCollection(type, generic));
			s.update();
		}));

		Component<?> add = field.type().createComponent(manager, componentClass, "config." + category.command() + "." + field.command(), "add", "Wert hinzufügen", (s, v) -> MenuCommand.<Collection>handle(bot, s, categoryInstance, getter, c -> c.add(v)));

		Component<?> remove = new StringSelectComponent("remove", s -> MenuCommand.<Collection<?>>get(bot, s, getter).stream()
				.map(e -> field.type().createSelectOption(bot, e))
				.toList()
		).setPlaceholder("Wert entfernen").appendHandler((s, v) -> {
			MenuCommand.<Collection<?>>handle(bot, s, categoryInstance, getter, c -> c.remove(field.type().parse(componentClass, v.get(0).getValue())));
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
				"config." + category.command() + "." + field.command(),
				MessageRenderer.embed(s -> {
					Collection<?> value = get(bot, s, getter);

					return new EmbedBuilder()
							.setDescription("## " + category.name() + " → " + display.apply(s) + "\n")
							.setColor(bot.getColor(s.event.getGuild()))
							.appendDescription(field.description())
							.appendDescription("\n### Aktuelle Einträge\n")
							.appendDescription(value.isEmpty() ? "*Keine Einträge*" : value.stream().map(e -> "- " + field.type().toString(e)).collect(Collectors.joining("\n")))
							.build();
				}),
				ComponentRow.ofMany(components)
		);
	}

	@NotNull
	@SuppressWarnings({"rawtypes", "unchecked"})
	private static MessageMenu createEnumSetValueMenu(@NotNull SlimeBot bot, @NotNull UIManager manager, @NotNull Type generic, @NotNull Function<GuildConfig, Object> categoryInstance, @NotNull Getter getter, @NotNull Setter setter, @NotNull Function<DataState<?>, String> display, @NotNull CategoryInfo category, @NotNull ConfigField field) {
		Class<?> componentClass = getClass(getGenericType(generic, 0));

		List<Component<?>> components = new ArrayList<>();

		components.add(new ButtonComponent("back", ButtonColor.GRAY, "Zurück").appendHandler(s -> manager.getMenu("config." + category.command()).display(s.event)));
		components.add(new ButtonComponent("reset", ButtonColor.RED, "Zurücksetzten").appendHandler(s -> {
			set(bot, s, categoryInstance, setter, emptyEnumSet(componentClass));
			s.update();
		}));

		components.add(new StringSelectComponent("value", s -> Arrays.stream(componentClass.getEnumConstants())
				.map(e -> SelectOption.of(e.toString(), ((Enum<?>) e).name())
						.withDefault(MenuCommand.<EnumSet<?>>get(bot, s, getter).contains(e))
				)
				.toList()
		).setMinValues(0).setMaxValues(componentClass.getEnumConstants().length).appendHandler((s, v) -> {
			MenuCommand.<EnumSet>handle(bot, s, categoryInstance, getter, c -> {
				c.clear();
				c.addAll(v.stream().map(x -> Arrays.stream(componentClass.getEnumConstants()).map(e -> (Enum<?>) e).filter(e -> e.name().equals(x.getValue())).findFirst().orElseThrow()).toList());
			});

			s.update();
		}));

		return manager.createMenu(
				"config." + category.command() + "." + field.command(),
				MessageRenderer.embed(s -> {
					EnumSet<?> value = get(bot, s, getter);

					return new EmbedBuilder()
							.setDescription("## " + category.name() + " → " + display.apply(s) + "\n")
							.setColor(bot.getColor(s.event.getGuild()))
							.appendDescription(field.description())
							.appendDescription("\n### Aktuelle Einträge\n")
							.appendDescription(value.isEmpty() ? "*Keine Einträge*" : value.stream().map(e -> "- " + field.type().toString(e)).collect(Collectors.joining("\n")))
							.build();
				}),
				ComponentRow.ofMany(components)
		);
	}

	@NotNull
	@SuppressWarnings({"unchecked", "rawtypes"})
	private static MessageMenu createMapValueMenu(@NotNull SlimeBot bot, @NotNull UIManager manager, @NotNull Class<?> type, @NotNull Type generic, @NotNull Function<GuildConfig, Object> categoryInstance, @NotNull Getter getter, @NotNull Setter setter, @NotNull Function<DataState<?>, String> display, @NotNull CategoryInfo category, @NotNull ConfigField field, @Nullable ConfigFieldType keyType) {
		Class<?> keyClass = getClass(getGenericType(generic, 0));

		Type valueType = getGenericType(generic, 1);
		Class<?> valueClass = getClass(valueType);

		if (Map.class.isAssignableFrom(keyClass) || Collection.class.isAssignableFrom(keyClass)) throw new UnsupportedOperationException();
		if (Map.class.isAssignableFrom(valueClass)) throw new UnsupportedOperationException("Cannot create config menu with map inside of map");

		List<Component<?>> components = new ArrayList<>();

		components.add(new ButtonComponent("back", ButtonColor.GRAY, "Zurück").appendHandler(s -> manager.getMenu("config." + category.command()).display(s.event)));
		components.add(new ButtonComponent("reset", ButtonColor.RED, "Zurücksetzten").appendHandler(s -> {
			set(bot, s, categoryInstance, setter, type.isAssignableFrom(HashMap.class) ? new HashMap<>() : new LinkedHashMap<>());
			s.update();
		}));

		CategoryInfo valueCategory = createCategory(category.name() + " → " + field.name(), category.command() + "." + field.command(), field.description());
		ConfigField valueField = createField("?", "value", field.description(), field.type());

		MessageMenu valueMenu = createFieldMenu(bot, manager, valueType, categoryInstance,
				(s, c) -> ((Map) getter.get(s, c)).computeIfAbsent(s.getRawState("key", keyClass), x -> Collection.class.isAssignableFrom(valueClass) ? createEmptyCollection(valueClass, valueType) : null),
				(s, c, v) -> {
					Map value = (Map) getter.get(s, c);

					Object key = keyType.parse(keyClass, s.getRawState("key", keyClass));

					if (v != null) value.put(key, v);
					else value.remove(key);
				},
				s -> keyType.toString(s.getRawState("key", keyClass)), valueCategory, valueField, null
		);

		Component<?> add = keyType.createComponent(manager, keyClass, "config." + category.command() + "." + field.command(), "add", "Wert hinzufügen", (s, k) -> {
			valueMenu.createState(s).setState("key", k).display(s.event);
			throw new RenderTermination();
		});

		Component<?> remove = new StringSelectComponent("remove", s -> MenuCommand.<Map<?, ?>>get(bot, s, getter).keySet().stream()
				.map(e -> keyType.createSelectOption(bot, e))
				.toList()
		).setPlaceholder("Wert entfernen").appendHandler((s, v) -> {
			MenuCommand.<Map<?, ?>>handle(bot, s, categoryInstance, getter, c -> c.remove(keyType.parse(keyClass, v.get(0).getValue())));
			s.update();
		});

		Component<?> edit = new StringSelectComponent("edit", s -> MenuCommand.<Map<?, ?>>get(bot, s, getter).keySet().stream()
				.map(e -> keyType.createSelectOption(bot, e))
				.toList()
		).setPlaceholder("Wert bearbeiten").appendHandler((s, v) -> valueMenu.createState(s).setState("key", v.get(0).getValue()).display(s.event));


		if (add instanceof EntitySelectComponent || add instanceof StringSelectComponent) {
			components.add(0, add);
			components.add(1, remove);
			components.add(2, edit);
		} else {
			components.add(add);
			components.add(remove);
			components.add(edit);
		}

		return manager.createMenu(
				"config." + category.command() + "." + field.command(),
				MessageRenderer.embed(s -> {
					Map<?, ?> value = get(bot, s, getter);

					return new EmbedBuilder()
							.setDescription("## " + category.name() + " → " + display.apply(s) + "\n")
							.setColor(bot.getColor(s.event.getGuild()))
							.appendDescription(field.description())
							.appendDescription("\n### Aktuelle Einträge\n")
							.appendDescription(value.isEmpty() ? "*Keine Einträge*" : value.entrySet().stream().map(e -> "- " + keyType.toString(e.getKey()) + " = " + field.type().toString(e.getValue())).collect(Collectors.joining("\n")))
							.build();
				}),
				ComponentRow.ofMany(components)
		);
	}

	@ApplicationCommandMethod
	public void performCommand(@NotNull SlashCommandInteractionEvent event) {
		menu.display(event);
	}

	private interface Getter {
		@Nullable
		Object get(@NotNull DataState<?> state, @NotNull GuildConfig config);
	}

	private interface Setter {
		void set(@NotNull DataState<?> state, @NotNull GuildConfig config, @Nullable Object value);
	}

	private static CategoryInfo createCategory(@NotNull String name, @NotNull String command, @NotNull String description) {
		return new CategoryInfo() {
			@Override
			public String name() {
				return name;
			}

			@Override
			public String command() {
				return command;
			}

			@Override
			public String description() {
				return description;
			}

			@Override
			public Class<? extends Annotation> annotationType() {
				return CategoryInfo.class;
			}
		};
	}

	private static ConfigField createField(@NotNull String name, @NotNull String command, @NotNull String description, @NotNull ConfigFieldType type) {
		return new ConfigField() {
			@Override
			public String name() {
				return name;
			}

			@Override
			public String command() {
				return command;
			}

			@Override
			public String description() {
				return description;
			}

			@Override
			public boolean required() {
				return false;
			}

			@Override
			public ConfigFieldType type() {
				return type;
			}

			@Override
			public Class<? extends Annotation> annotationType() {
				return ConfigField.class;
			}
		};
	}
}
