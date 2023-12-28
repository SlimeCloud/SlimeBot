package de.slimecloud.slimeball.config.commands;

import de.mineking.discordutils.commands.Command;
import de.mineking.discordutils.commands.CommandManager;
import de.mineking.discordutils.commands.context.ICommandContext;
import de.mineking.discordutils.ui.MessageMenu;
import de.slimecloud.slimeball.config.ConfigCategory;
import de.slimecloud.slimeball.config.GuildConfig;
import de.slimecloud.slimeball.config.engine.CategoryInfo;
import de.slimecloud.slimeball.config.engine.ConfigField;
import de.slimecloud.slimeball.config.engine.ValidationException;
import de.slimecloud.slimeball.main.SlimeBot;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class ConfigCollectionCommand extends Command<ICommandContext> {
	public ConfigCollectionCommand(@NotNull SlimeBot bot, @NotNull CommandManager<ICommandContext, ?> manager, @NotNull Function<GuildConfig, Object> instance, @NotNull Field field, @NotNull CategoryInfo category, @NotNull ConfigField info, @NotNull MessageMenu menu) {
		super(manager, info.command(), info.description());

		addSubcommand(new AddCommand(bot, manager, instance, field, category, info));
		addSubcommand(new RemoveCommand(bot, manager, instance, field, category, info));
		addSubcommand(new ListCommand(manager, field, menu));
	}

	public static Class<?> getGenericType(@NotNull Field field, int index) {
		return (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[index];
	}

	@SuppressWarnings("unchecked")
	private static <T> void modify(@NotNull Object collection, @NotNull BiConsumer<Collection<T>, T> handler, @Nullable Object value) {
		handler.accept((Collection<T>) collection, (T) value);
	}

	@Override
	public void performCommand(@NotNull ICommandContext context) throws Exception {
	}

	public static class AddCommand extends Command<ICommandContext> {
		private final SlimeBot bot;
		private final Function<GuildConfig, Object> instance;
		private final Field field;
		private final CategoryInfo category;
		private final ConfigField info;

		public AddCommand(@NotNull SlimeBot bot, @NotNull CommandManager<ICommandContext, ?> manager, @NotNull Function<GuildConfig, Object> instance, @NotNull Field field, @NotNull CategoryInfo category, @NotNull ConfigField info) {
			super(manager, "add", "Fügt einen Eintrag hinzu");

			this.bot = bot;
			this.instance = instance;
			this.field = field;
			this.category = category;
			this.info = info;

			addOption(info.type().getConfiguration().apply(getGenericType(field, 0), new OptionData(info.type().getType(), "value", "Element, das hinzugefügt wird", true)));
		}

		@Override
		public void performCommand(@NotNull ICommandContext context) throws Exception {
			try {
				//Load config and category
				GuildConfig config = bot.loadGuild(context.getEvent().getGuild());
				Object instance = this.instance.apply(config);

				//Add entry
				Object value = info.type().getExtractor().apply(getGenericType(field, 0), context.getEvent().getOption("value"));
				modify(field.get(instance), Collection::add, value);

				//Call update method
				if (instance instanceof ConfigCategory c) c.update();

				//Save
				config.save();
				context.getEvent().reply("**" + info.type().getString().apply(value) + "** zu **" + category.name() + " -> " + info.name() + "** hinzugefügt").setEphemeral(true).queue();
			} catch (ValidationException e) {
				context.getEvent().reply(":x: Ungültige Eingabe!").setEphemeral(true).queue();
			}
		}
	}

	public static class RemoveCommand extends Command<ICommandContext> {
		private final SlimeBot bot;
		private final Function<GuildConfig, Object> instance;
		private final Field field;
		private final CategoryInfo category;
		private final ConfigField info;

		public RemoveCommand(@NotNull SlimeBot bot, @NotNull CommandManager<ICommandContext, ?> manager, @NotNull Function<GuildConfig, Object> instance, @NotNull Field field, @NotNull CategoryInfo category, @NotNull ConfigField info) {
			super(manager, "remove", "Entfernt einen Eintrag");

			this.bot = bot;
			this.instance = instance;
			this.field = field;
			this.category = category;
			this.info = info;

			addOption(info.type().getConfiguration().apply(getGenericType(field, 0), new OptionData(info.type().getType(), "value", "Element, das entfernt wird", true)));
		}

		@Override
		public void performCommand(@NotNull ICommandContext context) throws Exception {
			try {
				//Load config and category
				GuildConfig config = bot.loadGuild(context.getEvent().getGuild());
				Object instance = this.instance.apply(config);

				//Add entry
				Object value = info.type().getExtractor().apply(getGenericType(field, 0), context.getEvent().getOption("value"));
				modify(field.get(instance), Collection::remove, value);

				//Call update method
				if (instance instanceof ConfigCategory c) c.update();

				//Save
				config.save();
				context.getEvent().reply("**" + info.type().getString().apply(value) + "** von **" + category.name() + " -> " + info.name() + "** entfernt").setEphemeral(true).queue();
			} catch (ValidationException e) {
				context.getEvent().reply(":x: Ungültige Eingabe!").setEphemeral(true).queue();
			}
		}
	}

	public static class ListCommand extends Command<ICommandContext> {
		private final Field field;
		private final MessageMenu menu;

		public ListCommand(@NotNull CommandManager<ICommandContext, ?> manager, @NotNull Field field, @NotNull MessageMenu menu) {
			super(manager, "display", "Zeigt alle Einträge an");

			this.menu = menu;
			this.field = field;
		}

		@Override
		public void performCommand(@NotNull ICommandContext context) throws Exception {
			menu.createState()
					//State is very limited (1 component -> about 90 characters. Therefore, we will shorten everything as good as possible)
					.setState("c", field.getDeclaringClass().getName().replace("de.slimecloud.slimeball.", ""))
					.setState("f", field.getName())
					.display(context.getEvent());
		}
	}
}
