package storage.positionals

import java.util.function.Function

public data class Index(val x: Int, val y: Int) {
    // Rounds to the nearest multiple of an integer and outputs the result (a new Index)
    public fun toNearest(multiple: Int): Index {
        return Index(x - x % multiple, y - y % multiple)
    }
    public operator fun plus(additive: Index): Index {
        return Index(x + additive.x, y + additive.y)
    }
    public operator fun plus(additive: Int): Index {
        return Index(x + additive, y + additive)
    }
    public operator fun minus(subtractive: Index): Index {
        return Index(x - subtractive.x, y - subtractive.y)
    }
    public operator fun minus(subtractive: Int): Index {
        return Index(x - subtractive, y - subtractive)
    }
    public operator fun times(multiple: Int): Index {
        return Index(x * multiple, y * multiple)
    }
    public operator fun div(multiple: Int): Index {
        return Index(x / multiple, y / multiple)
    }
    public operator fun rem(multiple: Int): Index {
        return Index(x % multiple, y % multiple)
    }
    // Size is the size of one dimension of the 2d array
    // size^2 is the length of the accessed array
    public fun arrIndex(size: Int): Int {
        return x + y * size
    }
    public fun fromArrIndex(index: Int, size: Int): Index {
        return Index(index % size, index / size)
    }
    public fun toVector(size: Int): Vector2 {
        return Vector2(doubleArrayOf(x / size.toDouble(), y / size.toDouble()))
    }

    companion object {
        // Executes the function at every index
        public fun iterate(size: Int, function: java.util.function.Consumer<Index>) {
            for (x in 0 until size) {
                for (y in 0 until size) {
                    function.accept(Index(x, y))
                }
            }
        }
        public fun fromVector(point: Vector2, size: Int): Index {
            val x: Int = (point.getX() * size).toInt().coerceAtMost(size-1)
            val y: Int = (point.getY() * size).toInt().coerceAtMost(size-1)
            return Index(x, y)
        }
    }
}