package de.slimecloud.slimeball.config.engine;

import de.mineking.discordutils.ui.UIManager;
import de.mineking.discordutils.ui.components.button.ButtonColor;
import de.mineking.discordutils.ui.components.button.ButtonComponent;
import de.mineking.discordutils.ui.components.button.MenuComponent;
import de.mineking.discordutils.ui.components.button.label.TextLabel;
import de.mineking.discordutils.ui.components.select.EntitySelectComponent;
import de.mineking.discordutils.ui.components.select.StringSelectComponent;
import de.mineking.discordutils.ui.components.types.Component;
import de.mineking.discordutils.ui.components.types.ComponentRow;
import de.mineking.discordutils.ui.modal.ModalMenu;
import de.mineking.discordutils.ui.modal.TextComponent;
import de.mineking.discordutils.ui.state.DataState;
import de.slimecloud.slimeball.main.SlimeBot;
import de.slimecloud.slimeball.util.ColorUtil;
import de.slimecloud.slimeball.util.StringUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

@Getter
@AllArgsConstructor
public enum ConfigFieldType {
	ALL_CHANNEL("Kanal (Alle)", OptionMapping::getAsLong) {
		@NotNull
		@Override
		public SelectOption createSelectOption(@NotNull SlimeBot bot, @NotNull Object value) {
			return SelectOption.of(bot.getJda().getChannelById(Channel.class, (Long) value).getName(), value.toString());
		}

		@NotNull
		@Override
		public OptionData createOption(@NotNull Class<?> type, @NotNull ConfigField info) {
			return new OptionData(OptionType.CHANNEL, info.command(), info.description());
		}

		@NotNull
		@Override
		public ModalMenu getModal(@NotNull UIManager manager, @NotNull Class<?> type, @NotNull String menu, @NotNull String name, @NotNull String display, @NotNull Function<DataState<?>, Object> value, @NotNull BiConsumer<DataState<?>, Object> handler) {
			throw new UnsupportedOperationException();
		}

		@NotNull
		@Override
		public Component<?> createComponent(@NotNull UIManager manager, @NotNull Class<?> type, @NotNull Info info, @NotNull String menu, @NotNull String name, @NotNull String display, @NotNull Function<DataState<?>, Object> value, @NotNull BiConsumer<DataState<?>, Object> handler) {
			return new EntitySelectComponent(name, EntitySelectMenu.SelectTarget.CHANNEL)
					.setPlaceholder(display)
					.appendHandler((s, v) -> {
						handler.accept(s, v.getChannels().get(0).getIdLong());
						s.update();
					});
		}

		@NotNull
		@Override
		public Object parse(@NotNull Class<?> type, @NotNull String value) {
			return Long.parseLong(value.replaceAll("[<#>]", ""));
		}

		@NotNull
		@Override
		public String toString(@NotNull Object value) {
			return "<#" + value + ">";
		}
	},

	MESSAGE_CHANNEL("Kanal (Text)", OptionMapping::getAsLong) {
		@NotNull
		@Override
		public SelectOption createSelectOption(@NotNull SlimeBot bot, @NotNull Object value) {
			return SelectOption.of(bot.getJda().getChannelById(Channel.class, (Long) value).getName(), value.toString());
		}

		@NotNull
		@Override
		public OptionData createOption(@NotNull Class<?> type, @NotNull ConfigField info) {
			return new OptionData(OptionType.CHANNEL, info.command(), info.description())
					.setChannelTypes(ChannelType.TEXT, ChannelType.NEWS, ChannelType.FORUM, ChannelType.GUILD_NEWS_THREAD, ChannelType.GUILD_PRIVATE_THREAD, ChannelType.GUILD_PUBLIC_THREAD);
		}

		@NotNull
		@Override
		public ModalMenu getModal(@NotNull UIManager manager, @NotNull Class<?> type, @NotNull String menu, @NotNull String name, @NotNull String display, @NotNull Function<DataState<?>, Object> value, @NotNull BiConsumer<DataState<?>, Object> handler) {
			throw new UnsupportedOperationException();
		}

		@NotNull
		@Override
		public Component<?> createComponent(@NotNull UIManager manager, @NotNull Class<?> type, @NotNull Info info, @NotNull String menu, @NotNull String name, @NotNull String display, @NotNull Function<DataState<?>, Object> value, @NotNull BiConsumer<DataState<?>, Object> handler) {
			return new EntitySelectComponent(name, EntitySelectMenu.SelectTarget.CHANNEL)
					.setPlaceholder(display)
					.setChannelTypes(ChannelType.TEXT, ChannelType.NEWS, ChannelType.FORUM, ChannelType.GUILD_NEWS_THREAD, ChannelType.GUILD_PRIVATE_THREAD, ChannelType.GUILD_PUBLIC_THREAD)
					.appendHandler((s, v) -> {
						handler.accept(s, v.getChannels().get(0).getIdLong());
						s.update();
					});
		}

		@NotNull
		@Override
		public Object parse(@NotNull Class<?> type, @NotNull String value) {
			return Long.parseLong(value.replaceAll("[<#>]", ""));
		}

		@NotNull
		@Override
		public String toString(@NotNull Object value) {
			return "<#" + value + ">";
		}
	},

	VOICE_CHANNEL("Kanal (Voice)", OptionMapping::getAsLong) {
		@NotNull
		@Override
		public SelectOption createSelectOption(@NotNull SlimeBot bot, @NotNull Object value) {
			return SelectOption.of(bot.getJda().getChannelById(Channel.class, (Long) value).getName(), value.toString());
		}

		@NotNull
		@Override
		public OptionData createOption(@NotNull Class<?> type, @NotNull ConfigField info) {
			return new OptionData(OptionType.CHANNEL, info.command(), info.description())
					.setChannelTypes(ChannelType.VOICE, ChannelType.STAGE);
		}

		@NotNull
		@Override
		public ModalMenu getModal(@NotNull UIManager manager, @NotNull Class<?> type, @NotNull String menu, @NotNull String name, @NotNull String display, @NotNull Function<DataState<?>, Object> value, @NotNull BiConsumer<DataState<?>, Object> handler) {
			throw new UnsupportedOperationException();
		}

		@NotNull
		@Override
		public Component<?> createComponent(@NotNull UIManager manager, @NotNull Class<?> type, @NotNull Info info, @NotNull String menu, @NotNull String name, @NotNull String display, @NotNull Function<DataState<?>, Object> value, @NotNull BiConsumer<DataState<?>, Object> handler) {
			return new EntitySelectComponent(name, EntitySelectMenu.SelectTarget.CHANNEL)
					.setPlaceholder(display)
					.setChannelTypes(ChannelType.VOICE, ChannelType.STAGE)
					.appendHandler((s, v) -> {
						handler.accept(s, v.getChannels().get(0).getIdLong());
						s.update();
					});
		}

		@NotNull
		@Override
		public Object parse(@NotNull Class<?> type, @NotNull String value) {
			return Long.parseLong(value.replaceAll("[<#>]", ""));
		}

		@NotNull
		@Override
		public String toString(@NotNull Object value) {
			return "<#" + value + ">";
		}
	},

	ROLE("Rolle", OptionMapping::getAsLong) {
		@NotNull
		@Override
		public SelectOption createSelectOption(@NotNull SlimeBot bot, @NotNull Object value) {
			return SelectOption.of(bot.getJda().getRoleById((Long) value).getName(), value.toString());
		}

		@NotNull
		@Override
		public OptionData createOption(@NotNull Class<?> type, @NotNull ConfigField info) {
			return new OptionData(OptionType.ROLE, info.command(), info.description());
		}

		@NotNull
		@Override
		public ModalMenu getModal(@NotNull UIManager manager, @NotNull Class<?> type, @NotNull String menu, @NotNull String name, @NotNull String display, @NotNull Function<DataState<?>, Object> value, @NotNull BiConsumer<DataState<?>, Object> handler) {
			throw new UnsupportedOperationException();
		}

		@NotNull
		@Override
		public Component<?> createComponent(@NotNull UIManager manager, @NotNull Class<?> type, @NotNull Info info, @NotNull String menu, @NotNull String name, @NotNull String display, @NotNull Function<DataState<?>, Object> value, @NotNull BiConsumer<DataState<?>, Object> handler) {
			return new EntitySelectComponent(name, EntitySelectMenu.SelectTarget.ROLE)
					.setPlaceholder(display)
					.appendHandler((s, v) -> {
						handler.accept(s, v.getRoles().get(0).getIdLong());
						s.update();
					});
		}

		@NotNull
		@Override
		public Object parse(@NotNull Class<?> type, @NotNull String value) {
			return Long.parseLong(value.replaceAll("[<@&>]", ""));
		}

		@NotNull
		@Override
		public String toString(@NotNull Object value) {
			return "<@&" + value + ">";
		}
	},


	ENUM("Select", StringUtil::extractEnum) {
		@NotNull
		@Override
		public SelectOption createSelectOption(@NotNull SlimeBot bot, @NotNull Object value) {
			return SelectOption.of(value.toString(), ((Enum<?>) value).name());
		}

		@NotNull
		@Override
		public ModalMenu getModal(@NotNull UIManager manager, @NotNull Class<?> type, @NotNull String menu, @NotNull String name, @NotNull String display, @NotNull Function<DataState<?>, Object> value, @NotNull BiConsumer<DataState<?>, Object> handler) {
			throw new UnsupportedOperationException();
		}

		@NotNull
		@Override
		public OptionData createOption(@NotNull Class<?> type, @NotNull ConfigField info) {
			return new OptionData(OptionType.STRING, info.command(), info.description())
					.addChoices(Arrays.stream(type.getEnumConstants())
							.map(e -> new Command.Choice(e.toString(), ((Enum<?>) e).name()))
							.toList()
					);
		}

		@NotNull
		@Override
		public Component<?> createComponent(@NotNull UIManager manager, @NotNull Class<?> type, @NotNull Info info, @NotNull String menu, @NotNull String name, @NotNull String display, @NotNull Function<DataState<?>, Object> value, @NotNull BiConsumer<DataState<?>, Object> handler) {
			return new StringSelectComponent(name, Arrays.stream(type.getEnumConstants())
					.map(e -> SelectOption.of(e.toString(), ((Enum<?>) e).name()))
					.toList()
			).setPlaceholder(display).appendHandler((s, v) -> {
				handler.accept(s, v.get(0).getValue());
				s.update();
			});
		}

		@Override
		public boolean validate(@NotNull Class<?> type, @NotNull String value) {
			return Arrays.stream(type.getEnumConstants()).anyMatch(e -> ((Enum<?>) e).name().equals(value));
		}

		@NotNull
		@Override
		public Object parse(@NotNull Class<?> type, @NotNull String value) {
			return Arrays.stream(type.getEnumConstants())
					.filter(e -> ((Enum<?>) e).name().equals(value))
					.findFirst().orElseThrow();
		}
	},


	COLOR("Farbe", ColorUtil::extract) {
		@NotNull
		@Override
		public OptionData createOption(@NotNull Class<?> type, @NotNull ConfigField info) {
			return new OptionData(OptionType.STRING, info.description(), info.command())
					.setRequiredLength(4, 9);
		}

		@Override
		public boolean validate(@NotNull Class<?> type, @NotNull String value) {
			return ColorUtil.parseColor(value) != null;
		}

		@NotNull
		@Override
		public Object parse(@NotNull Class<?> type, @NotNull String value) {
			return type.equals(Color.class) ? ColorUtil.parseColor(value) : value;
		}

		@NotNull
		@Override
		public String toString(@NotNull Object value) {
			if (value instanceof Color c) return ColorUtil.toString(c);
			else return super.toString(value);
		}
	},


	URL("Link", StringUtil::extractUrl) {
		@NotNull
		@Override
		public OptionData createOption(@NotNull Class<?> type, @NotNull ConfigField info) {
			return new OptionData(OptionType.STRING, info.description(), info.command());
		}

		@Override
		public boolean validate(@NotNull Class<?> type, @NotNull String value) {
			return StringUtil.isValidURL(value);
		}

		@NotNull
		@Override
		public Object parse(@NotNull Class<?> type, @NotNull String value) {
			return value;
		}

		@NotNull
		@Override
		public String toString(@NotNull Object value) {
			return value == null ? "" : value.toString();
		}
	},


	STRING("Text", OptionMapping::getAsString) {
		@NotNull
		@Override
		public OptionData createOption(@NotNull Class<?> type, @NotNull ConfigField info) {
			return new OptionData(OptionType.STRING, info.command(), info.description());
		}

		@NotNull
		@Override
		public Object parse(@NotNull Class<?> type, @NotNull String value) {
			return value;
		}
	},

	INTEGER("Ganze Zahl", OptionMapping::getAsInt) {
		@NotNull
		@Override
		public OptionData createOption(@NotNull Class<?> type, @NotNull ConfigField info) {
			return new OptionData(OptionType.INTEGER, info.command(), info.description());
		}

		@Override
		public boolean validate(@NotNull Class<?> type, @NotNull String value) {
			return StringUtil.isInteger(value);
		}

		@NotNull
		@Override
		public Object parse(@NotNull Class<?> type, @NotNull String value) {
			return Integer.parseInt(value);
		}
	},

	INTEGER_UI("Ganze Zahl", OptionMapping::getAsInt) {
		@NotNull
		@Override
		public OptionData createOption(@NotNull Class<?> type, @NotNull ConfigField info) {
			return new OptionData(OptionType.INTEGER, info.command(), info.description());
		}

		@Override
		public List<? extends ComponentRow> createAdvancedComponents(@NotNull UIManager manager, @NotNull Class<?> type, @NotNull Info info, @NotNull String menu, @NotNull String name, @NotNull String display, @NotNull Function<DataState<?>, Object> value, @NotNull BiConsumer<DataState<?>, Object> handler) {
			return List.of(
					super.createComponent(manager, type, info, menu, name, display, value, handler),
					ComponentRow.of(
							new ButtonComponent(name + ".subtract", ButtonColor.BLUE, "-").asDisabled(s -> (Integer) value.apply(s) <= info.minValue()).appendHandler(s -> {
								handler.accept(s, (Integer) value.apply(s) - 1);
								s.update();
							}),
							new MenuComponent<>(getModal(manager, type, menu, name + ".display", display, value, handler), ButtonColor.GRAY, (TextLabel) s -> "\uD83D\uDCDD " + value.apply(s)).setStateCreator(ModalMenu::createState),
							new ButtonComponent(name + ".add", ButtonColor.BLUE, "+").asDisabled(s -> (Integer) value.apply(s) >= info.maxValue()).appendHandler(s -> {
								handler.accept(s, (Integer) value.apply(s) + 1);
								s.update();
							})
					)
			);
		}

		@Override
		public boolean validate(@NotNull Class<?> type, @NotNull String value) {
			return StringUtil.isInteger(value);
		}

		@NotNull
		@Override
		public Object parse(@NotNull Class<?> type, @NotNull String value) {
			return Integer.parseInt(value);
		}
	},

	NUMBER("Fließkomma Zahl", OptionMapping::getAsDouble) {
		@NotNull
		@Override
		public OptionData createOption(@NotNull Class<?> type, @NotNull ConfigField info) {
			return new OptionData(OptionType.NUMBER, info.command(), info.description());
		}

		@Override
		public boolean validate(@NotNull Class<?> type, @NotNull String value) {
			return StringUtil.isNumeric(value);
		}

		@NotNull
		@Override
		public Object parse(@NotNull Class<?> type, @NotNull String value) {
			return Double.parseDouble(value);
		}
	},

	NUMBER_UI("Fließkomma Zahl", OptionMapping::getAsInt) {
		@NotNull
		@Override
		public OptionData createOption(@NotNull Class<?> type, @NotNull ConfigField info) {
			return new OptionData(OptionType.NUMBER, info.command(), info.description());
		}

		@Override
		public List<? extends ComponentRow> createAdvancedComponents(@NotNull UIManager manager, @NotNull Class<?> type, @NotNull Info info, @NotNull String menu, @NotNull String name, @NotNull String display, @NotNull Function<DataState<?>, Object> value, @NotNull BiConsumer<DataState<?>, Object> handler) {
			return List.of(ComponentRow.of(
					new ButtonComponent(name + ".subtract", ButtonColor.BLUE, "-").asDisabled(s -> (Double) value.apply(s) - 1 < info.minValue()).appendHandler(s -> {
						handler.accept(s, (Double) value.apply(s) - 1);
						s.update();
					}),
					new MenuComponent<>(getModal(manager, type, menu, name + ".display", display, value, handler), ButtonColor.GRAY, (TextLabel) s -> "\uD83D\uDCDD " + value.apply(s)).setStateCreator(ModalMenu::createState),
					new ButtonComponent(name + ".add", ButtonColor.BLUE, "+").asDisabled(s -> (Double) value.apply(s) + 1 > info.maxValue()).appendHandler(s -> {
						handler.accept(s, (Double) value.apply(s) + 1);
						s.update();
					})
			));
		}

		@Override
		public boolean validate(@NotNull Class<?> type, @NotNull String value) {
			return StringUtil.isNumeric(value);
		}

		@NotNull
		@Override
		public Object parse(@NotNull Class<?> type, @NotNull String value) {
			return Double.parseDouble(value);
		}
	};

	private final String name;
	private final BiFunction<Class<?>, OptionMapping, Object> extractor;

	@NotNull
	public abstract OptionData createOption(@NotNull Class<?> type, @NotNull ConfigField info);

	@NotNull
	public SelectOption createSelectOption(@NotNull SlimeBot bot, @NotNull Object value) {
		return SelectOption.of(value.toString(), value.toString());
	}

	@NotNull
	public ModalMenu getModal(@NotNull UIManager manager, @NotNull Class<?> type, @NotNull String menu, @NotNull String name, @NotNull String display, @NotNull Function<DataState<?>, Object> value, @NotNull BiConsumer<DataState<?>, Object> handler) throws UnsupportedOperationException {
		return manager.createModal(menu + "." + name,
				s -> display,
				List.of(new TextComponent("value", "Neuer Wert", TextInputStyle.SHORT)
						.setPlaceholder(this.name)
						.setValue(s -> {
							Object v = value.apply(s);
							String temp = toString(v);

							return (v == null && temp.equals("null")) || temp.isEmpty() ? null : temp;
						})
				),
				(s, m) -> {
					if (validate(type, m.getString("value"))) {
						handler.accept(s, parse(type, m.getString("value")));
						manager.getMenu(menu).createState(s).display(s.getEvent());
					} else {
						manager.getMenu(menu).display(s.getEvent());
						s.getEvent().getHook().sendMessage(":x: Ungültiger Wert").setEphemeral(true).queue();
					}
				}
		);
	}

	public List<? extends ComponentRow> createAdvancedComponents(@NotNull UIManager manager, @NotNull Class<?> type, @NotNull Info info, @NotNull String menu, @NotNull String name, @NotNull String display, @NotNull Function<DataState<?>, Object> value, @NotNull BiConsumer<DataState<?>, Object> handler) {
		return List.of(createComponent(manager, type, info, menu, name, display, value, handler));
	}

	@NotNull
	public Component<?> createComponent(@NotNull UIManager manager, @NotNull Class<?> type, @NotNull Info info, @NotNull String menu, @NotNull String name, @NotNull String display, @NotNull Function<DataState<?>, Object> value, @NotNull BiConsumer<DataState<?>, Object> handler) {
		return new MenuComponent<>(getModal(manager, type, menu, name, display, value, handler), ButtonColor.BLUE, display).setStateCreator(ModalMenu::createState);
	}

	public boolean validate(@NotNull Class<?> type, @NotNull String value) {
		return true;
	}

	@NotNull
	public abstract Object parse(@NotNull Class<?> type, @NotNull String value);

	@NotNull
	public String toString(@NotNull Object value) {
		return Objects.toString(value);
	}

	ConfigFieldType(@NotNull String name, @NotNull Function<OptionMapping, Object> extractor) {
		this(name, (t, o) -> extractor.apply(o));
	}
}
