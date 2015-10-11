package utils;
import java.util.Scanner;

/**
 * Restriction: A sequence of prohibited moves represented by the nodes passed
 * through, in order of their occurrence.
 * 
 * @author harryross
 * 
 */
public class Restriction {

	private int node;
	private int node1;
	private int node2;
	private int road1;
	private int road2;

	public Restriction(String line) {
		Scanner sc = new Scanner(line);
		sc.useDelimiter("\t");
		node1 = sc.nextInt();
		road1 = sc.nextInt();
		node = sc.nextInt();
		road2 = sc.nextInt();
		node2 = sc.nextInt();
		sc.close();
	}

	public int getRoad1() {
		return road1;
	}

	public void setRoad1(int road1) {
		this.road1 = road1;
	}

	public int getNode() {
		return node;
	}

	public int getNode1() {
		return node1;
	}

	public int getNode2() {
		return node2;
	}

	public int getRoad2() {
		return road2;
	}

	public void setRoad2(int road2) {
		this.road2 = road2;
	}

}
