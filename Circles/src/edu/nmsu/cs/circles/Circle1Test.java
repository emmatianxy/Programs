package edu.nmsu.cs.circles;

/***
 * Example JUnit testing class for Circle1 (and Circle)
 *
 * - must have your classpath set to include the JUnit jarfiles - to run the test do: java
 * org.junit.runner.JUnitCore Circle1Test - note that the commented out main is another way to run
 * tests - note that normally you would not have print statements in a JUnit testing class; they are
 * here just so you see what is happening. You should not have them in your test cases.
 ***/

import org.junit.*;

public class Circle1Test {
	// Data you need for each test case
	private Circle1 circle1;
	private Circle1 circle2;//added
	private Circle1 circle3;//added
	//
	// Stuff you want to do before each test case
	//
	@Before
	public void setup() {
		System.out.println("\nTest starting...");
		circle1 = new Circle1(1, 2, 3);
		circle2 = new Circle1(2,2,4);
		circle3 = new Circle1(8,8,1);
	}

	//
	// Stuff you want to do after each test case
	//
	@After
	public void teardown() {
		System.out.println("\nTest finished.");
	}

	//
	// Test a simple positive move
	//
	@Test
	public void simpleMove() {
		Point p;
		System.out.println("Running test simpleMove.");
		p = circle1.moveBy(1, 1);
		Assert.assertTrue(p.x == 2 && p.y == 3);
	}

	//
	// Test a simple negative move
	//
	@Test
	public void simpleMoveNeg() {
		Point p;
		System.out.println("Running test simpleMoveNeg.");
		p = circle1.moveBy(-1,-1);
		Assert.assertTrue(p.x == 0 && p.y == 1);
	}
	//
//testing Circle1 intersects() to see if it catches an intersecting circle
//if intersect works then the Circle2 should intersect with circle1 since they are so close
//and have larger r values
//
	@Test
	public void IntersectTest()
	{
		Assert.assertTrue(circle2.intersects(circle1)==true);
	}
	//
//testing Circle1 intersects() to see if it catches a non intersecting cirlce
//if intersect works then the circle3 should not intersect with circle1 since they are so far
//
	@Test
	public void IntersectTest2()
	{
		Assert.assertTrue(circle3.intersects(circle1)==false);
	}
	//
//testing the scale() function in super class circle
//if scale works then the circle2.r should go from 4 go 2 with a scale of .5 or 50%
//
	@Test
	public void ScaleS()
	{
		Assert.assertTrue(circle2.scale(.5)==2);
	}
	//
//testing the scale() function in super class circle
//if scale works then the circle2.r should go from 4 to 8 with a scale of 2 or 200%
//
	@Test
	public void ScaleL()
	{
		Assert.assertTrue(circle2.scale(2)==8);
	}

	public static void main(String[] args) {
		try {
			org.junit.runner.JUnitCore.runClasses(java.lang.Class.forName("Circle1Test"));
		} catch (Exception e) {
			System.out.println("Exception: " + e);
		}
	}
}


