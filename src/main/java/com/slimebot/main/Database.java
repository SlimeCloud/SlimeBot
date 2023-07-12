package com.slimebot.main;

import com.slimebot.message.StaffRole;
import com.slimebot.report.assets.Report;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;

import java.awt.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class Database {
	public final Jdbi jdbi;

	public Database() {
		jdbi = Jdbi.create("jdbc:postgresql://" + Main.config.database.host, Main.config.database.user, Main.config.database.password);

		run(handle -> {
			handle.createUpdate("create table if not exists guild_config(guild bigint, color text, logChannel bigint, greetingsChannel bigint, punishmentChannel bigint, staffRole bigint)").execute();
			handle.createUpdate("create table if not exists fdmds(guild bigint, channel bigint, logChannel bigint, role bigint)").execute();

			handle.createUpdate("create table if not exists spotify(guild bigint, notificationRole bigint, podcastChannel bigint, musicChannel bigint)").execute();
			handle.createUpdate("create table if not exists spotify_known(id text)").execute();

			handle.createUpdate("create table if not exists staff_roles(guild bigint, role bigint, description text)").execute();
			handle.createUpdate("create table if not exists staff_config(guild bigint, channel bigint, message bigint)").execute();

			handle.createUpdate("create table if not exists report_blocks(guild bigint, \"user\" bigint)").execute();
			handle.createUpdate("create table if not exists reports(guild bigint, id serial, issuer bigint, target bigint, type text, time timestamp default now(), message text, status text default 'OPEN', closeReason text)").execute();
		});

		jdbi.registerRowMapper(Report.class, new Report.ReportRowMapper());
		jdbi.registerRowMapper(StaffRole.class, new StaffRole.StaffRoleRowMapper());
	}

	public <T, U> U handle(Class<T> type, Function<T, U> handler) {
		return jdbi.withExtension(type, handler::apply);
	}

	public <T> void run(Class<T> type, Consumer<T> handler) {
		jdbi.useExtension(type, handler::accept);
	}

	public void run(Consumer<Handle> handler) {
		jdbi.useHandle(handler::accept);
	}

	public <T> T handle(Function<Handle, T> handler) {
		return jdbi.withHandle(handler::apply);
	}

	public Color getColor(long guild) {
		return Color.decode(handle(handle -> handle.createQuery("select color from guild_config where guild = :guild")
				.bind("guild", guild)
				.mapTo(String.class)
				.findOne().orElse(Main.config.color)
		));
	}

	public Color getColor(Guild guild) {
		return getColor(guild.getIdLong());
	}

	public long getSnowflake(Guild guild, String table, String field) {
		return handle(handle -> handle.createQuery("select " + field + " from " + table + " where guild = :guild")
				.bind("guild", guild.getIdLong())
				.mapTo(Long.class)
				.findOne()
				.orElse(0L)
		);
	}

	public MessageChannel getChannel(Guild guild, DatabaseField field) {
		return guild.getChannelById(MessageChannel.class, getSnowflake(guild, field.table, field.columnName));
	}

	public Role getRole(Guild guild, DatabaseField field) {
		return guild.getRoleById(getSnowflake(guild, field.table, field.columnName));
	}
}
