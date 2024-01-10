package de.slimecloud.slimeball.util.types;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.constant.Constable;
import java.lang.constant.ConstantDesc;
import java.util.Optional;

@AllArgsConstructor
@NoArgsConstructor
public class AtomicString implements java.io.Serializable, Comparable<String>, CharSequence, Constable {

	private String value = "";

	@NotNull
	public String getSafe() {
		return value == null ? "" : value;
	}

	@Override
	public int length() {
		return getSafe().length();
	}

	@Override
	public char charAt(int index) {
		return getSafe().charAt(index);
	}

	@NotNull
	@Override
	public CharSequence subSequence(int start, int end) {
		return getSafe().subSequence(start, end);
	}

	public void set(@Nullable String value) {
		this.value = value;
	}

	@Nullable
	public String get() {
		return value;
	}

	@Override
	public String toString() {
		return value;
	}

	@Override
	public int compareTo(@NotNull String o) {
		return getSafe().compareTo(o);
	}

	@Override
	public Optional<? extends ConstantDesc> describeConstable() {
		return getSafe().describeConstable();
	}
}
