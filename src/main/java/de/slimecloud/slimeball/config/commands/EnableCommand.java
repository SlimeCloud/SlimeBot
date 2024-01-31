package de.slimecloud.slimeball.config.commands;

import de.mineking.discordutils.commands.Command;
import de.mineking.discordutils.commands.CommandManager;
import de.mineking.discordutils.commands.context.ICommandContext;
import de.slimecloud.slimeball.config.ConfigCategory;
import de.slimecloud.slimeball.config.GuildConfig;
import de.slimecloud.slimeball.config.engine.CategoryInfo;
import de.slimecloud.slimeball.config.engine.ConfigField;
import de.slimecloud.slimeball.config.engine.ValidationException;
import de.slimecloud.slimeball.main.Main;
import de.slimecloud.slimeball.main.SlimeBot;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class EnableCommand extends Command<ICommandContext> {
	private final SlimeBot bot;
	private final Function<GuildConfig, Object> instance;
	private final Field field;
	private final CategoryInfo category;
	private final Map<String, Field> fields;

	public EnableCommand(@NotNull SlimeBot bot, @NotNull CommandManager<ICommandContext, ?> manager, @NotNull Function<GuildConfig, Object> instance, @NotNull Field field, @NotNull CategoryInfo category, @NotNull Map<String, Field> fields, @NotNull Set<Field> required, @NotNull Set<Field> optional) {
		super(manager, "enable", "Aktiviert dieses Feature");

		this.bot = bot;
		this.instance = instance;
		this.field = field;
		this.category = category;

		this.fields = fields;

		required.forEach(f -> addOption(getOption(f)));
		optional.forEach(f -> {
			try {
				addOption(getOption(f));
			} catch (IllegalArgumentException ignore) {
			}
		});
	}

	@NotNull
	public static OptionData getOption(@NotNull Field f) {
		ConfigField info = f.getAnnotation(ConfigField.class);

		if (Collection.class.isAssignableFrom(f.getType())) throw new IllegalArgumentException("Cannot use collection types in this context!");
		if (Map.class.isAssignableFrom(f.getType())) throw new IllegalArgumentException("Cannot use map types in this context!");

		return info.type().createOption(f.getType(), info).setRequired(info.required());
	}

	@Override
	public void performCommand(@NotNull ICommandContext context) throws Exception {
		context.getEvent().deferReply(true).queue();

		//Load config
		GuildConfig config = bot.loadGuild(context.getEvent().getGuild());

		if (field.get(config) != null) {
			context.getEvent().getHook().editOriginal(":x: Feature ist bereits aktiviert!").queue();
			return;
		}

		//Get or create category instance
		Object instance = this.instance.apply(config);

		//Basic enable
		if (!getOptions().isEmpty()) {
			//Extract values from options
			Map<Field, Object> values = new HashMap<>();
			try {
				for (OptionMapping m : context.getEvent().getOptions()) {
					Field f = fields.get(m.getName());
					values.put(f, f.getAnnotation(ConfigField.class).type().getExtractor().apply(f.getType(), m));
				}
			} catch (ValidationException e) {
				context.getEvent().getHook().editOriginal(":x: Ungültige Eingabe!").queue();
				return;
			}

			values.forEach((f, v) -> {
				try {
					f.set(instance, v);
				} catch (IllegalAccessException e) {
					throw new RuntimeException(e);
				}
			});
		}

		//Call init method
		if (instance instanceof ConfigCategory c) {
			c.bot = bot;
			c.enable(context.getEvent().getGuild());
		}

		//Save
		config.save();
		context.getEvent().getHook().editOriginal("✅ Feature **" + category.name() + "** aktiviert\n```json\n" + Main.formattedJson.toJson(instance) + "```").queue();

		//Update commands to add commands that might be affected by this
		bot.updateGuildCommands(context.getEvent().getGuild());
	}
}
