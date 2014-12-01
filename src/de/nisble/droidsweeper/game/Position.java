package de.nisble.droidsweeper.game;

import java.io.Serializable;

/** Helper that stores the position of a filed in a matrix.<br>
 * <p>
 * <b>Immutable: Members are public final and capitalized.</b>
 * <b></b>
 * </p>
 * @author Moritz Nisblé moritz.nisble@gmx.de */
public final class Position implements Serializable {
	private static final long serialVersionUID = 1L;

	/** The horizontal coordinate. */
	public final int X;
	/** The vertical coordinate. */
	public final int Y;

	/** Initialize with invalid coordinates (-1,-1). */
	public Position() {
		X = -1;
		Y = -1;
	}

	/** Initialize with the given coordinates.
	 * @param x The horizontal coordinate.
	 * @param y The vertical coordinate. */
	public Position(int x, int y) {
		X = x;
		Y = y;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + X;
		result = prime * result + Y;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Position))
			return false;
		Position other = (Position) obj;
		if (X != other.X)
			return false;
		if (Y != other.Y)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Position [X=" + X + ", Y=" + Y + "]";
	}
}
