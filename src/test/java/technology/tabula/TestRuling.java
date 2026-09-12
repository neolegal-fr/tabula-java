package technology.tabula;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestRuling {
	
	Ruling ruling;
	
	@BeforeEach
	public void setUpRuling() {
		ruling = new Ruling(0, 0, 10, 10);
	}

	@Test
	public void testGetWidth() {
		assertEquals(10f, ruling.getWidth(), 1e-5);
	}

	@Test
	public void testGetHeight() {
		assertEquals(10f, ruling.getHeight(), 1e-5);
	}

	@Test
	public void testToString() {
		assertEquals("class technology.tabula.Ruling[x1=0.000000 y1=0.000000 x2=10.000000 y2=10.000000]",ruling.toString());
	}
	
	@Test
	public void testEqualsOther() {
		Ruling other = new Ruling(0, 0, 11, 10);
		assertTrue(ruling.equals(ruling));
	}
	
	@Test
	public void testEqualsDifferentInstance() {
		assertFalse(ruling.equals("test"));
	}
	
	@Test
	public void testNearlyIntersects(){
		Ruling another = new Ruling(0, 0, 11, 10);

		assertTrue(ruling.nearlyIntersects(another));
	}
	
	@Test
	public void testGetPositionError(){
		assertThrows(UnsupportedOperationException.class, () -> {
			Ruling other = new Ruling(0, 0, 1, 1);
			other.getPosition();
		});
	}
	
	@Test
	public void testSetPositionError(){
		assertThrows(UnsupportedOperationException.class, () -> {
			Ruling other = new Ruling(0, 0, 1, 1);
			other.setPosition(5f);
		});
	}
	
	@Test
	public void testsetPosition(){
		assertThrows(UnsupportedOperationException.class, () -> {
			ruling.setPosition(0);
		});
	}
	
	@Test
	public void testGetStartError(){
		assertThrows(UnsupportedOperationException.class, () -> {
			Ruling other = new Ruling(0, 0, 1, 1);
			other.getStart();
		});
	}
	
	@Test
	public void testGetEndError(){
		assertThrows(UnsupportedOperationException.class, () -> {
			Ruling other = new Ruling(0, 0, 1, 1);
			other.getEnd();
		});
	}
	
	@Test
	public void testSetEndError(){
		assertThrows(UnsupportedOperationException.class, () -> {
			Ruling other = new Ruling(0, 0, 1, 1);
			other.setEnd(5f);
		});
	}
	
	
	@Test
	public void testColinear(){
//		Ruling another = new Ruling(0, 0, 500, 5);
		java.awt.geom.Point2D.Float float1 = new java.awt.geom.Point2D.Float(20, 20);
		java.awt.geom.Point2D.Float float2 = new java.awt.geom.Point2D.Float(0, 0);
		java.awt.geom.Point2D.Float float3 = new java.awt.geom.Point2D.Float(20, 0);
		java.awt.geom.Point2D.Float float4 = new java.awt.geom.Point2D.Float(0, 20);
		
		assertFalse(ruling.colinear(float1));
		assertTrue(ruling.colinear(float2));
		assertFalse(ruling.colinear(float3));
		assertFalse(ruling.colinear(float4));


	}

}
