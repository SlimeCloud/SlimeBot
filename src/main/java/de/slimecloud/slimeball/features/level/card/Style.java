package de.slimecloud.slimeball.features.level.card;

import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

public enum Style {
	ROUND {
		@NotNull
		@Override
		public Shape getShape(int x, int y, int width, int height) {
			return new Ellipse2D.Double(x, y, width, height);
		}

		@Override
		public int getArc(int base) {
			return base;
		}

		@Override
		public String toString() {
			return "Rund";
		}
	},
	SQUARE {
		@NotNull
		@Override
		public Shape getShape(int x, int y, int width, int height) {
			return new Rectangle2D.Double(x, y, width, height);
		}

		@Override
		public int getArc(int base) {
			return 0;
		}

		@Override
		public String toString() {
			return "Eckig";
		}
	},
	ROUND_SQUARE {
		@NotNull
		@Override
		public Shape getShape(int x, int y, int width, int height) {
			return new RoundRectangle2D.Double(x, y, width, height, (double) width / 4, (double) height / 4);
		}

		@Override
		public int getArc(int base) {
			return base / 2;
		}

		@Override
		public String toString() {
			return "Abgerundet";
		}
	};

	@NotNull
	public abstract Shape getShape(int x, int y, int width, int height);

	public abstract int getArc(int base);

	@NotNull
	public static Style ofId(int id) {
		return values()[id % values().length];
	}
}
