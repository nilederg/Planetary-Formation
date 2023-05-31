import Storage.Positionals.Vector3;

import java.util.Scanner;

public class planetaryFormation {
  public static void main(String[] args) {
    /*Scanner sc = new Scanner(System.in);
    Random random = new Random();
    double radius = getDouble("Planetary radius (meters): ", sc);*/
    Vector3 test = new Vector3(new double[]{1, 0.1, 1.3});
    test.normalizeCube();
    test.println();
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