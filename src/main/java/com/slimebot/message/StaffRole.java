package com.slimebot.message;

import com.slimebot.main.Main;
import net.dv8tion.jda.api.entities.Role;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

public class StaffRole {
	public final Role role;
	public final String description;

	private StaffRole(Role role, String description) {
		this.role = role;
		this.description = description;
	}

	public static class StaffRoleRowMapper implements RowMapper<StaffRole> {
		@Override
		public StaffRole map(ResultSet rs, StatementContext ctx) throws SQLException {
			return new StaffRole(
					Main.jdaInstance.getRoleById(rs.getLong("role")),
					rs.getString("description")
			);
		}
	}
}
