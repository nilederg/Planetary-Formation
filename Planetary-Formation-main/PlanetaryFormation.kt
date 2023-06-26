import Storage.Positionals.GeoCoord;
import Storage.Positionals.Vector3;
import Storage.STL.TriangleFace;

import java.util.Arrays;
import java.util.Scanner;

public class planetaryFormation {
  public static void main(String[] args) {
    TerrestrialPlanet planet = new TerrestrialPlanet(5, 10000000000L);
    planet.initFractalNoise(5, 6);
    System.out.println(planet.terrain.getPoint(new GeoCoord(Math.toRadians(40), Math.toRadians(20))));
    System.out.println(planet.terrain.getPoint(new GeoCoord(Math.toRadians(40), Math.toRadians(21))));
    System.out.println(planet.terrain.getPoint(new GeoCoord(Math.toRadians(90), Math.toRadians(128))));

    System.out.println(Arrays.toString(TriangleFace.fromOrientingPoint(new Vector3(new double[]{0, 0, 0}), true, new Vector3[]{new Vector3(new double[]{9, 4, 5}), new Vector3(new double[]{9, 3, 8}), new Vector3(new double[]{16, 49, 1})}).exportCode()));
  }

  static double getDouble(String prompt, Scanner sc) {
    double radius;
    while (true) {
      try {
        System.out.print(prompt);
        radius = Double.parseDouble(sc.nextLine());
        break;
      } catch(NumberFormatException e) {
        System.out.println("\nNot a valid input. Please input a double.");
      }
    }
    return radius;
  }
}