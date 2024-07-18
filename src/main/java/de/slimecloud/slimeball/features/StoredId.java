package de.slimecloud.slimeball.features;

import de.mineking.databaseutils.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StoredId {
	@Column(key = true) private String type;
	@Column(key = true) private String id;
}
