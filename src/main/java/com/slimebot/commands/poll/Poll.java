package com.slimebot.commands.poll;

import com.slimebot.database.DataClass;
import com.slimebot.database.Key;
import com.slimebot.main.Main;
import com.slimebot.util.Util;

import java.util.List;

public class Poll extends DataClass {

	@Key
	private final long id;

	private String json;

	private transient List<Long>[] values;

	public Poll(long id, int options) {
		this.id = id;
		this.values = Util.createListArray(options);
		generateJson();
	}

	@Override
	public synchronized Poll save() {
		generateJson();
		super.save();
		return this;
	}

	@Override
	@SuppressWarnings({"unchecked"})
	protected void finishedLoading() {
		this.values = Main.gson.fromJson(json, List[].class);
	}

	private void generateJson() {
		this.json = Main.gson.toJson(values);
	}

	private void remove(long member) {
		for (List<Long> value : values) {
			value.remove(member);
		}
	}

	public void set(int option, long member) {
		if (values[option].contains(member)) values[option].remove(member);
		else {
			remove(member);
			values[option].add(member);
		}
	}

	public List<Long> getOption(int option) {
		return values[option];
	}
}
