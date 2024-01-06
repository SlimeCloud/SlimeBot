package de.slimecloud.slimeball.main.extensions;

import de.mineking.discordutils.commands.CommandCancellation;
import de.mineking.discordutils.commands.CommandManager;
import de.mineking.discordutils.commands.option.IOptionParser;
import de.mineking.javautils.ID;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.nio.BufferUnderflowException;

public class IDOptionParser implements IOptionParser {
	@Override
	public boolean accepts(@NotNull Type type, @NotNull Parameter param) {
		return type.equals(ID.class);
	}

	@NotNull
	@Override
	public OptionType getType(@NotNull CommandManager<?, ?> manager, @NotNull Type type, @NotNull Parameter param) {
		return OptionType.STRING;
	}

	@Nullable
	@Override
	public Object parse(@NotNull CommandManager<?, ?> manager, @NotNull GenericCommandInteractionEvent event, @NotNull String name, @NotNull Parameter param, @NotNull Type type) {
		try {
			return event.getOption(name, o -> {
				ID id = ID.decode(o.getAsString());
				id.getTimeCreated(); //Throws exception when invalid
				return id;
			});
		} catch (IllegalArgumentException | BufferUnderflowException e) {
			event.reply(":x: Ungültige ID").setEphemeral(true).queue();
			throw new CommandCancellation();
		}
	}
}
