package de.nisble.droidsweeper.utilities;

import java.io.Serializable;

import de.nisble.droidsweeper.game.jni.FieldStatus;

public final class Field implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public final Position POSITION;
	public final FieldStatus STATUS;
	public final int ADJACENT_BOMBS;

	public Field(Position position, FieldStatus status, int adjacentBombs) {
		POSITION = position;
		STATUS = status;
		ADJACENT_BOMBS = adjacentBombs;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ADJACENT_BOMBS;
		result = prime * result + ((POSITION == null) ? 0 : POSITION.hashCode());
		result = prime * result + ((STATUS == null) ? 0 : STATUS.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Field))
			return false;
		Field other = (Field) obj;
		if (ADJACENT_BOMBS != other.ADJACENT_BOMBS)
			return false;
		if (POSITION == null) {
			if (other.POSITION != null)
				return false;
		} else if (!POSITION.equals(other.POSITION))
			return false;
		if (STATUS != other.STATUS)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Field [POSITION=" + POSITION.toString() + ", STATUS=" + STATUS.toString() + ", ADJACENT_BOMBS="
				+ ADJACENT_BOMBS + "]";
	}
}
