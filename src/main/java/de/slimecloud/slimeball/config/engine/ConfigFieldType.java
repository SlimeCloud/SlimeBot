package de.slimecloud.slimeball.config.engine;

import de.mineking.discordutils.ui.UIManager;
import de.mineking.discordutils.ui.components.select.EntitySelectComponent;
import de.mineking.discordutils.ui.components.select.StringSelectComponent;
import de.mineking.discordutils.ui.components.types.Component;
import de.mineking.discordutils.ui.state.DataState;
import de.slimecloud.slimeball.util.ColorUtil;
import de.slimecloud.slimeball.util.StringUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

@Getter
@AllArgsConstructor
public enum ConfigFieldType {
	ALL_CHANNEL(OptionMapping::getAsLong) {
		@NotNull
		@Override
		public OptionData createOption(@NotNull Class<?> type, @NotNull ConfigField info) {
			return new OptionData(OptionType.CHANNEL, info.command(), info.description());
		}

		@NotNull
		@Override
		public Component<?> createComponent(@NotNull UIManager manager, @NotNull Class<?> type, @NotNull String menu, @NotNull String name, @NotNull String display, @NotNull BiConsumer<DataState<?>, Object> handler) {
			return new EntitySelectComponent(name, EntitySelectMenu.SelectTarget.CHANNEL)
					.setPlaceholder(display)
					.appendHandler((s, v) -> handler.accept(s, v.getChannels().get(0).getIdLong()));
		}

		@NotNull
		@Override
		public String toString(@NotNull Object value) {
			return "<#" + value + ">";
		}
	},

	MESSAGE_CHANNEL(OptionMapping::getAsLong) {
		@NotNull
		@Override
		public OptionData createOption(@NotNull Class<?> type, @NotNull ConfigField info) {
			return new OptionData(OptionType.CHANNEL, info.command(), info.description())
					.setChannelTypes(ChannelType.TEXT, ChannelType.NEWS, ChannelType.FORUM);
		}

		@NotNull
		@Override
		public Component<?> createComponent(@NotNull UIManager manager, @NotNull Class<?> type, @NotNull String menu, @NotNull String name, @NotNull String display, @NotNull BiConsumer<DataState<?>, Object> handler) {
			return new EntitySelectComponent(name, EntitySelectMenu.SelectTarget.CHANNEL)
					.setPlaceholder(display)
					.setChannelTypes(ChannelType.TEXT, ChannelType.NEWS, ChannelType.FORUM)
					.appendHandler((s, v) -> handler.accept(s, v.getChannels().get(0).getIdLong()));
		}

		@NotNull
		@Override
		public String toString(@NotNull Object value) {
			return "<#" + value + ">";
		}
	},

	VOICE_CHANNEL(OptionMapping::getAsLong) {
		@NotNull
		@Override
		public OptionData createOption(@NotNull Class<?> type, @NotNull ConfigField info) {
			return new OptionData(OptionType.CHANNEL, info.command(), info.description())
					.setChannelTypes(ChannelType.VOICE, ChannelType.STAGE);
		}

		@NotNull
		@Override
		public Component<?> createComponent(@NotNull UIManager manager, @NotNull Class<?> type, @NotNull String menu, @NotNull String name, @NotNull String display, @NotNull BiConsumer<DataState<?>, Object> handler) {
			return new EntitySelectComponent(name, EntitySelectMenu.SelectTarget.CHANNEL)
					.setPlaceholder(display)
					.setChannelTypes(ChannelType.VOICE, ChannelType.STAGE)
					.appendHandler((s, v) -> handler.accept(s, v.getChannels().get(0).getIdLong()));
		}

		@NotNull
		@Override
		public String toString(@NotNull Object value) {
			return "<#" + value + ">";
		}
	},

	ROLE(OptionMapping::getAsLong) {
		@NotNull
		@Override
		public OptionData createOption(@NotNull Class<?> type, @NotNull ConfigField info) {
			return new OptionData(OptionType.ROLE, info.command(), info.description());
		}

		@NotNull
		@Override
		public Component<?> createComponent(@NotNull UIManager manager, @NotNull Class<?> type, @NotNull String menu, @NotNull String name, @NotNull String display, @NotNull BiConsumer<DataState<?>, Object> handler) {
			return new EntitySelectComponent(name, EntitySelectMenu.SelectTarget.ROLE)
					.setPlaceholder(display)
					.appendHandler((s, v) -> handler.accept(s, v.getRoles().get(0).getIdLong()));
		}

		@NotNull
		@Override
		public String toString(@NotNull Object value) {
			return "<@" + value + ">";
		}
	},



	ENUM(StringUtil::extractEnum) {
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
		public Component<?> createComponent(@NotNull UIManager manager, @NotNull Class<?> type, @NotNull String menu, @NotNull String name, @NotNull String display, @NotNull BiConsumer<DataState<?>, Object> handler) {
			return new StringSelectComponent(name, Arrays.stream(type.getEnumConstants())
					.map(e -> SelectOption.of(e.toString(), ((Enum<?>) e).name()))
					.toList()
			).setPlaceholder(display).appendHandler((s, v) -> handler.accept(s, v.get(0).getValue()));
		}
	},


	COLOR(ColorUtil::extract) {
		@NotNull
		@Override
		public OptionData createOption(@NotNull Class<?> type, @NotNull ConfigField info) {
			return new OptionData(OptionType.STRING, info.description(), info.command())
					.setRequiredLength(4, 9);
		}

		@NotNull
		@Override
		public Component<?> createComponent(@NotNull UIManager manager, @NotNull Class<?> type, @NotNull String menu, @NotNull String name, @NotNull String display, @NotNull BiConsumer<DataState<?>, Object> handler) {
			return null;
		}

		@Override
		public boolean validate(@NotNull Class<?> type, @NotNull String value) {
			return ColorUtil.parseColor(value) != null;
		}

		@NotNull
		@Override
		public Object parse(@NotNull Class<?> type, @NotNull String value) {
			return value;
		}
	},


	URL(StringUtil::extractUrl) {
		@NotNull
		@Override
		public OptionData createOption(@NotNull Class<?> type, @NotNull ConfigField info) {
			return new OptionData(OptionType.STRING, info.description(), info.command());
		}

		@NotNull
		@Override
		public Component<?> createComponent(@NotNull UIManager manager, @NotNull Class<?> type, @NotNull String menu, @NotNull String name, @NotNull String display, @NotNull BiConsumer<DataState<?>, Object> handler) {
			return null;
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
	},


	STRING(OptionMapping::getAsString) {
		@NotNull
		@Override
		public OptionData createOption(@NotNull Class<?> type, @NotNull ConfigField info) {
			return new OptionData(OptionType.STRING, info.command(), info.description());
		}

		@NotNull
		@Override
		public Component<?> createComponent(@NotNull UIManager manager, @NotNull Class<?> type, @NotNull String menu, @NotNull String name, @NotNull String display, @NotNull BiConsumer<DataState<?>, Object> handler) {
			return null;
		}
	},

	INTEGER(OptionMapping::getAsInt) {
		@NotNull
		@Override
		public OptionData createOption(@NotNull Class<?> type, @NotNull ConfigField info) {
			return new OptionData(OptionType.INTEGER, info.command(), info.description());
		}

		@NotNull
		@Override
		public Component<?> createComponent(@NotNull UIManager manager, @NotNull Class<?> type, @NotNull String menu, @NotNull String name, @NotNull String display, @NotNull BiConsumer<DataState<?>, Object> handler) {
			return null;
		}
	},

	NUMBER(OptionMapping::getAsDouble) {
		@NotNull
		@Override
		public OptionData createOption(@NotNull Class<?> type, @NotNull ConfigField info) {
			return new OptionData(OptionType.NUMBER, info.command(), info.description());
		}

		@NotNull
		@Override
		public Component<?> createComponent(@NotNull UIManager manager, @NotNull Class<?> type, @NotNull String menu, @NotNull String name, @NotNull String display, @NotNull BiConsumer<DataState<?>, Object> handler) {
			return null;
		}
	};;

	private final BiFunction<Class<?>, OptionMapping, Object> extractor;

	@NotNull
	public abstract OptionData createOption(@NotNull Class<?> type, @NotNull ConfigField info);

	public boolean validate(@NotNull Class<?> type, @NotNull String value) {
		throw new RuntimeException();
	}

	@NotNull
	public Object parse(@NotNull Class<?> type, @NotNull String value) {
		throw new RuntimeException();
	}

	@NotNull
	public abstract Component<?> createComponent(@NotNull UIManager manager, @NotNull Class<?> type, @NotNull String menu, @NotNull String name, @NotNull String display, @NotNull BiConsumer<DataState<?>, Object> handler);

	@NotNull
	public String toString(@NotNull Object value) {
		return Objects.toString(value);
	}

	ConfigFieldType(@NotNull Function<OptionMapping, Object> extractor) {
		this((t, o) -> extractor.apply(o));
	}
}
