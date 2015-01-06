package de.nisble.droidsweeper.utilities;

import java.io.Serializable;

/** Helper that stores the position of a filed in a matrix.
 * @author Moritz Nisblé moritz.nisble@gmx.de */
public final class Position implements Serializable {
	private static final long serialVersionUID = 1L;
	public final int X;
	public final int Y;

	public Position() {
		X = -1;
		Y = -1;
	}

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
