package de.slimecloud.slimeball.main.extensions;

import de.mineking.discordutils.commands.CommandManager;
import de.mineking.discordutils.commands.option.IOptionParser;
import de.mineking.javautils.ID;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

public class IDOptionParser implements IOptionParser {
	@Override
	public boolean accepts(@NotNull Class<?> type, @NotNull Parameter param) {
		return type.isAssignableFrom(ID.class);
	}

	@NotNull
	@Override
	public OptionType getType(@NotNull CommandManager<?, ?> manager, @NotNull Class<?> type, @NotNull Type generic, @NotNull Parameter param) {
		return OptionType.STRING;
	}

	@Nullable
	@Override
	public Object parse(@NotNull CommandManager<?, ?> manager, @NotNull GenericCommandInteractionEvent event, @NotNull String name, @NotNull Parameter param, @NotNull Class<?> type, @NotNull Type generic) {
		return event.getOption(name, o -> ID.decode(o.getAsString()));
	}
}
