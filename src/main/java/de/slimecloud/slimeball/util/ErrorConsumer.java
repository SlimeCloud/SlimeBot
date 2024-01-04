package de.slimecloud.slimeball.util;

public interface ErrorConsumer<T> {
	void accept(T arg) throws Exception;
}
