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
import de.slimecloud.slimeball.config.engine.Info;
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
		//Create components for categories
		List<Component<?>> components = new ArrayList<>(Arrays.stream(GuildConfig.class.getDeclaredFields())
				.filter(f -> f.isAnnotationPresent(CategoryInfo.class)) //Field with category annotation
				.map(f -> {
					CategoryInfo info = f.getAnnotation(CategoryInfo.class);
					f.setAccessible(true);

					//Create component
					return new MenuComponent<>(createCategory(bot, manager, c -> {
						try {
							//Return category instance for a guild config instance via reflection
							return f.get(c);
						} catch (IllegalAccessException e) {
							throw new RuntimeException(e);
						}
					}, info, f.getType().getDeclaredFields()), ButtonColor.GRAY, info.name()).asDisabled(s -> {
						try {
							//Disable if category is null -> use has to use config enable command first
							return f.get(bot.loadGuild(s.getEvent().getGuild())) == null;
						} catch (IllegalAccessException e) {
							throw new RuntimeException(e);
						}
					});
				})
				.toList()
		);

		//Create component for general category
		components.add(0, new MenuComponent<>(createCategory(bot, manager, c -> c, GuildConfig.class.getAnnotation(CategoryInfo.class), GuildConfig.class.getDeclaredFields()), ButtonColor.BLUE, "Allgemein"));

		//Create menu
		menu = manager.createMenu(
				"config",
				MessageRenderer.embed(s -> new EmbedBuilder()
						.setDescription("## Konfiguration für **" + s.getEvent().getGuild().getName() + "**\n")
						.setColor(bot.getColor(s.getEvent().getGuild()))
						.setThumbnail(s.getEvent().getGuild().getIconUrl())
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
				.filter(f -> f.isAnnotationPresent(ConfigField.class)) //Field with annotation
				.map(f -> {
					f.setAccessible(true);
					ConfigField info = f.getAnnotation(ConfigField.class);

					//Create component
					return new MenuComponent<>(
							createFieldMenu(bot, manager, f.isAnnotationPresent(Info.class) ? f.getAnnotation(Info.class) : createDefaultInfo(), f.getGenericType(), instance, (s, c) -> {
								try {
									//Get field value from guild config instance via reflection
									return f.get(instance.apply(c));
								} catch (IllegalAccessException e) {
									throw new RuntimeException(e);
								}
							}, (s, c, v) -> {
								try {
									//Set field value to guild config instance via reflection
									f.set(instance.apply(c), v);
								} catch (IllegalAccessException e) {
									throw new RuntimeException(e);
								}
							}, s -> info.name(), category, info, f.isAnnotationPresent(Info.class) ? f.getAnnotation(Info.class).keyType() : null),
							ButtonColor.BLUE,
							f.getAnnotation(ConfigField.class).name()
					);
				})
				.toList()
		);

		//Add button to go back to main menu. We have to get the menu by name here because the menu doesn't exist yet
		components.add(new ButtonComponent("back", ButtonColor.GRAY, "Zurück").appendHandler(s -> manager.getMenu("config").display(s.getEvent())));

		//Create menu
		return manager.createMenu(
				"config." + category.command(),
				MessageRenderer.embed(s -> new EmbedBuilder()
						.setDescription("## " + category.name() + "\n")
						.setColor(bot.getColor(s.getEvent().getGuild()))
						.appendDescription(category.description())
						.appendDescription("\n### Aktuelle Konfiguration\n")
						.appendDescription("```json\n" + Main.formattedJson.toJson(instance.apply(bot.loadGuild(s.getEvent().getGuild()))) + "```")
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

	@NotNull
	@SuppressWarnings("unchecked")
	private static <E extends Enum<E>> EnumSet<E> emptyEnumSet(@NotNull Class<?> type) {
		return EnumSet.noneOf((Class<E>) type);
	}

	@NotNull
	private static MessageMenu createFieldMenu(@NotNull SlimeBot bot, @NotNull UIManager manager, @NotNull Info info, @NotNull Type generic, @NotNull Function<GuildConfig, Object> categoryInstance, @NotNull Getter getter, @NotNull Setter setter, @NotNull Function<DataState<?>, String> display, @NotNull CategoryInfo category, @NotNull ConfigField field, @Nullable ConfigFieldType keyType) {
		Class<?> type = getClass(generic);

		//Decide which menu to create based on actual field type
		if (EnumSet.class.isAssignableFrom(type)) return createEnumSetValueMenu(bot, manager, info, generic, categoryInstance, getter, setter, display, category, field);
		if (Collection.class.isAssignableFrom(type)) return createListValueMenu(bot, manager, info, type, generic, categoryInstance, getter, setter, display, category, field);
		if (Map.class.isAssignableFrom(type)) return createMapValueMenu(bot, manager, info, type, generic, categoryInstance, getter, setter, display, category, field, keyType);
		return createValueMenu(bot, manager, info, type, categoryInstance, getter, setter, display, category, field);
	}

	private static void set(@NotNull SlimeBot bot, @NotNull DataState<?> state, @NotNull Function<GuildConfig, Object> category, @NotNull Setter setter, @Nullable Object value) {
		GuildConfig config = bot.loadGuild(state.getEvent().getGuild());
		setter.set(state, config, value);

		//Call update method on category
		if (category.apply(config) instanceof ConfigCategory c) c.update(state.getEvent().getGuild());

		//Save changes
		config.save();
	}


	@Nullable
	@SuppressWarnings("unchecked")
	private static <T> T get(@NotNull SlimeBot bot, @NotNull DataState<?> state, @NotNull Getter getter) {
		return (T) getter.get(state, bot.loadGuild(state.getEvent().getGuild()));
	}

	@SuppressWarnings("unchecked")
	private static <T> void handle(@NotNull SlimeBot bot, @NotNull DataState<?> state, @NotNull Function<GuildConfig, Object> category, @NotNull Getter getter, @NotNull Consumer<T> handler) {
		GuildConfig config = bot.loadGuild(state.getEvent().getGuild());
		handler.accept((T) getter.get(state, config));

		//Call update method on category
		if (category.apply(config) instanceof ConfigCategory c) c.update(state.getEvent().getGuild());

		//Save changes
		config.save();
	}

	@NotNull
	private static MessageMenu createValueMenu(@NotNull SlimeBot bot, @NotNull UIManager manager, @NotNull Info info, @NotNull Class<?> type, @NotNull Function<GuildConfig, Object> categoryInstance, @NotNull Getter getter, @NotNull Setter setter, @NotNull Function<DataState<?>, String> display, @NotNull CategoryInfo category, @NotNull ConfigField field) {
		List<Component<?>> base = new ArrayList<>();
		List<ComponentRow> components = new ArrayList<>();

		//Add base components
		base.add(new ButtonComponent("back", ButtonColor.GRAY, "Zurück").appendHandler(s -> manager.getMenu("config." + category.command()).display(s.getEvent())));
		base.add(new ButtonComponent("reset", ButtonColor.RED, "Zurücksetzten").appendHandler(s -> {
			//For normal fields the default value is 'null'
			set(bot, s, categoryInstance, setter, null);
			s.update();
		}));

		components.add(ComponentRow.of(base));

		//Create component for setting the field value. Will be select menu or button that opens modal
		List<? extends ComponentRow> component = field.type().createAdvancedComponents(manager, type, info, "config." + category.command() + "." + field.command(), "value", "Wert festlegen", s -> get(bot, s, getter), (s, v) -> set(bot, s, categoryInstance, setter, v));

		boolean first = true;
		for (ComponentRow c : component) {
			if (first && c.size() <= 3) base.addAll(c.getComponents());
			else components.add(c);

			first = false;
		}

		//Create menu
		return manager.createMenu(
				"config." + category.command() + "." + field.command(),
				MessageRenderer.embed(s -> {
					Object value = get(bot, s, getter);

					return new EmbedBuilder()
							.setDescription("## " + category.name() + " → " + display.apply(s) + "\n")
							.setColor(bot.getColor(s.getEvent().getGuild()))
							.appendDescription(field.description())
							.appendDescription("\n### Aktueller Wert\n")
							.appendDescription(value == null ? "*nicht gesetzt*" : field.type().toString(value))
							.build();
				}),
				components
		);
	}

	@NotNull
	@SuppressWarnings({"rawtypes", "unchecked"})
	private static MessageMenu createListValueMenu(@NotNull SlimeBot bot, @NotNull UIManager manager, @NotNull Info info, @NotNull Class<?> type, @NotNull Type generic, @NotNull Function<GuildConfig, Object> categoryInstance, @NotNull Getter getter, @NotNull Setter setter, @NotNull Function<DataState<?>, String> display, @NotNull CategoryInfo category, @NotNull ConfigField field) {
		Type componentType = getGenericType(generic, 0);
		Class<?> componentClass = getClass(componentType);

		List<Component<?>> base = new ArrayList<>();
		List<ComponentRow> components = new ArrayList<>();

		//Add base components
		base.add(new ButtonComponent("back", ButtonColor.GRAY, "Zurück").appendHandler(s -> manager.getMenu("config." + category.command()).display(s.getEvent())));
		base.add(new ButtonComponent("reset", ButtonColor.RED, "Zurücksetzten").appendHandler(s -> {
			//We use empty collections as default here so that we don't have to handle the 'null' case
			set(bot, s, categoryInstance, setter, createEmptyCollection(type, generic));
			s.update();
		}));

		components.add(ComponentRow.of(base));

		//Create component for adding new values. Will be a select menu or a button that opens a modal
		Component<?> add = field.type().createComponent(manager, componentClass, info, "config." + category.command() + "." + field.command(), "add", "Wert hinzufügen", s -> get(bot, s, getter), (s, v) -> MenuCommand.<Collection>handle(bot, s, categoryInstance, getter, c -> c.add(v)));

		//Add remove select with current values
		Component<?> remove = new StringSelectComponent("remove", s -> MenuCommand.<Collection<?>>get(bot, s, getter).stream()
				.map(e -> field.type().createSelectOption(bot, e))
				.toList()
		).setPlaceholder("Wert entfernen").appendHandler((s, v) -> {
			MenuCommand.<Collection<?>>handle(bot, s, categoryInstance, getter, c -> c.remove(field.type().parse(componentClass, v.get(0).getValue())));
			s.update();
		});

		//Add component to the very top if it is as select menu
		if (add instanceof EntitySelectComponent || add instanceof StringSelectComponent) {
			components.add(0, add);
			components.add(1, remove);
		} else {
			components.add(add);
			components.add(remove);
		}

		//Create menu
		return manager.createMenu(
				"config." + category.command() + "." + field.command(),
				MessageRenderer.embed(s -> {
					Collection<?> value = get(bot, s, getter);

					return new EmbedBuilder()
							.setDescription("## " + category.name() + " → " + display.apply(s) + "\n")
							.setColor(bot.getColor(s.getEvent().getGuild()))
							.appendDescription(field.description())
							.appendDescription("\n### Aktuelle Einträge\n")
							.appendDescription(value.isEmpty() ? "*Keine Einträge*" : value.stream().map(e -> "- " + field.type().toString(e)).collect(Collectors.joining("\n")))
							.build();
				}),
				components
		);
	}

	@NotNull
	@SuppressWarnings({"rawtypes", "unchecked"})
	private static MessageMenu createEnumSetValueMenu(@NotNull SlimeBot bot, @NotNull UIManager manager, @NotNull Info info, @NotNull Type generic, @NotNull Function<GuildConfig, Object> categoryInstance, @NotNull Getter getter, @NotNull Setter setter, @NotNull Function<DataState<?>, String> display, @NotNull CategoryInfo category, @NotNull ConfigField field) {
		Class<?> componentClass = getClass(getGenericType(generic, 0));

		List<Component<?>> components = new ArrayList<>();

		//Add base components
		components.add(new ButtonComponent("back", ButtonColor.GRAY, "Zurück").appendHandler(s -> manager.getMenu("config." + category.command()).display(s.getEvent())));
		components.add(new ButtonComponent("reset", ButtonColor.RED, "Zurücksetzten").appendHandler(s -> {
			set(bot, s, categoryInstance, setter, emptyEnumSet(componentClass));
			s.update();
		}));

		//Add component to select which flags to use
		components.add(new StringSelectComponent("value", s -> Arrays.stream(componentClass.getEnumConstants())
				.map(e -> SelectOption.of(e.toString(), ((Enum<?>) e).name())
						.withDefault(MenuCommand.<EnumSet<?>>get(bot, s, getter).contains(e))
				)
				.toList()
		).setMinValues(0).setMaxValues(componentClass.getEnumConstants().length).appendHandler((s, v) -> {
			MenuCommand.<EnumSet>handle(bot, s, categoryInstance, getter, c -> {
				//Clear current values and add selected ones
				c.clear();
				c.addAll(v.stream().map(x -> Arrays.stream(componentClass.getEnumConstants()).map(e -> (Enum<?>) e).filter(e -> e.name().equals(x.getValue())).findFirst().orElseThrow()).toList());
			});

			s.update();
		}));

		//Create menu
		return manager.createMenu(
				"config." + category.command() + "." + field.command(),
				MessageRenderer.embed(s -> {
					EnumSet<?> value = get(bot, s, getter);

					return new EmbedBuilder()
							.setDescription("## " + category.name() + " → " + display.apply(s) + "\n")
							.setColor(bot.getColor(s.getEvent().getGuild()))
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
	private static MessageMenu createMapValueMenu(@NotNull SlimeBot bot, @NotNull UIManager manager, @NotNull Info info, @NotNull Class<?> type, @NotNull Type generic, @NotNull Function<GuildConfig, Object> categoryInstance, @NotNull Getter getter, @NotNull Setter setter, @NotNull Function<DataState<?>, String> display, @NotNull CategoryInfo category, @NotNull ConfigField field, @Nullable ConfigFieldType keyType) {
		Class<?> keyClass = getClass(getGenericType(generic, 0));

		Type valueType = getGenericType(generic, 1);
		Class<?> valueClass = getClass(valueType);

		//Check if type arguments are valid
		if (Map.class.isAssignableFrom(keyClass) || Collection.class.isAssignableFrom(keyClass)) throw new UnsupportedOperationException();
		if (Map.class.isAssignableFrom(valueClass)) throw new UnsupportedOperationException("Cannot create config menu with map inside of map");

		List<Component<?>> components = new ArrayList<>();

		//Add base components
		components.add(new ButtonComponent("back", ButtonColor.GRAY, "Zurück").appendHandler(s -> manager.getMenu("config." + category.command()).display(s.getEvent())));
		components.add(new ButtonComponent("reset", ButtonColor.RED, "Zurücksetzten").appendHandler(s -> {
			set(bot, s, categoryInstance, setter, type.isAssignableFrom(HashMap.class) ? new HashMap<>() : new LinkedHashMap<>());
			s.update();
		}));

		//Create virtual info environment for sub-menus
		CategoryInfo valueCategory = createCategory(category.name() + " → " + field.name(), category.command() + "." + field.command(), field.description());
		ConfigField valueField = createField("?", "value", field.description(), field.type());

		//The value menu shows the current value for a key. This can also be used to change the value
		MessageMenu valueMenu = createFieldMenu(bot, manager, info, valueType, categoryInstance,
				//Implementation for getter method
				(s, c) -> ((Map) getter.get(s, c)).computeIfAbsent(keyType.parse(keyClass, s.getState("key", String.class)), x -> Collection.class.isAssignableFrom(valueClass) ? createEmptyCollection(valueClass, valueType) : null),
				//Implementation for setter method
				(s, c, v) -> {
					Map value = (Map) getter.get(s, c);

					//Read key from state
					Object key = keyType.parse(keyClass, s.getState("key", String.class));

					if (v != null) value.put(key, v);
					else value.remove(key);
				},
				s -> s.getState("key", String.class), valueCategory, valueField, null
		);

		//Add components that opens the value menu to add a new entry
		Component<?> add = keyType.createComponent(manager, keyClass, info, "config." + category.command() + "." + field.command(), "add", "Wert hinzufügen", s -> "", (s, k) -> {
			valueMenu.createState(s).setState("key", keyType.toString(k)).display(s.getEvent());
			throw new RenderTermination();
		});

		//Add component to remove th value for an existing key
		Component<?> remove = new StringSelectComponent("remove", s -> MenuCommand.<Map<?, ?>>get(bot, s, getter).keySet().stream()
				.map(e -> keyType.createSelectOption(bot, e))
				.toList()
		).setPlaceholder("Wert entfernen").appendHandler((s, v) -> {
			MenuCommand.<Map<?, ?>>handle(bot, s, categoryInstance, getter, c -> c.remove(keyType.parse(keyClass, v.get(0).getValue())));
			s.update();
		});

		//Add component that opens the value menu for an existing key
		Component<?> edit = new StringSelectComponent("edit", s -> MenuCommand.<Map<?, ?>>get(bot, s, getter).keySet().stream()
				.map(e -> keyType.createSelectOption(bot, e))
				.toList()
		).setPlaceholder("Wert bearbeiten").appendHandler((s, v) -> valueMenu.createState(s).setState("key", v.get(0).getValue()).display(s.getEvent()));

		//Add component to the very top if it is as select menu
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
							.setColor(bot.getColor(s.getEvent().getGuild()))
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

	@NotNull
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

	@NotNull
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

	@NotNull
	private static Info createDefaultInfo() {
		return new Info() {
			@Override
			public ConfigFieldType keyType() {
				return ConfigFieldType.STRING;
			}

			@Override
			public double minValue() {
				return Double.MIN_VALUE;
			}

			@Override
			public double maxValue() {
				return Double.MAX_VALUE;
			}

			@Override
			public Class<? extends Annotation> annotationType() {
				return Info.class;
			}
		};
	}
}
