package Storage.Positionals;

public class Particle {
  protected Vector3 position; // Meters from origin
  protected Vector3 velocity; // Meters/sec
  protected double radius; // Meters
  protected double mass; // Kg
  // 1 distance away and 1 mass, should produce 1 force
  void applyForce(double force, double interval) {
    // 1 force at 1 interval produces 1 distance change
    
  }
}