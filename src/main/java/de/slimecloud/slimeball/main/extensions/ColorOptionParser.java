package de.slimecloud.slimeball.main.extensions;

import de.mineking.discordutils.commands.Command;
import de.mineking.discordutils.commands.CommandCancellation;
import de.mineking.discordutils.commands.CommandManager;
import de.mineking.discordutils.commands.option.OptionParser;
import de.slimecloud.slimeball.util.ColorUtil;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

public class ColorOptionParser extends OptionParser {
	public ColorOptionParser() {
		super(Color.class, OptionType.STRING);
	}

	@Nullable
	@Override
	public Object parse(@NotNull CommandManager<?, ?> manager, @NotNull GenericCommandInteractionEvent event, @NotNull String name, @NotNull Parameter param, @NotNull Type type) {
		return event.getOption(name, o -> {
			Color color = ColorUtil.parseColor(o.getAsString()); //Try to parse color
			if (color == null) {
				event.reply(":x: Ungültige Farbe").setEphemeral(true).queue();
				throw new CommandCancellation(); //This will silently cancel the command execution
			}
			return color;
		});
	}

	@NotNull
	@Override
	public OptionData configure(@NotNull Command<?> command, @NotNull OptionData option, @NotNull Parameter param, @NotNull Type type) {
		//#fff, 255,255,255,255
		return option.setRequiredLength(4, 4 * 3 + 3);
	}
}
