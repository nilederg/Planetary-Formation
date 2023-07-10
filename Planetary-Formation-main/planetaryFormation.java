import Storage.Positionals.GeoCoord;
import Storage.Positionals.Vector3;
import Storage.ScalarSphere;

import java.util.Scanner;

public class planetaryFormation {
  public static void main(String[] args) {
    ScalarSphere sphere = new ScalarSphere(5);
    sphere.
    System.out.println(sphere.getPoint(new GeoCoord(40, 20)));
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