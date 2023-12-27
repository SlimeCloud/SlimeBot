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
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.function.Function;

public class ConfigFieldCommand extends Command<ICommandContext> {
	private final SlimeBot bot;
	private final CategoryInfo category;
	private final ConfigField info;
	private final Field field;
	private final Function<GuildConfig, Object> instance;

	private final MessageMenu menu;

	public ConfigFieldCommand(@NotNull SlimeBot bot, @NotNull CommandManager<ICommandContext, ?> manager, @NotNull Function<GuildConfig, Object> instance, @NotNull Field field, @NotNull CategoryInfo category, @NotNull ConfigField info, @NotNull MessageMenu menu) {
		super(manager, info.command(), info.description());

		this.bot = bot;
		this.category = category;
		this.info = info;
		this.field = field;
		this.instance = instance;
		this.menu = menu;

		addOption(info.type().getConfiguration().apply(field.getType(), new OptionData(info.type().getType(), "value", "Der Wert für diese Eigenschaft", false)));
	}

	@Override
	public void performCommand(@NotNull ICommandContext context) throws Exception {
		//Get option
		OptionMapping option = context.getEvent().getOption("value");

		//Show menu if no value is provided
		if (option == null) {
			menu.createState()
					//State is very limited (1 component -> about 90 characters. Therefore, we will shorten everything as good as possible)
					.setState("c", field.getDeclaringClass().getName().replace("de.slimecloud.slimeball.", ""))
					.setState("f", field.getName())
					.display(context.getEvent());
		} else {
			//Set value
			try {
				//Load config and category
				GuildConfig config = bot.loadGuild(context.getEvent().getGuild());
				Object instance = this.instance.apply(config);

				Object value = info.type().getExtractor().apply(field.getType(), option);
				field.set(instance, value);

				//Call update method
				if (instance instanceof ConfigCategory c) c.update();

				context.getEvent().reply("Konfiguration **" + category.name() + " -> " + info.name() + "** auf **" + info.type().getString().apply(value) + "** gesetzt").setEphemeral(true).queue();

				//Save
				config.save();

				bot.updateGuildCommands(context.getEvent().getGuild());
			} catch (ValidationException e) {
				context.getEvent().reply(":x: Ungültige Eingabe!").setEphemeral(true).queue();
			}
		}
	}
}
