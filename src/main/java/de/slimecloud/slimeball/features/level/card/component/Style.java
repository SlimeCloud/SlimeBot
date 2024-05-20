package de.slimecloud.slimeball.features.level.card.component;

import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

public enum Style {
	SQUARE {
		@NotNull
		@Override
		public Shape getShape(int x, int y, int width, int height) {
			return new Rectangle2D.Double(x, y, width, height);
		}

		@Override
		public int getArc(int base) {
			return -1;
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
			return new RoundRectangle2D.Double(x, y, width, height, getArc(width), getArc(height));
		}

		@Override
		public int getArc(int base) {
			return (int) (Math.sqrt(base) * 5);
		}

		@Override
		public String toString() {
			return "Abgerundet";
		}
	},
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
	};

	@NotNull
	public abstract Shape getShape(int x, int y, int width, int height);

	public abstract int getArc(int base);

	@NotNull
	public static Style ofId(int id) {
		return values()[id % values().length];
	}
}
