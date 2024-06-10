package storage.positionals

class Particle constructor() {
    protected var position: Vector3? = null // Meters from origin
    protected var velocity: Vector3? = null // Meters/sec
    protected var radius: Double = 0.0 // Meters
    protected var mass: Double = 0.0 // Kg

    // 1 distance away and 1 mass, should produce 1 force
    fun applyForce(force: Double, interval: Double) {
        // 1 force at 1 interval produces 1 distance change
    }
}